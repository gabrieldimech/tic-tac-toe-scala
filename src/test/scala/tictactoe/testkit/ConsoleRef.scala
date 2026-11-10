package tictactoe.testkit

import cats.effect.{IO, Ref}
import tictactoe.app.Console

/** A [[Console]] in real `IO`, backed by `Ref`s, so the program runs on the
  * Cats Effect runtime with no terminal.
  */
final class ConsoleRef(
    inputs: Ref[IO, List[String]],
    outputs: Ref[IO, Vector[String]]
) extends Console[IO]:
  /** Removes and returns the next line, or `None` once none are left. */
  val readLine: IO[Option[String]] = inputs.modify {
    case Nil          => (Nil, None)
    case line :: rest => (rest, Some(line))
  }

  /** Appends `s` to the outputs. */
  def print(s: String): IO[Unit] = outputs.update(_ :+ s)

  /** Returns everything printed so far, oldest first. */
  val printed: IO[Vector[String]] = outputs.get

object ConsoleRef:
  /** Returns a console where the given lines will be typed. */
  def withInputs(lines: String*): IO[ConsoleRef] =
    for
      inputs <- Ref[IO].of(lines.toList)
      outputs <- Ref[IO].of(Vector.empty[String])
    yield ConsoleRef(inputs, outputs)
