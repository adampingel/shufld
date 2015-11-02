package models

import concurrent.Future
import concurrent.duration._
import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
import play.api.libs.ws.{ WS, Response }
import axle.game._

case class Game(id: Long, which: String, label: String, lastState: Long) {

  lazy val game = which match {
    case "ttt" => Some(new ttt.TicTacToe(3, "human", "ai"))
    case "poker" => PokerGame.find(id)
  }

}

object Game {

  val game =
    get[Long]("id") ~
      get[String]("which") ~
      get[String]("label") ~
      get[Long]("last_state") map {
        case id ~ which ~ label ~ last_state => Game(id, which, label, last_state)
      }

  def all(): List[Game] =
    DB.withConnection { implicit c =>
      SQL("select * from game where deleted = 'N'").as(game *)
    }

  def find(id: Long): Option[Game] =
    DB.withConnection { implicit c =>
      SQL("select * from game where id = {id}")
        .on('id -> id)
        .as(game *)
        .headOption
    }

  def insertPoker(id: Long) =
    models.Game.find(id).map(game => { // TODO: avoid having to look up what was just inserted
      val pokerGame = new poker.Poker(3)
      models.PokerState.insert(id, pokerGame, pokerGame.startState) map { last_state_id =>
        PokerGame.insert(id, pokerGame)
        models.Game.updateState(id, last_state_id)
      }
    })

  def insert(which: String, label: String) =
    DB.withConnection { implicit c =>
      SQL("insert into game (which, label, last_state) values ({which}, {label}, {last_state})").on(
        'which -> which,
        'label -> label,
        'last_state -> 0
      ).executeInsert()
    } map { id =>
      which match {
        case "poker" => insertPoker(id)
        case "ttt" => null // TicTacToeGame.create(1L, "human", "ai")
      }
      id
    }

  def updateState(id: Long, lastState: Long) =
    DB.withConnection { implicit c =>
      SQL("update game set last_state = {last_state} where id = {id}").on(
        'id -> id,
        'last_state -> lastState
      ).executeUpdate()
    }

  def delete(id: Long) =
    DB.withConnection { implicit c =>
      SQL("update game set deleted = 'Y' where id = {id}").on(
        'id -> id
      ).executeUpdate()
    }

}