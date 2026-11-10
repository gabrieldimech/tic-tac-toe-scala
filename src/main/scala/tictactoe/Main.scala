package tictactoe

import cats.Monad
import cats.effect.{ExitCode, IO, IOApp}
import tictactoe.app.{Console, GameLoop, MoveSource, SessionResult}
import tictactoe.domain.{Game, Player}
import tictactoe.interpreters.{ConsoleIO, HumanMoves, MinimaxMoves}

/** Who plays O. X is always a person. */
enum Opponent:
  case Human, Ai

/** Starts the program. Errors are deliberately not handled here: an unexpected
  * failure ends the process with a stack trace and a non-zero exit code.
  */
object Main extends IOApp:
  private val usage = "Usage: tictactoe [--vs-ai]"

  /** Returns the opponent the arguments ask for, or `None` if they are not
    * understood.
    */
  def parseArgs(args: List[String]): Option[Opponent] = args match
    case Nil             => Some(Opponent.Human)
    case List("--vs-ai") => Some(Opponent.Ai)
    case _               => None

  /** Returns a full session on `console`, with a person as X and `opponent` as
    * O. It is separate from `run` so that tests can run the same wiring with no
    * terminal.
    */
  def session[F[_]: Monad](
      console: Console[F],
      opponent: Opponent = Opponent.Human
  ): F[SessionResult] =
    val human = HumanMoves[F](console)
    val second: MoveSource[F] = opponent match
      case Opponent.Human => human
      case Opponent.Ai    => MinimaxMoves[F]
    val movesFor: Player => MoveSource[F] =
      case Player.X => human
      case Player.O => second
    GameLoop[F](console, movesFor).run(Game.initial)

  /** Runs a session on the real terminal. `GameLoop` has already shown the
    * outcome by the time it returns, so the result needs no more rendering. Bad
    * arguments are an expected failure, so they exit with a message and code 2.
    */
  def run(args: List[String]): IO[ExitCode] = parseArgs(args) match
    case Some(opponent) =>
      session[IO](ConsoleIO, opponent).as(ExitCode.Success)
    case None => cats.effect.std.Console[IO].errorln(usage).as(ExitCode(2))
