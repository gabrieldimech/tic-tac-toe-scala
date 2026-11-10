package tictactoe.domain

import tictactoe.domain.BoardFixtures.boardOf
import tictactoe.domain.Coord.{One, Three, Two}

class RulesSuite extends munit.FunSuite:
  private def cells(line: Line): List[Position] =
    List(line.a, line.b, line.c)

  /** Fills every cell: `o` for the given positions and X for the rest. */
  private def fullBoard(o: List[Position]): Board =
    boardOf(
      Position.all.map(pos =>
        (pos, if o.contains(pos) then Player.O else Player.X)
      )
    )

  private val topRow =
    Line(Position(One, One), Position(One, Two), Position(One, Three))

  test("lines has 8 distinct lines") {
    assertEquals(Rules.lines.distinct.size, 8)
  }

  test("each cell sits on the expected number of lines") {
    val coords = Coord.values.toList
    val counts = coords.map(row =>
      coords.map(col =>
        Rules.lines.count(line => cells(line).contains(Position(row, col)))
      )
    )
    assertEquals(counts, List(List(3, 2, 3), List(2, 4, 2), List(3, 2, 3)))
  }

  for
    line <- Rules.lines
    player <- Player.values.toList
  do
    test(s"$player wins on $line") {
      val board = boardOf(cells(line).map((_, player)))
      assertEquals(Rules.winner(board), Some((player, line)))
    }

  test("empty board has no winner") {
    assertEquals(Rules.winner(Board.empty), None)
  }

  test("two cells of a line are not a win") {
    val board = boardOf(
      List((topRow.a, Player.X), (topRow.b, Player.X))
    )
    assertEquals(Rules.winner(board), None)
  }

  test("a line held by both players is not a win") {
    val mixed = List(
      List(Player.O, Player.X, Player.X),
      List(Player.X, Player.O, Player.X),
      List(Player.X, Player.X, Player.O)
    )
    mixed.foreach { owners =>
      val board = boardOf(cells(topRow).zip(owners))
      assertEquals(Rules.winner(board), None)
    }
  }

  test("a full board with no line is Drawn") {
    val board = fullBoard(
      List(
        Position(One, Two),
        Position(Two, Two),
        Position(Two, Three),
        Position(Three, One)
      )
    )
    assertEquals(Rules.status(board, Player.O), Game.Drawn(board))
  }

  test("a full board with a line is Won, not Drawn") {
    val board = fullBoard(
      List(
        Position(Two, One),
        Position(Two, Two),
        Position(Three, Two),
        Position(Three, Three)
      )
    )
    assertEquals(
      Rules.status(board, Player.O),
      Game.Won(board, Player.X, topRow)
    )
  }

  test("a partial board with no line is InProgress with next to move") {
    val board = boardOf(List((topRow.a, Player.X)))
    Player.values.foreach { next =>
      assertEquals(Rules.status(board, next), Game.InProgress(board, next))
    }
  }
