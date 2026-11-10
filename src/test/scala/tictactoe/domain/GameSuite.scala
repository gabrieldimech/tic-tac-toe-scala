package tictactoe.domain

import tictactoe.domain.BoardFixtures.boardOf
import tictactoe.domain.Coord.{One, Three, Two}
import tictactoe.domain.Player.{O, X}
import tictactoe.testkit.Generators.replay

class GameSuite extends munit.FunSuite:
  private val topLeft = Position(One, One)
  private val topMiddle = Position(One, Two)
  private val centre = Position(Two, Two)

  private val topRow = Line(topLeft, topMiddle, Position(One, Three))

  private val xWinsTopRow = List(
    (topLeft, X),
    (Position(Two, One), O),
    (topMiddle, X),
    (centre, O),
    (Position(One, Three), X)
  )

  /** X O X X O O O X X
    */
  private val drawnGame = List(
    (topLeft, X),
    (topMiddle, O),
    (Position(One, Three), X),
    (centre, O),
    (Position(Two, One), X),
    (Position(Two, Three), O),
    (Position(Three, Two), X),
    (Position(Three, One), O),
    (Position(Three, Three), X)
  )

  test("initial is an empty board with X to move") {
    assertEquals(Game.initial, Game.InProgress(Board.empty, X))
  }

  test("turns alternate, starting with X") {
    assertEquals(
      replay(List(topLeft)),
      Right(Game.InProgress(boardOf(List((topLeft, X))), O))
    )
    assertEquals(
      replay(List(topLeft, centre)),
      Right(Game.InProgress(boardOf(List((topLeft, X), (centre, O))), X))
    )
  }

  test("an occupied cell is rejected and the turn is not lost") {
    val game = Game.InProgress(boardOf(List((topLeft, X))), O)
    assertEquals(
      Game.play(game, topLeft),
      Left(MoveError.CellOccupied(topLeft))
    )
    assertEquals(
      Game.play(game, centre),
      Right(Game.InProgress(boardOf(List((topLeft, X), (centre, O))), X))
    )
  }

  test("a scripted sequence reaches X winning on the top row") {
    assertEquals(
      replay(xWinsTopRow.map(_._1)),
      Right(Game.Won(boardOf(xWinsTopRow), X, topRow))
    )
  }

  test("a scripted sequence draws") {
    assertEquals(
      replay(drawnGame.map(_._1)),
      Right(Game.Drawn(boardOf(drawnGame)))
    )
  }

  test("moves after a win are rejected, on free and occupied cells") {
    val won = Game.Won(boardOf(xWinsTopRow), X, topRow)
    List(Position(Three, Three), topLeft).foreach { pos =>
      assertEquals(Game.play(won, pos), Left(MoveError.GameAlreadyOver))
    }
  }

  test("moves after a draw are rejected") {
    val drawn = Game.Drawn(boardOf(drawnGame))
    assertEquals(Game.play(drawn, topLeft), Left(MoveError.GameAlreadyOver))
  }
