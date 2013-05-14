package models

import axle.game.cards._
import axle.game.poker._

object Serialization {

  def serialize(player: PokerPlayer)(implicit game: Poker): String = player match {
    case game.dealer => "D"
    case _ => game._players.indexOf(player).toString
  }

  def serialize(deck: Deck): String = deck.cards.map(c => c.serialize).mkString(",")

  def serialize(outcome: PokerOutcome)(implicit game: Poker) = {
    val playerId = if (outcome.winner equals game.dealer) "D" else game._players.indexOf(outcome.winner)
    playerId + ":" + outcome.hand.map(_.cards.map(_.serialize).mkString(",")).getOrElse("")
  }

  def serialize(move: PokerMove)(implicit game: Poker) = {

    val playerId = if (move.player equals game.dealer) "D" else game._players.indexOf(move.player)

    val moveStr = move match {
      case Call(pokerPlayer) => "call"
      case Raise(pokerPlayer, amount) => "raise " + amount
      case Fold(pokerPlayer) => "fold"
      case Deal() => "deal"
      case Flop() => "flop"
      case Turn() => "turn"
      case River() => "river"
      case Payout() => "payout"
    }

    playerId + ":" + moveStr
  }

  def serialize(event: axle.game.Event[Poker])(implicit game: Poker): String = {
    event match {
      case outcome: PokerOutcome => serialize(outcome)
      case move: PokerMove => serialize(move)
    }
  }

  def splitSensibly(s: String, d: String): IndexedSeq[String] =
    if (s.length > 1) s.split(d) else Vector.empty

  def deserializeOutcome(outcome: String)(implicit game: Poker): PokerOutcome = {
    val player = game._players(outcome.substring(0, outcome.indexOf(":")).toInt)
    PokerOutcome(
      player,
      if (outcome.indexOf(":") + 1 == outcome.length)
        None
      else
        Some(PokerHand(splitSensibly(outcome.substring(outcome.indexOf(":") + 1), ",").map(Card(_)).toVector))
    )
  }

  def deserializeEvent(event: String)(implicit game: Poker) = {

    val playerId = event.substring(0, event.indexOf(":"))

    val player = if (playerId equals "D") game.dealer else game._players(playerId.toInt)

    event.substring(event.indexOf(":") + 1).split(" ").toList match {
      case "call" :: _ => Call(player)
      case "raise" :: amount :: _ => Raise(player, amount.toInt)
      case "fold" :: _ => Fold(player)
      case "deal" :: _ => Deal()
      case "flop" :: _ => Flop()
      case "turn" :: _ => Turn()
      case "river" :: _ => River()
      case "payout" :: _ => Payout()
      case _ => {
        PokerOutcome(
          player,
          if (event.indexOf(":") + 1 == event.length)
            None
          else
            Some(PokerHand(splitSensibly(event.substring(event.indexOf(":") + 1), ",").map(Card(_)).toVector))
        )
      }
    }
  }

  def deserializePlayer(s: String)(implicit game: Poker): PokerPlayer = s match {
    case "D" => game.dealer
    case _ => game._players(s.toInt)
  }

  def deserializeDeck(s: String) =
    Deck(s.split(",").map(Card(_)))

}
