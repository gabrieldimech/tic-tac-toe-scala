package tictactoe.domain

/** A request to change a game. It may be rejected. */
enum Command:
  case Place(player: Player, pos: Position)
