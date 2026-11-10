package tictactoe.app

import tictactoe.domain.Game

/** How a session ended: played to the end, or left unfinished. `Completed` only
  * accepts a finished game, so a completed session that is still in progress
  * cannot be built.
  */
enum SessionResult:
  case Completed(game: Game.Won | Game.Drawn)
  case Abandoned(game: Game)
