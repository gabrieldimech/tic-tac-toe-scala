package tictactoe.input

import tictactoe.domain.Position

/** Turns raw text into domain values at the edge of the program. */
object Parser:
  /** Returns the cell for a number from 1 to 9, read left to right and top to
    * bottom, so 1 is top-left and 9 is bottom-right. Surrounding whitespace is
    * ignored. `Position.all` is already in that order, and `lift` makes the
    * lookup total, so a bad index is a `None` rather than an exception.
    */
  def position(raw: String): Either[InputError, Position] =
    val trimmed = raw.trim
    if trimmed.isEmpty then Left(InputError.Empty)
    else
      trimmed.toIntOption match
        case None    => Left(InputError.NotANumber(raw))
        case Some(n) =>
          Position.all.lift(n - 1).toRight(InputError.OutOfRange(n))
