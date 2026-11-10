package tictactoe.interpreters

import cats.effect.IO
import cats.syntax.all.*
import java.io.EOFException
import tictactoe.app.Console

/** The real terminal. This and `Main` are the only code that touches `IO`. */
object ConsoleIO extends Console[IO]:
  /** Returns the next line, or `None` once input is closed. */
  val readLine: IO[Option[String]] = untilEof(IO.readLine)

  /** Writes `s` to standard output, with no newline. */
  def print(s: String): IO[Unit] = IO.print(s)

  /** Turns the end of input into `None`. It catches `EOFException` and nothing
    * else, so any other failure stays a failure. Separate from `readLine` so
    * that this can be tested without real standard input.
    */
  private[interpreters] def untilEof(read: IO[String]): IO[Option[String]] =
    read.map(_.some).recover { case _: EOFException => None }
