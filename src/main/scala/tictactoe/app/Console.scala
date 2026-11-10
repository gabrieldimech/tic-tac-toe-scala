package tictactoe.app

/** Text input and output, in an effect `F` chosen by the caller. */
trait Console[F[_]]:
  /** Returns the next line, or `None` once input is closed (EOF). */
  def readLine: F[Option[String]]

  /** Writes `s` as it is, with no newline, so a prompt can stay on its line. */
  def print(s: String): F[Unit]
