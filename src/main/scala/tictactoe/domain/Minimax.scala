package tictactoe.domain

/** A perfect player. It searches every legal continuation through
  * [[Game.play]], so it can only ever consider moves the rules allow.
  */
object Minimax:
  /** Returns the best cell for the player to move, or `None` if no cell is
    * free. `Game.play` never produces such a game, but the `Game` constructors
    * are public, so the type says so rather than inventing a move. Ties go to
    * the earliest cell in [[Position.all]] order, so the same game always gets
    * the same move.
    */
  def bestMove(game: Game.InProgress): Option[Position] =
    scoredMoves(game).maxByOption(_._2).map(_._1)

  /** Pairs each legal move with its score for the player making it. */
  private def scoredMoves(game: Game.InProgress): List[(Position, Int)] =
    game.board.emptyCells.flatMap(pos =>
      Game.play(game, pos).toOption.map(after => (pos, score(after)))
    )

  /** Scores `after` for the player who has just moved. The sign is the outcome:
    * positive for a win, 0 for a draw, negative for a loss. The size is the
    * free cells left plus one, so an earlier win scores higher and a later loss
    * scores less badly. In a live game the opponent picks the move that is best
    * for them, which is worst for this player, hence the negation.
    */
  private def score(after: Game): Int = after match
    case Game.Won(board, _, _)        => board.emptyCells.size + 1
    case Game.Drawn(_)                => 0
    case next @ Game.InProgress(_, _) =>
      -scoredMoves(next).map(_._2).maxOption.getOrElse(0)
