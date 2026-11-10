package tictactoe.domain

import cats.syntax.all.*
import munit.ScalaCheckSuite
import org.scalacheck.Prop.forAllNoShrink
import tictactoe.testkit.Generators.{legalMoves, replay, states}

class DeciderPropertiesSuite extends ScalaCheckSuite:
  private def decideNext(
      game: Game,
      pos: Position
  ): Either[MoveError, List[Event]] = game match
    case Game.InProgress(_, next) =>
      Decider.decide(game, Command.Place(next, pos))
    case Game.Won(_, _, _) | Game.Drawn(_) => Left(MoveError.GameAlreadyOver)

  /** Returns all the events of a game, in order. */
  private def eventsOf(moves: List[Position]): Either[MoveError, List[Event]] =
    states(moves).zip(moves).flatTraverse { (state, pos) =>
      state.flatMap(decideNext(_, pos))
    }

  /** The rules as they were before `decide` and `evolve`, with no event in
    * sight, so they check the refactor independently.
    */
  private def directPlay(game: Game, pos: Position): Either[MoveError, Game] =
    game match
      case Game.InProgress(board, next) =>
        board.place(pos, next).map(Rules.status(_, next.other))
      case Game.Won(_, _, _) | Game.Drawn(_) =>
        Left(MoveError.GameAlreadyOver)

  property("replay of all events equals the final Game from play") {
    forAllNoShrink(legalMoves) { moves =>
      val played = replay(moves)
      played.isRight && eventsOf(moves).map(Decider.replay) == played
    }
  }

  property("play matches the rules applied without events") {
    forAllNoShrink(legalMoves) { moves =>
      val direct = moves.foldLeft[Either[MoveError, Game]](
        Right(Game.initial)
      )((game, pos) => game.flatMap(directPlay(_, pos)))
      direct.isRight && direct == replay(moves)
    }
  }
