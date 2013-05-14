package models

import concurrent.Future
import concurrent.duration._
import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
import play.api.libs.ws.{ WS, Response }
import axle.game._
import axle.game.cards._

object PokerState {

  def splitSensibly(s: String, d: String): IndexedSeq[String] =
    if (s.length > 1) s.split(d) else Vector.empty

  def rowParser(game: poker.Poker) =
    get[Long]("state_id") ~
      get[String]("player") ~
      get[String]("deck") ~
      get[String]("shared") ~
      get[Int]("num_shown") ~
      get[String]("hands") ~
      get[Int]("pot") ~
      get[Int]("current_bet") ~
      get[String]("still_in") ~
      get[String]("in_fors") ~
      get[String]("piles") ~
      get[Option[String]]("outcome") ~
      get[String]("event_queues") map {
        case state_id ~ player ~ deck ~ shared ~ num_shown ~ hands ~ pot ~ current_bet ~ still_in ~ in_fors ~ piles ~ outcome ~ event_queues =>
          new poker.PokerState(
            (st: poker.PokerState) => Serialization.deserializePlayer(player)(game),
            Serialization.deserializeDeck(deck),
            splitSensibly(shared, ",").map(Card(_)),
            num_shown,
            game._players.zip(splitSensibly(hands, "-")).map({ case (p, handString) => (p, splitSensibly(handString, ",").map(Card(_)).toList) }).toMap,
            pot,
            current_bet,
            still_in.split(",").map(i => game._players(i.toInt)).toSet,
            game._players.zip(splitSensibly(in_fors, ",")).flatMap({ case (p, amtStr) => if (amtStr equals "x") None else Some((p, amtStr.toInt)) }).toMap,
            game._players.zip(splitSensibly(piles, ",")).map({ case (p, amtStr) => (p, amtStr.toInt) }).toMap,
            outcome.map(Serialization.deserializeOutcome(_)(game)),
            game._players.zip(splitSensibly(event_queues, """\|\|"""))
              .map({ case (p, eventsStr) => (p, splitSensibly(eventsStr, "__").map(Serialization.deserializeEvent(_)(game)).toList) })
              .toMap
          )(game)
      }

  def find(id: Long, game: poker.Poker) =
    DB.withConnection { implicit c =>
      SQL("select * from poker_state where state_id = {state_id}")
        .on('state_id -> id)
        .as(rowParser(game) *)
        .headOption
    }

  def insert(gameId: Long, game: poker.Poker, state: poker.PokerState) = {
    DB.withConnection { implicit c =>
      SQL("""
        insert into poker_state
               (game_id, player, deck, shared, num_shown,
                hands, pot, current_bet,
                still_in, in_fors, piles,
                outcome, event_queues)
        values ({game_id}, {player}, {deck}, {shared}, {num_shown},
                {hands}, {pot}, {current_bet},
                {still_in}, {in_fors}, {piles},
                {outcome}, {event_queues})
""").on('game_id -> gameId,
        'player -> Serialization.serialize(state.player)(game),
        'deck -> Serialization.serialize(state.deck),
        'shared -> state.shared.map(_.serialize).mkString(","),
        'num_shown -> state.numShown,
        'hands -> game._players.map(player =>
          state.hands.get(player).getOrElse(Nil).map(_.serialize).mkString(",")
        ).mkString("-"),
        'pot -> state.pot,
        'current_bet -> state.currentBet,
        'still_in -> state.stillIn.map(game._players.indexOf(_)).mkString(","),
        'in_fors -> game._players.map(state.inFors.get(_).getOrElse("x")).mkString(","),
        'piles -> game._players.map(state.piles(_)).mkString(","),
        'outcome -> state.outcome.map(Serialization.serialize(_)(game)),
        'event_queues -> game._players.map(state._eventQueues.get(_).getOrElse(Nil).map(Serialization.serialize(_)(game)).mkString("__")).mkString("||")
      ).executeInsert()
    }
  }

}