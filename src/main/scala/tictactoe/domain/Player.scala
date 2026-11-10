package tictactoe.domain

/** One of the two players. */
enum Player:
  case X, O

  /** Returns the opponent, which is whose turn comes after this player's. */
  def other: Player = this match
    case X => O
    case O => X
