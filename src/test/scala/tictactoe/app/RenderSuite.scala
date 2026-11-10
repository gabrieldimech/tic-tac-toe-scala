package tictactoe.app

import tictactoe.domain.BoardFixtures.boardOf
import tictactoe.domain.Coord.{One, Three, Two}
import tictactoe.domain.Player.{O, X}
import tictactoe.domain.{Board, Game, Line, MoveError, Position}
import tictactoe.input.{InputError, Parser}

class RenderSuite extends munit.FunSuite:
  private val topLeft = Position(One, One)
  private val centre = Position(Two, Two)
  private val topRow =
    Line(topLeft, Position(One, Two), Position(One, Three))

  test("an empty board shows every cell number") {
    assertEquals(
      Render.grid(Board.empty),
      """| 1 | 2 | 3
         |---+---+---
         | 4 | 5 | 6
         |---+---+---
         | 7 | 8 | 9""".stripMargin
    )
  }

  test("a mid-game board shows marks and the remaining numbers") {
    assertEquals(
      Render.grid(boardOf(List((centre, X), (topLeft, O)))),
      """| O | 2 | 3
         |---+---+---
         | 4 | X | 6
         |---+---+---
         | 7 | 8 | 9""".stripMargin
    )
  }

  test("a full board shows no numbers") {
    val moves = List(
      (topLeft, X),
      (Position(One, Two), O),
      (Position(One, Three), X),
      (Position(Two, One), X),
      (centre, O),
      (Position(Two, Three), O),
      (Position(Three, One), O),
      (Position(Three, Two), X),
      (Position(Three, Three), X)
    )
    assertEquals(
      Render.grid(boardOf(moves)),
      """| X | O | X
         |---+---+---
         | X | O | O
         |---+---+---
         | O | X | X""".stripMargin
    )
  }

  test("each number shown is the one the parser reads for that cell") {
    (1 to 9).foreach { n =>
      val board = Parser
        .position(n.toString)
        .map(pos => boardOf(List((pos, X))))
        .fold(error => fail(s"cell $n did not parse: $error"), identity)
      val shown = Render.grid(board)
      assert(shown.contains("X"), shown)
      assert(!shown.contains(n.toString), shown)
    }
  }

  test("prompt names the player") {
    assertEquals(Render.prompt(X), "Player X, choose a cell (1-9, q to quit): ")
    assertEquals(Render.prompt(O), "Player O, choose a cell (1-9, q to quit): ")
  }

  test("moveError: CellOccupied gives the number the player typed") {
    assertEquals(
      Render.moveError(MoveError.CellOccupied(centre)),
      "Cell 5 is already taken."
    )
  }

  test("moveError: GameAlreadyOver") {
    assertEquals(
      Render.moveError(MoveError.GameAlreadyOver),
      "The game is already over."
    )
  }

  test("moveError: NotYourTurn") {
    assertEquals(
      Render.moveError(MoveError.NotYourTurn(O)),
      "It is not player O's turn."
    )
  }

  test("inputError: Empty") {
    assertEquals(
      Render.inputError(InputError.Empty),
      "Nothing entered. Enter a number from 1 to 9."
    )
  }

  test("inputError: NotANumber quotes the input") {
    assertEquals(
      Render.inputError(InputError.NotANumber("abc")),
      "'abc' is not a cell. Enter a number from 1 to 9."
    )
  }

  test("inputError: OutOfRange quotes the number") {
    assertEquals(
      Render.inputError(InputError.OutOfRange(10)),
      "10 is not a cell. Enter a number from 1 to 9."
    )
  }

  test("outcome: Won names the winner") {
    val board = boardOf(List((topLeft, X)))
    assertEquals(
      Render.outcome(SessionResult.Completed(Game.Won(board, O, topRow))),
      "Player O wins!"
    )
  }

  test("outcome: Drawn") {
    assertEquals(
      Render.outcome(SessionResult.Completed(Game.Drawn(Board.empty))),
      "It's a draw."
    )
  }

  test("outcome: Abandoned, whatever state the game was in") {
    assertEquals(
      Render.outcome(SessionResult.Abandoned(Game.initial)),
      "Game abandoned."
    )
  }
