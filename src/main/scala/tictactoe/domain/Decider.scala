package tictactoe.domain

/** The game's logic as two pure functions, following Jérémie Chassaing's
  * Decider pattern: `decide` judges a command, and `evolve` applies the facts.
  */
object Decider:
  /** Returns the events a command causes, or why it is rejected. Checks run in
    * a fixed order: game over, then wrong player, then occupied cell. This is
    * the only place that makes a judgement about a move.
    */
  def decide(game: Game, command: Command): Either[MoveError, List[Event]] =
    command match
      case Command.Place(player, pos) =>
        game match
          case Game.Won(_, _, _) | Game.Drawn(_) =>
            Left(MoveError.GameAlreadyOver)
          case Game.InProgress(_, next) if player != next =>
            Left(MoveError.NotYourTurn(player))
          case Game.InProgress(board, _) =>
            board
              .place(pos, player)
              .map(updated =>
                Event.MarkPlaced(player, pos) :: outcome(updated, player.other)
              )

  /** Returns the event that ends the game, if the board does. */
  private def outcome(board: Board, next: Player): List[Event] =
    Rules.status(board, next) match
      case Game.Won(_, winner, line) => List(Event.GameWon(winner, line))
      case Game.Drawn(_)             => List(Event.GameDrawn)
      case Game.InProgress(_, _)     => Nil

  /** Returns the game after one event. It is total: it has no way to fail,
    * because an event records something that already happened.
    *
    * Invariant: it is only given events produced by [[decide]], in order. Under
    * that invariant the tolerant branches are never reached. An event that
    * cannot apply, such as a mark on a taken cell or anything after the game
    * has ended, leaves the game unchanged.
    */
  def evolve(game: Game, event: Event): Game = game match
    case Game.InProgress(board, _) =>
      event match
        case Event.MarkPlaced(player, pos) =>
          board
            .place(pos, player)
            .fold(_ => game, Game.InProgress(_, player.other))
        case Event.GameWon(player, line) => Game.Won(board, player, line)
        case Event.GameDrawn             => Game.Drawn(board)
    case Game.Won(_, _, _) | Game.Drawn(_) => game

  /** Returns the game that a full history of events describes. */
  def replay(events: List[Event]): Game =
    events.foldLeft(Game.initial)(evolve)
