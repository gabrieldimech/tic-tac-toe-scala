package tictactoe.testkit

import cats.data.State
import tictactoe.app.Console

/** A [[Console]] with no I/O. Each operation is a function from one
  * [[TestWorld]] to the next, so a test runs by supplying the starting world.
  */
object ConsoleScripted extends Console[ConsoleScripted.Scripted]:
  /** `State[TestWorld, *]` under a name, because `*` needs a compiler flag. */
  type Scripted[A] = State[TestWorld, A]

  /** Removes and returns the next line, or `None` once none are left. */
  val readLine: Scripted[Option[String]] = State { world =>
    world.inputs match
      case Nil          => (world, None)
      case line :: rest => (world.copy(inputs = rest), Some(line))
  }

  /** Appends `s` to the outputs. */
  def print(s: String): Scripted[Unit] =
    State.modify(world => world.copy(outputs = world.outputs :+ s))
