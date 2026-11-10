package tictactoe.testkit

import cats.syntax.all.*

class ConsoleScriptedSuite extends munit.FunSuite:
  test("readLine takes lines in order, then None") {
    val read = ConsoleScripted.readLine
    val (world, lines) = (read, read, read).tupled
      .run(TestWorld.withInputs("a", "b"))
      .value
    assertEquals(lines, (Some("a"), Some("b"), None))
    assertEquals(world.inputs, Nil)
  }

  test("print appends outputs in order") {
    val (world, _) =
      (ConsoleScripted.print("a") *> ConsoleScripted.print("b"))
        .run(TestWorld.withInputs())
        .value
    assertEquals(world.outputs, Vector("a", "b"))
  }

  test("a program is a value: nothing happens until it is run") {
    val world = TestWorld.withInputs("a")
    val program = ConsoleScripted.readLine
    assertEquals(program.run(world).value, program.run(world).value)
  }
