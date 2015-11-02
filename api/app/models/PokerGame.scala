package models

import concurrent.Future
import concurrent.duration._
import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
import play.api.libs.ws.{ WS, Response }
import axle.game.poker._

object PokerGame {

  val pokerGame =
    get[Long]("game_id") ~
      get[Int]("num_players") map {
        case id ~ numPlayers => new Poker(numPlayers)
      }

  def find(gameId: Long) =
    DB.withConnection { implicit c =>
      SQL("select * from poker_game where game_id = {game_id}")
        .on('game_id -> gameId)
        .as(pokerGame *)
        .headOption
    }

  def insert(id: Long, game: Poker) =
    DB.withConnection { implicit c =>
      SQL("insert into poker_game (game_id, num_players) values ({id}, {num_players})").on(
        'id -> id,
        'num_players -> game._players.size
      ).executeInsert()
    }

  def applyAndMessageMove(game: Poker, s0: PokerState, move: PokerMove): Option[PokerState] =
    s0(move).map { s1 =>
      axle.game.poker.PokerState( // TODO: this calls for a zipper
        s1.playerFn,
        s1.deck,
        s1.shared,
        s1.numShown,
        s1.hands,
        s1.pot,
        s1.currentBet,
        s1.stillIn,
        s1.inFors,
        s1.piles,
        s1._outcome,
        game.players.map(p => { // clear out player's event queue; broadcast news to everyone
          (p, (
            if (p.toString equals s0.player.toString) Nil
            else s0._eventQueues.get(p).getOrElse(Nil)
          ) ++ List(move))
        }).toMap
      )(game)
    }

  def move(game: models.Game, which: String, amount: Option[Int])(implicit pokerGame: Poker) = {
    models.PokerState.find(game.lastState, pokerGame).map(state => {
      (state.player match {
        case pokerGame.dealer => {
          val (move, newState) = pokerGame.dealer.move(state)
          val messagedState = applyAndMessageMove(pokerGame, state, move).get // TODO: get
          Some((move, messagedState))
        }
        case _ =>
          (which match {
            case "fold" => Some(Fold(state.player))
            case "call" => Some(Call(state.player))
            case "raise" => amount.map(Raise(state.player, _))
          }) flatMap { move =>
            applyAndMessageMove(pokerGame, state, move).map { newState =>
              (move, newState)
            }
          }
      }) map {
        case (move, newState) =>
          models.PokerState.insert(game.id, pokerGame, newState) map { stateId =>
            models.Game.updateState(game.id, stateId)
          }
      }
    }
    )
  }

}