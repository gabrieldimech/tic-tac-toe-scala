package tictactoe.app

import tictactoe.domain.Game

/** Where moves come from, such as a person at a keyboard or a test script. */
trait MoveSource[F[_]]:
  /** Returns the next player's action. It takes `InProgress` so that a finished
    * game cannot be asked for a move.
    */
  def next(game: Game.InProgress): F[Action]
