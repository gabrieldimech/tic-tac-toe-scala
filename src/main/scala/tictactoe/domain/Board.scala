package tictactoe.domain

/** The cells claimed so far. Changing it requires going through
  * [[Board.place]].
  */
opaque type Board = Map[Position, Player]

object Board:
  /** Returns a board with no moves on it, the start of every game. */
  val empty: Board = Map.empty

  extension (board: Board)
    /** Returns who holds `pos`, or `None` if the cell is free. */
    def ownerOf(pos: Position): Option[Player] = board.get(pos)

    /** Returns whether no cell is free, which ends the game if nobody won. */
    def isFull: Boolean = emptyCells.isEmpty

    /** Returns the free cells in [[Position.all]] order: the legal moves. */
    def emptyCells: List[Position] = Position.all.filterNot(board.contains)

    /** Returns the board with `pos` claimed by `player`, or
      * [[MoveError.CellOccupied]] if the cell is taken, whoever holds it.
      */
    def place(pos: Position, player: Player): Either[MoveError, Board] =
      board.ownerOf(pos) match
        case Some(_) => Left(MoveError.CellOccupied(pos))
        case None    => Right(board.updated(pos, player))
