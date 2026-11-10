package tictactoe.interpreters

import cats.effect.IO
import java.io.{EOFException, IOException}
import munit.CatsEffectSuite

class ConsoleIOSuite extends CatsEffectSuite:
  test("a line is returned") {
    assertIO(ConsoleIO.untilEof(IO.pure("5")), Some("5"))
  }

  test("an empty line is a line, not the end of input") {
    assertIO(ConsoleIO.untilEof(IO.pure("")), Some(""))
  }

  test("EOFException becomes None") {
    assertIO(ConsoleIO.untilEof(IO.raiseError(EOFException())), None)
  }

  test("any other failure stays a failure") {
    val errors = List(IOException("stdin broke"), RuntimeException("boom"))
    errors
      .map { error =>
        assertIO(
          ConsoleIO.untilEof(IO.raiseError(error)).attempt,
          Left(error)
        )
      }
      .foldLeft(IO.unit)(_ *> _)
  }
