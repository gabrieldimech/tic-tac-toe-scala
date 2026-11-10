package tictactoe.interpreters

import cats.Applicative
import cats.syntax.all.*
import tictactoe.app.{Action, MoveSource}
import tictactoe.domain.{Game, Minimax}

/** A [[tictactoe.app.MoveSource]] that never asks anyone, so it needs no
  * [[tictactoe.app.Console]]. Each move is one pure step, so `Applicative` is
  * enough.
  */
final class MinimaxMoves[F[_]: Applicative] extends MoveSource[F]:
  /** Returns the move [[tictactoe.domain.Minimax.bestMove]] chooses, or `Quit`
    * if it has none, so an unplayable game ends the session instead of being
    * asked again forever.
    */
  def next(game: Game.InProgress): F[Action] =
    Minimax.bestMove(game).fold(Action.Quit)(Action.Play(_)).pure[F]
