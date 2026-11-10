package tictactoe.domain

/** The reasons a move can be rejected. `NotYourTurn` holds the player who tried
  * to move.
  */
enum MoveError:
  case CellOccupied(pos: Position)
  case GameAlreadyOver
  case NotYourTurn(player: Player)
