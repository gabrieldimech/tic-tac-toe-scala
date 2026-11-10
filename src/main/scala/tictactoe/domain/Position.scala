package tictactoe.domain

/** A cell on the board. */
final case class Position(row: Coord, col: Coord)

object Position:
  /** Every cell, row by row, so callers never hard-code the board's size. */
  val all: List[Position] =
    for
      row <- Coord.values.toList
      col <- Coord.values.toList
    yield Position(row, col)
