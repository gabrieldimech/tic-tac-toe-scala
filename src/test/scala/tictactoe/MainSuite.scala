package tictactoe

import munit.CatsEffectSuite
import cats.effect.{ExitCode, IO}
import tictactoe.app.{Console, Render, SessionResult}
import tictactoe.domain.BoardFixtures.boardOf
import tictactoe.domain.Coord.{One, Three, Two}
import tictactoe.domain.Player.{O, X}
import tictactoe.domain.{Game, Line, Position}
import tictactoe.testkit.ConsoleScripted.Scripted
import tictactoe.testkit.{ConsoleRef, ConsoleScripted, TestWorld}

class MainSuite extends CatsEffectSuite:
  private val win = List("1", "4", "2", "5", "3")

  test("the full wiring plays a game to a win in IO") {
    val topRow = Line(
      Position(One, One),
      Position(One, Two),
      Position(One, Three)
    )
    val board = boardOf(
      List(
        (Position(One, One), X),
        (Position(Two, One), O),
        (Position(One, Two), X),
        (Position(Two, Two), O),
        (Position(One, Three), X)
      )
    )
    val expected = SessionResult.Completed(Game.Won(board, X, topRow))
    for
      console <- ConsoleRef.withInputs(win*)
      result <- Main.session[IO](console)
      printed <- console.printed
    yield
      assertEquals(result, expected)
      assertEquals(
        printed.count(_ == Render.outcome(expected) + "\n"),
        1
      )
      assertEquals(printed.lastOption, Some(Render.outcome(expected) + "\n"))
  }

  test("IO and State give the same result and the same transcript") {
    val inputs = List("1", "abc", "1", "2", "10", "5", "q")
    val (world, stateResult) = Main
      .session[Scripted](ConsoleScripted)
      .run(TestWorld.withInputs(inputs*))
      .value
    for
      console <- ConsoleRef.withInputs(inputs*)
      ioResult <- Main.session[IO](console)
      printed <- console.printed
    yield
      assertEquals(ioResult, stateResult)
      assertEquals(printed, world.outputs)
  }

  test("a long run of bad input does not overflow the stack in IO") {
    val inputs = List.fill(200000)("abc") :+ "q"
    for
      console <- ConsoleRef.withInputs(inputs*)
      result <- Main.session[IO](console)
    yield assertEquals(result, SessionResult.Abandoned(Game.initial))
  }

  test("a failure in the console is a defect: the session fails with it") {
    val boom = new RuntimeException("boom")
    val broken = new Console[IO]:
      def readLine: IO[Option[String]] = IO.raiseError(boom)
      def print(s: String): IO[Unit] = IO.unit
    assertIO(Main.session[IO](broken).attempt, Left(boom))
  }

  test("an unknown argument is an expected failure: exit code 2, not 1") {
    assertIO(Main.run(List("--nope")), ExitCode(2))
  }
