package tictactoe.domain

class PositionSuite extends munit.FunSuite:
  test("all holds 9 distinct cells") {
    assertEquals(Position.all.distinct.size, 9)
  }
