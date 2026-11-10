package tictactoe.app

import tictactoe.domain.Position

/** What a player chose to do on their turn. */
enum Action:
  case Play(pos: Position)
  case Quit
