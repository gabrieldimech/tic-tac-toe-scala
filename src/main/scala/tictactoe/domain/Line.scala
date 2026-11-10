package tictactoe.domain

/** Three cells that win the game when one player holds all of them. */
final case class Line(a: Position, b: Position, c: Position)
