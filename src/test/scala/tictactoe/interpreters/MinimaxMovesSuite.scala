package tictactoe.interpreters

import cats.Id
import tictactoe.app.Action
import tictactoe.domain.BoardFixtures.boardOf
import tictactoe.domain.{Board, Coord, Game, Player, Position}

class MinimaxMovesSuite extends munit.FunSuite:
  test("plays the move Minimax chooses, with only Applicative required") {
    val game: Game.InProgress = Game.InProgress(Board.empty, Player.X)
    assertEquals(
      MinimaxMoves[Id].next(game),
      Action.Play(Position(Coord.One, Coord.One))
    )
  }

  test("a game with no free cell quits instead of inventing a move") {
    val full = boardOf(Position.all.map((_, Player.X)))
    assertEquals(
      MinimaxMoves[Id].next(Game.InProgress(full, Player.O)),
      Action.Quit
    )
  }
