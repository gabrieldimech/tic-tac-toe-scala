package tictactoe.domain

class PlayerSuite extends munit.FunSuite:
  test("other is an involution") {
    Player.values.foreach(player => assertEquals(player.other.other, player))
  }

  test("other is never the same player") {
    Player.values.foreach(player => assertNotEquals(player.other, player))
  }
