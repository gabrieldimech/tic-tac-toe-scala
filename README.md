# Tic-Tac-Toe in Scala

A console tic-tac-toe game in Scala 3, written in a functional style. The rules are
pure, effects stay at the edge of the program, and expected failures are values.
You can play against another person or an unbeatable computer opponent.

## Run it

You need a JDK (developed and tested on 21) and [sbt](https://www.scala-sbt.org/). The project pins its own sbt
and Scala versions. Run the game from a terminal, because it reads moves from the
keyboard. IDE consoles such as IntelliJ's sbt shell may not pass typed input through.

To play another person:

```bash
sbt run
```

To play the computer (you are X, the computer is O):

```bash
sbt "run --vs-ai"
```

Type a number from 1 to 9 to play that cell, or `q` to quit. Ctrl-D also quits.

```
 1 | 2 | 3
---+---+---
 4 | 5 | 6
---+---+---
 7 | 8 | 9
```

The quotes around `run --vs-ai` matter, because they pass the argument to the game
and not to sbt. An unknown argument prints a usage message and exits with code 2.

## Test it

Run all tests, one suite, or the full check (formatting, then all tests):

```bash
sbt test
sbt "testOnly tictactoe.domain.MinimaxSuite"
sbt scalafmtSbtCheck scalafmtCheckAll test
```

Tests combine examples, ScalaCheck properties over random legal games, and exhaustive
checks. The computer player is tested against every possible sequence of replies, as
X and as O. No test needs a terminal: the game loop runs in pure `State`, and one
suite runs the same wiring in `IO`.

## Architecture

There are four layers, in `src/main/scala/tictactoe`:

| Layer | Holds | May depend on |
|---|---|---|
| `domain` | The rules: `Board`, `Game`, `Decider`, `Rules`, `Minimax` | Nothing |
| `input` | Parsing typed text into domain values | `domain` |
| `app` | The game loop, the algebras and pure rendering | `domain`, `input` |
| `interpreters` | Implementations of the algebras | `app`, `domain`, `input` |

**The dependency rule:** dependencies point inwards, so `domain` knows nothing about
the layers around it. It has no imports at all, not even from Cats. `Main` sits
outside the layers and wires them together. Only `Main` and `ConsoleIO` import
`cats.effect`.

## Design decisions

### Tagless Final with two algebras

The program is written against two interfaces over an abstract effect `F[_]`:
`Console[F]` for how text moves, and `MoveSource[F]` for who chooses a move. They
change for different reasons, and parsing and rendering are pure, so nothing else
needs one.

**Why not concrete `IO`:** code tied to `IO` needs a real or redirected terminal to
test. Here the same loop runs in `IO` in production and in pure `State` in tests, so a
test can compare a whole transcript and always repeats exactly. The loop only asks for
`Monad`, so its signature shows it can do nothing but sequence steps. The computer
player was added later without changing the loop at all.

### Decide and evolve

The core uses the Decider pattern, a known functional event-sourcing pattern
described by Jérémie Chassaing. `decide` judges a command and returns events, or an
error. `evolve` applies one event, and cannot fail, because an event records something
that has already happened.

The same shape fits a keyed stream processor such as Apache Flink, with one key
per game:

- Each command is an incoming element, and `Game` is the state kept for its key.
- `decide` judges the element, and its events become the output stream.
- `evolve` updates the state for that key, and a fold over the event log rebuilds it.
- Both are pure, so the logic can be tested with no cluster.
- Consumers read what happened, with no copy of the rules.

[docs/DESIGN.md](docs/DESIGN.md#3-decide-and-evolve) covers this in more detail.

### Errors as values, defects as crashes

An expected failure, such as an occupied cell or unreadable input, is a value:
`MoveError` or `InputError` inside an `Either`. The type shows what can go wrong, and
the caller has to handle it.

A defect is anything nobody planned for, and it is deliberately not caught. Everything
expected is already a value, so anything that reaches `IOApp` is unexpected, and a stack
trace with a non-zero exit code is the honest result.

### EOF as `Option`

`Console.readLine` returns `F[Option[String]]`, where `None` means input has closed.
Ctrl-D or the end of a piped file is a normal way to stop, not a failure, so it
shouldn't be an exception. The type makes the loop handle it, and it stays separate
from real I/O errors. `ConsoleIO` catches only `EOFException`, so any other failure
still crashes.

## Possible extensions

- **Move timeouts:** a `MoveSource` wrapper using `Temporal`, tested with `TestControl`.
- **Event publishing:** a `GameEvents[F]` algebra with a Kafka interpreter.
- **Save and resume:** a `GameStore[F]` algebra that stores a game's events.
- **N x N boards:** no new algebra; `Coord` and `Line` would change, because both are
  fixed at three today.

## More detail

[docs/DESIGN.md](docs/DESIGN.md) records the main design decisions, why they were
made, and the alternatives that were rejected.
