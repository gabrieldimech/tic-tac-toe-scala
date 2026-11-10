package tictactoe.domain

/** A fact about what happened in a game. Unlike a [[Command]], it cannot be
  * rejected, because it has already happened.
  */
enum Event:
  case MarkPlaced(player: Player, pos: Position)
  case GameWon(player: Player, line: Line)
  case GameDrawn
