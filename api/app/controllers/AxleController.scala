package controllers

import scala.concurrent.duration.DurationInt
import com.twitter.zookeeper.ZooKeeperClient
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import akka.actor.actorRef2Scala
import axle.game._
import models._
import play.api.Logger
import play.api.Play.current
import play.api.data.Form
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.iteratee.Enumerator
import play.api.libs.ws.WS
import play.api.mvc.Action
import play.api.mvc.AsyncResult
import play.api.mvc.Controller
import play.api.mvc.ResponseHeader
import play.api.mvc.SimpleResult
import play.api.mvc.AnyContentAsFormUrlEncoded

object Protocol {
  case class DoFoo()
  case class FooTick()
}

class FooActor extends Actor with ActorLogging {
  import Protocol._
  def receive = {
    case DoFoo() ⇒ log.info("do foo")
    case FooTick() ⇒ log.info("foo tick")
    case u @ _ ⇒ log.info(this + " can't handle " + u)
  }
}

object AxleController extends Controller {

  // see https://github.com/twitter/scala-zookeeper-client

  //val zooKeeperClient = new ZooKeeperClient("localhost:2181")

  //val mongoIp = "127.0.0.1" // new String(zooKeeperClient.get("/mongodb/mongodb1"))

  //val fooActor = Akka.system.actorOf(Props[FooActor], name = "fooActor")

  //Akka.system.scheduler.schedule(0.seconds, 10.seconds, fooActor, Protocol.FooTick())

  //  def index = Action {
  //    Redirect(routes.AxleController.games)
  //  }

  // request.body match { case AnyContentAsFormUrlEncoded(params) => params.get("amount").flatMap(_.headOption).map(_.toInt)

  def index = Action {
    Ok(views.html.index(Game.all, Forms.createGameForm))
  }

  def login = Action {
    Ok(views.html.login())
  }

  def loginPost = Action { implicit request =>
    Forms.loginForm.bindFromRequest.fold(
      error => {
        Logger.info("bad request " + error.toString)
        BadRequest(error.toString)
      },
      //      {
      //        case (openid) => AsyncResult(OpenID.redirectURL(openid, routes.AxleController.openIDCallback.absoluteURL())
      //          .extend(_.value match {
      //            case Redeemed(url) => Redirect(url)
      //            case Thrown(t) => Redirect(routes.Application.login)
      //          })
      //        )
      //      }
      ???
    )
  }

  def openIDCallback = Action { implicit request =>
    AsyncResult(
      ???
    //      OpenID.verifiedId.extend(_.value match {
    //        case Redeemed(info) => Ok(info.id + "\n" + info.attributes)
    //        case Thrown(t) => {
    //          // Here you should look at the error, and give feedback to the user
    //          Redirect(routes.Application.login)
    //        }
    //      })
    )
  }

  def game(id: Long) = Action {
    Game.find(id).flatMap(game => {
      val g = game.game.get
      game.which match {
        case "ttt" => {
          val tttGame = g.asInstanceOf[ttt.TicTacToe]
          Some(Ok(views.html.ttt(game, tttGame.startState)))
        }
        case "poker" => {
          val pokerGame = g.asInstanceOf[poker.Poker]
          models.PokerState.find(game.lastState, pokerGame) map { state =>
            Ok(views.html.poker(game, pokerGame, state, Forms.moveForm))
          }
        }
        case _ => Some(BadRequest("no such game type"))
      }
    }
    ).getOrElse(BadRequest("no such game id"))
  }

  def games = Action {
    Ok(views.html.index(Game.all, Forms.createGameForm))
  }

  def continue(id: Long) = Action { implicit request =>
    Game.find(id).map(game => {
      val g = game.game.get
      game.which match {
        case "poker" => {
          val pokerGame = g.asInstanceOf[poker.Poker]
          models.PokerState.find(game.lastState, pokerGame).map { previous =>
            pokerGame.startFrom(previous).map(continuingState =>
              models.PokerState.insert(id, pokerGame, continuingState) map { last_state_id =>
                models.Game.updateState(id, last_state_id)
              }
            )
          }
        }
        case _ =>
      }
    })
    Redirect(routes.AxleController.game(id))
  }

  def newGame = Action { implicit request =>
    Forms.createGameForm.bindFromRequest.fold(
      errors => BadRequest(views.html.index(Game.all(), errors)),
      args => {
        val (which, label) = args
        Game.insert(which, label).map({ id =>
          Redirect(routes.AxleController.game(id))
        }).getOrElse(
          Redirect(routes.AxleController.games)
        )
      }
    )
  }

  def move(id: Long) = Action { implicit request =>
    Game.find(id).map(game => {
      val g = game.game.get
      game.which match {
        case "ttt" => {
          // TODO TicTacToeGame.move(game)(g.asInstanceOf[TicTacToe])
          Redirect(routes.AxleController.game(id))
        }
        case "poker" =>
          Forms.moveForm.bindFromRequest.fold(
            errors => BadRequest(s"errors $errors"),
            args => {
              val (which, amountOpt) = args
              PokerGame.move(game, which, amountOpt)(g.asInstanceOf[poker.Poker])
              Redirect(routes.AxleController.game(id))
            }
          )
        case _ => BadRequest(s"unknown game type ${game.which}")
      }
    }).getOrElse(BadRequest(s"no such game $id"))
  }

  def deleteGame(id: Long) = Action {
    Game.delete(id)
    Redirect(routes.AxleController.games)
  }

  def simple = Action {
    SimpleResult(
      header = ResponseHeader(200, Map(CONTENT_TYPE -> "text/plain")),
      body = Enumerator("Hello world!")
    )
  }

  def hello = Action {
    //    Akka.system.scheduler.scheduleOnce(10.seconds) {
    //      fooActor ! Protocol.DoFoo()
    //    }
    Ok("Hello world!")
  }

  def notFound = NotFound

  def pageNotFound = NotFound(<h1>Page not found</h1>)

  def badRequest = BadRequest("bad request") // views.html.form(formWithErrors))

  def fiveHundred = InternalServerError("Oops")

  def teapot = Status(418)("I'm a teapot")

  def redir = Action {
    Redirect("/user/home") // , status = MOVED_PERMANENTLY)
  }

  val feedUrl = "http://api.geonames.org/earthquakesJSON?north=44.1&south=-9.9&east=-22.4&west=55.2&username=demo"

  def earthquakes = Action {
    Async {
      WS.url(feedUrl).get().map { response =>
        // "Feed title: " + (response.json \ "title").as[String]
        Ok(views.html.earthquakes(response.json))
      }
    }
  }

}