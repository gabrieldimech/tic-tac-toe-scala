package tictactoe.app

import tictactoe.domain.{Board, Coord, Game, MoveError, Player, Position}
import tictactoe.input.InputError

/** Turns values into the text a player reads. Everything here is pure, so the
  * wording can be tested without any input or output.
  */
object Render:
  private val divider = "\n---+---+---\n"
  private val askForCell = "Enter a number from 1 to 9."

  /** Returns the number a player types for `pos`, the same one the parser
    * reads.
    */
  private def cellNumber(pos: Position): Int = Position.all.indexOf(pos) + 1

  /** Returns the board as a grid, showing each player's mark or, in a free
    * cell, the number that selects it. No trailing newline.
    */
  def grid(board: Board): String =
    Position.all
      .map(pos => board.ownerOf(pos).fold(cellNumber(pos).toString)(_.toString))
      .grouped(Coord.values.length)
      .map(_.mkString(" ", " | ", ""))
      .mkString(divider)

  /** Returns the text asking `player` for a move. */
  def prompt(player: Player): String =
    s"Player $player, choose a cell (1-9, q to quit): "

  /** Returns the message for a rejected move. The exhaustive match means a new
    * [[tictactoe.domain.MoveError]] case does not compile until it has a
    * message.
    */
  def moveError(error: MoveError): String = error match
    case MoveError.CellOccupied(pos) =>
      s"Cell ${cellNumber(pos)} is already taken."
    case MoveError.GameAlreadyOver     => "The game is already over."
    case MoveError.NotYourTurn(player) => s"It is not player $player's turn."

  /** Returns the message for unreadable input. The exhaustive match means a new
    * [[tictactoe.input.InputError]] case does not compile until it has a
    * message.
    */
  def inputError(error: InputError): String = error match
    case InputError.Empty           => s"Nothing entered. $askForCell"
    case InputError.NotANumber(raw) => s"'$raw' is not a cell. $askForCell"
    case InputError.OutOfRange(n)   => s"$n is not a cell. $askForCell"

  /** Returns the closing message of a session. */
  def outcome(result: SessionResult): String = result match
    case SessionResult.Abandoned(_)                      => "Game abandoned."
    case SessionResult.Completed(Game.Won(_, winner, _)) =>
      s"Player $winner wins!"
    case SessionResult.Completed(Game.Drawn(_)) => "It's a draw."
