package tictactoe.domain

object BoardFixtures extends munit.Assertions:
  /** Builds a board from explicit moves, failing the test on an illegal one. */
  def boardOf(moves: List[(Position, Player)]): Board =
    moves.foldLeft(Board.empty) { case (board, (pos, player)) =>
      board
        .place(pos, player)
        .fold(error => fail(s"invalid fixture: $error"), identity)
    }
