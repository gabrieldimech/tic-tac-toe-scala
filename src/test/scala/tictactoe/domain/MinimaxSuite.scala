package tictactoe.domain

import tictactoe.domain.BoardFixtures.boardOf
import tictactoe.domain.Coord.{One, Three, Two}
import tictactoe.domain.Player.{O, X}

class MinimaxSuite extends munit.FunSuite:

  private def cell(row: Coord, col: Coord) = Position(row, col)

  private def after(game: Game, pos: Position): Game =
    Game
      .play(game, pos)
      .fold(error => fail(s"illegal move $pos: $error"), identity)

  /** The AI's move in a live game, failing the test if it has none. */
  private def best(game: Game.InProgress): Position =
    Minimax.bestMove(game).getOrElse(fail(s"no move for $game"))

  /** Plays every legal move by `next` and every move the AI chooses. */
  private def endings(game: Game, ai: Player): List[Game] = game match
    case live @ Game.InProgress(board, next) =>
      val moves =
        if next == ai then List(best(live)) else board.emptyCells
      moves.flatMap(pos => endings(after(live, pos), ai))
    case Game.Won(_, _, _) | Game.Drawn(_) => List(game)

  /** Returns every game reachable from `game`, whoever moves. */
  private def reachable(game: Game): List[Game.InProgress] = game match
    case live @ Game.InProgress(board, _) =>
      live :: board.emptyCells.flatMap(pos => reachable(after(live, pos)))
    case Game.Won(_, _, _) | Game.Drawn(_) => Nil

  private def selfPlay(game: Game): Game = game match
    case live @ Game.InProgress(_, _) =>
      selfPlay(after(live, best(live)))
    case Game.Won(_, _, _) | Game.Drawn(_) => game

  private def isWin(game: Game): Boolean = game match
    case Game.Won(_, _, _)                     => true
    case Game.Drawn(_) | Game.InProgress(_, _) => false

  private def wonBy(game: Game, player: Player): Boolean = game match
    case Game.Won(_, winner, _)                => winner == player
    case Game.Drawn(_) | Game.InProgress(_, _) => false

  test("takes an immediate win") {
    val board = boardOf(
      List(
        (cell(One, One), X),
        (cell(Two, One), O),
        (cell(One, Two), X),
        (cell(Two, Two), O)
      )
    )
    val game: Game.InProgress = Game.InProgress(board, X)
    assertEquals(Minimax.bestMove(game), Some(cell(One, Three)))
  }

  test("blocks an immediate loss") {
    val board = boardOf(
      List(
        (cell(One, One), X),
        (cell(Two, Two), O),
        (cell(One, Two), X)
      )
    )
    val game: Game.InProgress = Game.InProgress(board, O)
    assertEquals(Minimax.bestMove(game), Some(cell(One, Three)))
  }

  test("takes a win over a block, even when the block comes first") {
    val board = boardOf(
      List(
        (cell(One, One), O),
        (cell(Two, One), X),
        (cell(One, Two), O),
        (cell(Two, Two), X)
      )
    )
    val game: Game.InProgress = Game.InProgress(board, X)
    assertEquals(Minimax.bestMove(game), Some(cell(Two, Three)))
  }

  test("in every reachable game, an available win is taken at once") {
    val wins = reachable(Game.initial).filter(game =>
      game.board.emptyCells.exists(pos => isWin(after(game, pos)))
    )
    assert(wins.nonEmpty)
    wins.foreach { game =>
      assert(isWin(after(game, best(game))), clue(game))
    }
  }

  test("ties go to the earliest cell") {
    val game: Game.InProgress = Game.InProgress(Board.empty, X)
    assertEquals(Minimax.bestMove(game), Some(cell(One, One)))
  }

  test("a game with no free cell has no best move") {
    val full = boardOf(Position.all.map((_, X)))
    assertEquals(Minimax.bestMove(Game.InProgress(full, O)), None)
  }

  test("AI vs AI always draws, from the start and from any first move") {
    val openings = Position.all.map(after(Game.initial, _))
    (Game.initial :: openings).foreach { game =>
      assert(
        selfPlay(game) match
          case Game.Drawn(_)                             => true
          case Game.Won(_, _, _) | Game.InProgress(_, _) => false
      )
    }
  }

  test("the AI never loses as X, against every possible reply") {
    val games = endings(Game.initial, X)
    assert(games.exists(wonBy(_, X)))
    assertEquals(games.filter(wonBy(_, O)), Nil)
  }

  test("the AI never loses as O, against every possible reply") {
    val games = endings(Game.initial, O)
    assert(games.exists(wonBy(_, O)))
    assertEquals(games.filter(wonBy(_, X)), Nil)
  }
