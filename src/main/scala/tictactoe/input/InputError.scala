package tictactoe.input

/** The reasons a line of text is not a cell number. */
enum InputError:
  case Empty
  case NotANumber(raw: String)
  case OutOfRange(n: Int)
