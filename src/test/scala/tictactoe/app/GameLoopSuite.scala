package tictactoe.app

import cats.data.State
import munit.ScalaCheckSuite
import org.scalacheck.Prop.forAllNoShrink
import tictactoe.domain.BoardFixtures.boardOf
import tictactoe.domain.Coord.{One, Three, Two}
import tictactoe.domain.Player.{O, X}
import tictactoe.domain.{Board, Game, Line, MoveError, Player, Position}
import tictactoe.interpreters.HumanMoves
import tictactoe.testkit.ConsoleScripted.Scripted
import tictactoe.testkit.Generators.{legalMoves, replay}
import tictactoe.testkit.{ConsoleScripted, TestWorld}

class GameLoopSuite extends ScalaCheckSuite:
  private val human = HumanMoves[Scripted](ConsoleScripted)
  private val loop = GameLoop[Scripted](ConsoleScripted, _ => human)

  private val topLeft = Position(One, One)
  private val topMiddle = Position(One, Two)
  private val topRow = Line(topLeft, topMiddle, Position(One, Three))

  private def line(s: String): String = s + "\n"
  private def grid(board: Board): String = line(Render.grid(board))

  private def play(inputs: String*): (TestWorld, SessionResult) =
    loop.run(Game.initial).run(TestWorld.withInputs(inputs*)).value

  test("X wins on a scripted sequence") {
    val (world, result) = play("1", "4", "2", "5", "3")
    val board = boardOf(
      List(
        (topLeft, X),
        (Position(Two, One), O),
        (topMiddle, X),
        (Position(Two, Two), O),
        (Position(One, Three), X)
      )
    )
    val expected = SessionResult.Completed(Game.Won(board, X, topRow))
    assertEquals(result, expected)
    assertEquals(
      world.outputs.takeRight(2),
      Vector(grid(board), line(Render.outcome(expected)))
    )
  }

  test("a full game draws") {
    val (world, result) = play("1", "2", "3", "5", "4", "6", "8", "7", "9")
    val board = boardOf(
      List(
        (topLeft, X),
        (topMiddle, O),
        (Position(One, Three), X),
        (Position(Two, Two), O),
        (Position(Two, One), X),
        (Position(Two, Three), O),
        (Position(Three, Two), X),
        (Position(Three, One), O),
        (Position(Three, Three), X)
      )
    )
    val expected = SessionResult.Completed(Game.Drawn(board))
    assertEquals(result, expected)
    assertEquals(
      world.outputs.takeRight(2),
      Vector(grid(board), line(Render.outcome(expected)))
    )
  }

  test("an occupied cell shows an error and re-prompts the same player") {
    val (world, _) = play("1", "1", "q")
    val afterX = boardOf(List((topLeft, X)))
    assertEquals(
      world.outputs,
      Vector(
        grid(Board.empty),
        Render.prompt(X),
        grid(afterX),
        Render.prompt(O),
        line(Render.moveError(MoveError.CellOccupied(topLeft))),
        grid(afterX),
        Render.prompt(O),
        line(Render.outcome(SessionResult.Abandoned(Game.initial)))
      )
    )
  }

  test("a rejected move leaves the game unchanged") {
    val (_, result) = play("1", "1", "2", "q")
    val board = boardOf(List((topLeft, X), (topMiddle, O)))
    assertEquals(result, SessionResult.Abandoned(Game.InProgress(board, X)))
  }

  test("q mid-game is Abandoned with the game so far") {
    val (world, result) = play("1", "q", "2")
    val game = Game.InProgress(boardOf(List((topLeft, X))), O)
    assertEquals(result, SessionResult.Abandoned(game))
    assertEquals(world.inputs, List("2"))
  }

  test("inputs running out is Abandoned with the game so far") {
    val (world, result) = play("1")
    val abandoned = SessionResult.Abandoned(
      Game.InProgress(boardOf(List((topLeft, X))), O)
    )
    assertEquals(result, abandoned)
    assertEquals(
      world.outputs.lastOption,
      Some(line(Render.outcome(abandoned)))
    )
  }

  test("a finished game prints its ending and asks nobody") {
    val board = boardOf(List((topLeft, X)))
    val game: Game.Won = Game.Won(board, X, topRow)
    val (world, result) = loop.run(game).run(TestWorld.withInputs("5")).value
    assertEquals(result, SessionResult.Completed(game))
    assertEquals(world.inputs, List("5"))
    assertEquals(world.outputs.size, 2)
  }

  test("each turn asks the source for that player") {
    val quitting = new MoveSource[Scripted]:
      def next(game: Game.InProgress): Scripted[Action] =
        State.pure(Action.Quit)
    def source(player: Player): MoveSource[Scripted] = player match
      case X => human
      case O => quitting
    val (world, result) = GameLoop[Scripted](ConsoleScripted, source)
      .run(Game.initial)
      .run(TestWorld.withInputs("1", "2"))
      .value
    assertEquals(
      result,
      SessionResult.Abandoned(
        Game.InProgress(boardOf(List((topLeft, X))), O)
      )
    )
    assertEquals(world.inputs, List("2"))
  }

  test("a very long run of rejected moves does not overflow the stack") {
    val repeats = List.fill(100000)("1")
    val (world, _) = play(("1" +: repeats :+ "q")*)
    assertEquals(world.inputs, Nil)
  }

  property("any legal game played as typed input matches Game.play") {
    forAllNoShrink(legalMoves) { moves =>
      val inputs = moves.map(pos => (Position.all.indexOf(pos) + 1).toString)
      val (_, result) = play(inputs*)
      replay(moves).exists(game =>
        result match
          case SessionResult.Completed(finished) => finished == game
          case SessionResult.Abandoned(_)        => false
      )
    }
  }
