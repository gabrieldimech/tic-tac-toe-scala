package tictactoe.interpreters

import tictactoe.app.{Action, Render}
import tictactoe.domain.Coord.{One, Three, Two}
import tictactoe.domain.Player.{O, X}
import tictactoe.domain.{Board, Game, Position}
import tictactoe.input.InputError
import tictactoe.testkit.ConsoleScripted.Scripted
import tictactoe.testkit.{ConsoleScripted, TestWorld}

class HumanMovesSuite extends munit.FunSuite:
  private val moves = HumanMoves[Scripted](ConsoleScripted)
  private val game: Game.InProgress = Game.InProgress(Board.empty, X)
  private val centre = Position(Two, Two)
  private val prompt = Render.prompt(X)

  private def run(inputs: String*): (TestWorld, Action) =
    moves.next(game).run(TestWorld.withInputs(inputs*)).value

  test("valid input gives Play") {
    val (_, action) = run("5")
    assertEquals(action, Action.Play(centre))
  }

  test("only the line that was needed is read") {
    val (world, _) = run("5", "6")
    assertEquals(world.inputs, List("6"))
  }

  test("q gives Quit straight away, with no error and no more reading") {
    val (world, action) = run("q", "5")
    assertEquals(action, Action.Quit)
    assertEquals(world.inputs, List("5"))
    assertEquals(world.outputs, Vector(prompt))
  }

  test("q is trimmed like any other input") {
    val (world, action) = run(" q ", "5")
    assertEquals(action, Action.Quit)
    assertEquals(world.inputs, List("5"))
  }

  test("no input left gives Quit") {
    val (world, action) = run()
    assertEquals(action, Action.Quit)
    assertEquals(world.outputs, Vector(prompt))
  }

  test("abc then 5 gives one error message, then Play") {
    val (world, action) = run("abc", "5")
    assertEquals(action, Action.Play(centre))
    assertEquals(
      world.outputs,
      Vector(
        prompt,
        Render.inputError(InputError.NotANumber("abc")) + "\n",
        prompt
      )
    )
  }

  test("input that closes after an error gives Quit") {
    val (_, action) = run("abc")
    assertEquals(action, Action.Quit)
  }

  test("a blank line is an error, not a quit") {
    val (world, action) = run("", "1")
    assertEquals(action, Action.Play(Position(One, One)))
    assert(world.outputs.contains(Render.inputError(InputError.Empty) + "\n"))
  }

  test("out-of-range input is an error, then the next cell is used") {
    val (_, action) = run("10", "9")
    assertEquals(action, Action.Play(Position(Three, Three)))
  }

  test("outputs contain the prompt for the player to move") {
    val (world, _) = moves
      .next(Game.InProgress(Board.empty, O))
      .run(TestWorld.withInputs("5"))
      .value
    assertEquals(world.outputs, Vector(Render.prompt(O)))
  }

  test("a very long run of bad input does not overflow the stack") {
    val bad = List.fill(200000)("abc")
    val (world, action) = run((bad :+ "5")*)
    assertEquals(action, Action.Play(centre))
    assertEquals(world.inputs, Nil)
  }
