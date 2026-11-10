package tictactoe.domain

import tictactoe.domain.BoardFixtures.boardOf
import tictactoe.domain.Coord.{One, Three, Two}
import tictactoe.domain.Event.{GameDrawn, GameWon, MarkPlaced}
import tictactoe.domain.Player.{O, X}

class DeciderSuite extends munit.FunSuite:
  private val topLeft = Position(One, One)
  private val topMiddle = Position(One, Two)
  private val topRight = Position(One, Three)
  private val centre = Position(Two, Two)

  private val topRow = Line(topLeft, topMiddle, topRight)

  private val xToWinOnTopRow = boardOf(
    List(
      (topLeft, X),
      (Position(Two, One), O),
      (topMiddle, X),
      (centre, O)
    )
  )

  /** X O X X O O O X _
    */
  private val xToDrawOnLastCell = boardOf(
    List(
      (topLeft, X),
      (topMiddle, O),
      (topRight, X),
      (centre, O),
      (Position(Two, One), X),
      (Position(Two, Three), O),
      (Position(Three, Two), X),
      (Position(Three, One), O)
    )
  )

  /** X X _ O O X X O O
    */
  private val xToWinOnLastCell = boardOf(
    List(
      (topLeft, X),
      (Position(Two, One), O),
      (topMiddle, X),
      (centre, O),
      (Position(Two, Three), X),
      (Position(Three, Two), O),
      (Position(Three, One), X),
      (Position(Three, Three), O)
    )
  )

  private val won = Game.Won(xToWinOnTopRow, X, topRow)
  private val drawn = Game.Drawn(xToDrawOnLastCell)

  test("a legal move that ends nothing emits only MarkPlaced") {
    assertEquals(
      Decider.decide(Game.initial, Command.Place(X, centre)),
      Right(List(MarkPlaced(X, centre)))
    )
  }

  test("the wrong player is rejected with NotYourTurn") {
    assertEquals(
      Decider.decide(Game.initial, Command.Place(O, centre)),
      Left(MoveError.NotYourTurn(O))
    )
  }

  test("NotYourTurn takes priority over an occupied cell") {
    val game = Game.InProgress(boardOf(List((centre, X))), O)
    assertEquals(
      Decider.decide(game, Command.Place(X, centre)),
      Left(MoveError.NotYourTurn(X))
    )
  }

  test("an occupied cell is rejected for the right player") {
    val game = Game.InProgress(boardOf(List((centre, X))), O)
    assertEquals(
      Decider.decide(game, Command.Place(O, centre)),
      Left(MoveError.CellOccupied(centre))
    )
  }

  test("GameAlreadyOver takes priority, for either player") {
    for
      game <- List[Game](won, drawn)
      player <- Player.values.toList
    do
      assertEquals(
        Decider.decide(game, Command.Place(player, topLeft)),
        Left(MoveError.GameAlreadyOver)
      )
  }

  test("a winning move emits exactly MarkPlaced then GameWon") {
    val game = Game.InProgress(xToWinOnTopRow, X)
    assertEquals(
      Decider.decide(game, Command.Place(X, topRight)),
      Right(List(MarkPlaced(X, topRight), GameWon(X, topRow)))
    )
  }

  test("a drawing move emits exactly MarkPlaced then GameDrawn") {
    val game = Game.InProgress(xToDrawOnLastCell, X)
    val last = Position(Three, Three)
    assertEquals(
      Decider.decide(game, Command.Place(X, last)),
      Right(List(MarkPlaced(X, last), GameDrawn))
    )
  }

  test("a win on the last cell emits GameWon and not GameDrawn") {
    val game = Game.InProgress(xToWinOnLastCell, X)
    assertEquals(
      Decider.decide(game, Command.Place(X, topRight)),
      Right(List(MarkPlaced(X, topRight), GameWon(X, topRow)))
    )
  }

  test("evolve applies each event") {
    assertEquals(
      Decider.evolve(Game.initial, MarkPlaced(X, centre)),
      Game.InProgress(boardOf(List((centre, X))), O)
    )
    val game = Game.InProgress(xToWinOnTopRow, X)
    assertEquals(Decider.evolve(game, GameWon(X, topRow)), won)
    assertEquals(
      Decider.evolve(Game.InProgress(xToDrawOnLastCell, O), GameDrawn),
      drawn
    )
  }

  test("evolve leaves the game unchanged for a mark on a taken cell") {
    val game = Game.InProgress(boardOf(List((centre, X))), O)
    Player.values.foreach { player =>
      assertEquals(Decider.evolve(game, MarkPlaced(player, centre)), game)
    }
  }

  test("evolve leaves a finished game unchanged for every event") {
    for
      game <- List[Game](won, drawn)
      event <- List(MarkPlaced(O, topLeft), GameWon(O, topRow), GameDrawn)
    do assertEquals(Decider.evolve(game, event), game)
  }

  test("replay of no events is the initial game") {
    assertEquals(Decider.replay(Nil), Game.initial)
  }
