package tictactoe.domain

import munit.ScalaCheckSuite
import org.scalacheck.Prop.forAllNoShrink
import tictactoe.testkit.Generators.{legalMoves, replay, states}

/** Uses `forAllNoShrink` because shrinking deletes moves from a failing list,
  * which would turn a real game into an unrelated, usually illegal, sequence.
  */
class GamePropertiesSuite extends ScalaCheckSuite:
  private def boardIn(game: Game): Board = game match
    case Game.InProgress(board, _) => board
    case Game.Won(board, _, _)     => board
    case Game.Drawn(board)         => board

  private def count(board: Board, player: Player): Int =
    Position.all.count(board.ownerOf(_).contains(player))

  property("every legal sequence ends in Won or Drawn within 9 moves") {
    forAllNoShrink(legalMoves) { moves =>
      val finished = replay(moves) match
        case Right(Game.Won(_, _, _) | Game.Drawn(_)) => true
        case Right(Game.InProgress(_, _)) | Left(_)   => false
      finished && moves.size <= 9
    }
  }

  property("X count is always O count or O count + 1") {
    forAllNoShrink(legalMoves) { moves =>
      states(moves).forall {
        case Right(game) =>
          val board = boardIn(game)
          val lead = count(board, Player.X) - count(board, Player.O)
          lead == 0 || lead == 1
        case Left(_) => false
      }
    }
  }

  property("play never rejects a cell from emptyCells of an InProgress game") {
    forAllNoShrink(legalMoves) { moves =>
      states(moves).forall {
        case Right(game @ Game.InProgress(board, _)) =>
          board.emptyCells.forall(Game.play(game, _).isRight)
        case Right(Game.Won(_, _, _) | Game.Drawn(_)) => true
        case Left(_)                                  => false
      }
    }
  }

  property("replaying the same positions always yields the same Game") {
    forAllNoShrink(legalMoves) { moves =>
      val first = replay(moves)
      first.isRight && first == replay(moves)
    }
  }
