package tictactoe.domain

class BoardSuite extends munit.FunSuite:
  private val centre = Position(Coord.Two, Coord.Two)

  private val full: Either[MoveError, Board] =
    Position.all.foldLeft[Either[MoveError, Board]](Right(Board.empty)) {
      (board, pos) => board.flatMap(_.place(pos, Player.X))
    }

  test("empty board has 9 empty cells and is not full") {
    assertEquals(Board.empty.emptyCells, Position.all)
    assert(!Board.empty.isFull)
  }

  test("place on an empty cell claims only that cell") {
    val placed = Board.empty.place(centre, Player.X)
    assertEquals(placed.map(_.ownerOf(centre)), Right(Some(Player.X)))
    assertEquals(
      placed.map(_.emptyCells),
      Right(Position.all.filterNot(_ == centre))
    )
  }

  test("place on an occupied cell returns CellOccupied with that position") {
    Player.values.foreach { second =>
      val result =
        Board.empty.place(centre, Player.X).flatMap(_.place(centre, second))
      assertEquals(result, Left(MoveError.CellOccupied(centre)))
    }
  }

  test("a full board isFull and has no empty cells") {
    assertEquals(full.map(_.isFull), Right(true))
    assertEquals(full.map(_.emptyCells), Right(Nil))
  }
