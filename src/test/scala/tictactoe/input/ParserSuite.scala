package tictactoe.input

import tictactoe.domain.Coord.{One, Three, Two}
import tictactoe.domain.Position

class ParserSuite extends munit.FunSuite:
  test("1 and 9 map to the top-left and bottom-right corners") {
    assertEquals(Parser.position("1"), Right(Position(One, One)))
    assertEquals(Parser.position("9"), Right(Position(Three, Three)))
  }

  test("numbers run left to right before top to bottom") {
    assertEquals(Parser.position("3"), Right(Position(One, Three)))
    assertEquals(Parser.position("7"), Right(Position(Three, One)))
  }

  test("surrounding whitespace is trimmed") {
    assertEquals(Parser.position(" 5 "), Right(Position(Two, Two)))
  }

  test("empty and blank input is Empty") {
    List("", "   ", "\t\n").foreach { raw =>
      assertEquals(Parser.position(raw), Left(InputError.Empty))
    }
  }

  test("text that is not a number is NotANumber, keeping the raw input") {
    List("abc", " abc ", "1.5", "5x").foreach { raw =>
      assertEquals(Parser.position(raw), Left(InputError.NotANumber(raw)))
    }
  }

  test("numbers outside 1 to 9 are OutOfRange") {
    List(0, 10, -1, Int.MaxValue, Int.MinValue).foreach { n =>
      assertEquals(Parser.position(n.toString), Left(InputError.OutOfRange(n)))
    }
  }

  test("all 9 inputs map to every position exactly once") {
    val parsed = (1 to 9).toList.map(n => Parser.position(n.toString))
    assertEquals(parsed.distinct.size, 9)
    assertEquals(parsed, Position.all.map(Right(_)))
  }
