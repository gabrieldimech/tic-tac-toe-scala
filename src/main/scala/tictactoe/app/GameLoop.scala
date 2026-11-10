package tictactoe.app

import cats.Monad
import cats.syntax.all.*
import tictactoe.domain.{Board, Game, Player}

/** Runs a game by joining the pure rules to the two algebras. It asks for one
  * [[MoveSource]] per player, so each side can be a person or anything else.
  *
  * The recursion goes through `flatMap`, so `F` needs a stack-safe `flatMap`,
  * as explained for `HumanMoves`.
  */
final class GameLoop[F[_]: Monad](
    console: Console[F],
    movesFor: Player => MoveSource[F]
):
  /** Plays from `game` until it ends or a player quits, and returns how the
    * session ended. A rejected move asks the same player again.
    */
  def run(game: Game): F[SessionResult] = game match
    case won @ Game.Won(board, _, _) =>
      finish(board, SessionResult.Completed(won))
    case drawn @ Game.Drawn(board) =>
      finish(board, SessionResult.Completed(drawn))
    case inProgress @ Game.InProgress(board, next) =>
      printLine(Render.grid(board))
        .flatMap(_ => movesFor(next).next(inProgress))
        .flatMap(handle(inProgress, _))

  /** Applies a player's action to the game. */
  private def handle(game: Game, action: Action): F[SessionResult] =
    action match
      case Action.Quit =>
        val result = SessionResult.Abandoned(game)
        printLine(Render.outcome(result)).as(result)
      case Action.Play(pos) =>
        Game.play(game, pos) match
          case Left(error) =>
            printLine(Render.moveError(error)).flatMap(_ => run(game))
          case Right(updated) => run(updated)

  /** Shows the final board and how the game ended. */
  private def finish(board: Board, result: SessionResult): F[SessionResult] =
    printLine(Render.grid(board))
      .flatMap(_ => printLine(Render.outcome(result)))
      .as(result)

  private def printLine(s: String): F[Unit] = console.print(s + "\n")
