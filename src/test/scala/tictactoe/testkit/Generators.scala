package tictactoe.testkit

import org.scalacheck.Gen
import tictactoe.domain.{Game, MoveError, Position}

object Generators:
  /** Generates the moves of one complete random game from [[Game.initial]]. */
  val legalMoves: Gen[List[Position]] = movesFrom(Game.initial)

  /** Picks a random free cell, plays it and recurses until the game ends. If
    * `play` rejects the move, the sequence stops with that move still in it, so
    * replaying it exposes the bug. Discarding it would hide the bug instead.
    */
  private def movesFrom(game: Game): Gen[List[Position]] = game match
    case Game.InProgress(board, _) =>
      for
        pos <- Gen.oneOf(board.emptyCells)
        rest <- Game.play(game, pos).fold(_ => Gen.const(Nil), movesFrom)
      yield pos :: rest
    case Game.Won(_, _, _) | Game.Drawn(_) => Gen.const(Nil)

  /** Returns the game after `moves`, or the first rejection. */
  def replay(moves: List[Position]): Either[MoveError, Game] =
    moves.foldLeft[Either[MoveError, Game]](Right(Game.initial)) {
      (game, pos) => game.flatMap(Game.play(_, pos))
    }

  /** Returns every state of a game, from [[Game.initial]] to the last move. */
  def states(moves: List[Position]): List[Either[MoveError, Game]] =
    moves.scanLeft[Either[MoveError, Game]](Right(Game.initial)) {
      (game, pos) => game.flatMap(Game.play(_, pos))
    }
