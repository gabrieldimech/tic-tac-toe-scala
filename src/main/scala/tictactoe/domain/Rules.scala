package tictactoe.domain

/** The rules that decide how a game ends. */
object Rules:
  private val coords: List[Coord] = Coord.values.toList

  /** Builds a line from a function, so it always has exactly three cells. */
  private def lineThrough(cell: Coord => Position): Line =
    Line(cell(Coord.One), cell(Coord.Two), cell(Coord.Three))

  /** Maps a coordinate to its opposite, giving the second diagonal. */
  private def mirror(coord: Coord): Coord = coord match
    case Coord.One   => Coord.Three
    case Coord.Two   => Coord.Two
    case Coord.Three => Coord.One

  /** Returns the 8 winning lines: rows, then columns, then the two diagonals.
    * Deriving them from [[Coord]] leaves no cell to mistype.
    */
  val lines: List[Line] =
    coords.map(row => lineThrough(Position(row, _))) ++
      coords.map(col => lineThrough(Position(_, col))) ++
      List(
        lineThrough(coord => Position(coord, coord)),
        lineThrough(coord => Position(coord, mirror(coord)))
      )

  /** Returns the player holding a complete line, with that line, or `None`.
    * Checks [[lines]] in order and stops at the first match.
    */
  def winner(board: Board): Option[(Player, Line)] =
    lines
      .map(line =>
        (
          board.ownerOf(line.a),
          board.ownerOf(line.b),
          board.ownerOf(line.c),
          line
        )
      )
      .collectFirst {
        case (Some(first), Some(second), Some(third), line)
            if first == second && second == third =>
          (first, line)
      }

  /** Returns the [[Game]] that `board` represents, with `next` to move if it is
    * still in progress. A win is checked first because the last move can both
    * complete a line and fill the board.
    */
  def status(board: Board, next: Player): Game =
    (winner(board), board.isFull) match
      case (Some((player, line)), _) => Game.Won(board, player, line)
      case (None, true)              => Game.Drawn(board)
      case (None, false)             => Game.InProgress(board, next)
