package tictactoe.testkit

/** Everything a console touches, as plain data: the lines still to be typed and
  * everything printed so far, oldest first.
  */
final case class TestWorld(inputs: List[String], outputs: Vector[String])

object TestWorld:
  /** Returns a world where the given lines will be typed, with nothing printed.
    */
  def withInputs(inputs: String*): TestWorld =
    TestWorld(inputs.toList, Vector.empty)
