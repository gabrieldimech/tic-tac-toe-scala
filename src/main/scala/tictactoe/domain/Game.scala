package tictactoe.domain

/** The state of a game. Each case holds only what makes sense for it, so a
  * finished game has no next player and a drawn game has no winner.
  */
enum Game:
  case InProgress(board: Board, next: Player)
  case Won(board: Board, winner: Player, line: Line)
  case Drawn(board: Board)

object Game:
  /** Returns the start of every game: an empty board with X to move. */
  val initial: Game = InProgress(Board.empty, Player.X)

  /** Returns the game after the next player claims `pos`, or a [[MoveError]] if
    * the move is illegal. This is the one way to advance a game by position: it
    * works out whose turn it is, then leaves the rules to [[Decider.decide]]
    * and the state change to [[Decider.evolve]].
    */
  def play(game: Game, pos: Position): Either[MoveError, Game] = game match
    case InProgress(_, next) =>
      Decider
        .decide(game, Command.Place(next, pos))
        .map(_.foldLeft(game)(Decider.evolve))
    case Won(_, _, _) | Drawn(_) => Left(MoveError.GameAlreadyOver)
