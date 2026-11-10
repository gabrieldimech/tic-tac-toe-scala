package tictactoe

import tictactoe.app.{Render, SessionResult}
import tictactoe.domain.{Game, Player}
import tictactoe.testkit.ConsoleScripted.Scripted
import tictactoe.testkit.{ConsoleScripted, TestWorld}

class MainArgsSuite extends munit.FunSuite:
  private def vsAi(inputs: String*): (TestWorld, SessionResult) = Main
    .session[Scripted](ConsoleScripted, Opponent.Ai)
    .run(TestWorld.withInputs(inputs*))
    .value

  test("no arguments means human vs human") {
    assertEquals(Main.parseArgs(Nil), Some(Opponent.Human))
  }

  test("--vs-ai means O is the AI") {
    assertEquals(Main.parseArgs(List("--vs-ai")), Some(Opponent.Ai))
  }

  test("anything else is not understood") {
    List(List("--nope"), List("--vs-ai", "extra")).foreach { args =>
      assertEquals(Main.parseArgs(args), None)
    }
  }

  test("with the AI as O, only X is ever prompted") {
    val (world, _) = vsAi("5", "1", "2", "3", "4", "6", "7", "8", "9")
    assert(world.outputs.contains(Render.prompt(Player.X)))
    assert(!world.outputs.contains(Render.prompt(Player.O)))
  }

  test("a person cannot beat the AI through the whole program") {
    val (_, result) = vsAi("1", "2", "3", "4", "5", "6", "7", "8", "9")
    result match
      case SessionResult.Completed(Game.Won(_, winner, _)) =>
        assertEquals(winner, Player.O)
      case SessionResult.Completed(Game.Drawn(_)) => ()
      case SessionResult.Abandoned(_)             =>
        fail(s"the game did not finish: $result")
  }
