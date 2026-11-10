# Design notes

The decisions behind this tic-tac-toe game, why they were made, and what was
rejected. Each public function's own Scaladoc says what it does.

## Contents

1. [Overview](#1-overview)
2. [Types that rule out bad states](#2-types-that-rule-out-bad-states)
3. [Decide and evolve](#3-decide-and-evolve)
4. [Expected failures and defects](#4-expected-failures-and-defects)
5. [Effects at the edge](#5-effects-at-the-edge)
6. [The AI player](#6-the-ai-player)
7. [Testing](#7-testing)
8. [Known limits](#8-known-limits)

## 1. Overview

```
  Main                picks IO and the interpreters
   │
   ▼
  interpreters        ConsoleIO · HumanMoves · MinimaxMoves
   │
   ▼
  app                 GameLoop · Console[F] · MoveSource[F] · Render
   │
   ▼
  input               Parser · InputError
   │
   ▼
  domain              Board · Game · Decider · Rules · Minimax
                      pure, no imports

  ▼ = depends on. Nothing points outwards.

  One turn:

  MoveSource.next ─Action─▶ Game.play ─▶ decide ─events─▶ evolve ─▶ next Game
                                            │
                                        MoveError ─▶ Render ─▶ same player again
```

Only `Main` and `ConsoleIO` import `cats.effect`. Everything else is pure or
written against an abstract `F[_]`.

## 2. Types that rule out bad states

- **`Coord` is an enum of three values**, so an off-board `Position` cannot be
  written and no bounds check or `OutOfBounds` error exists. Rejected: `Int` with
  validation, which moves the check to every caller.
- **`Board` is an opaque type over `Map[Position, Player]`.** Outside its file only
  `empty`, `ownerOf`, `emptyCells`, `isFull` and `place` exist, so nothing can
  overwrite a cell. It costs nothing at runtime. Rejected: a type alias exposes
  `Map`; a 2D `Vector` brings back indexes.
- **`Game` has three cases**, `InProgress(board, next)`, `Won(board, winner, line)`
  and `Drawn(board)`. A finished game has no next player and a draw has no winner.
  Rejected: one class with `Option` fields and an `isOver` flag.
- **`SessionResult.Completed` takes `Game.Won | Game.Drawn`**, so a session cannot
  be "completed" while the game is still in progress, and `Render` needs no message
  for that case.
- **`Rules.lines` is derived from `Coord`**, not typed out, so no cell can be
  mistyped.

## 3. Decide and evolve

The core follows Jérémie Chassaing's Decider pattern:

| | `decide` | `evolve` |
|---|---|---|
| Input | A `Command`, which may be refused | An `Event`, which already happened |
| Result | `Either[MoveError, List[Event]]` | `Game` |
| Can fail | Yes | No |

`decide` checks, in order: game over, wrong player, occupied cell. A legal move
emits `MarkPlaced`, then `GameWon` or `GameDrawn` if it ends the game.
`Game.play(game, pos)` is the one entry point: it fills in the player to move,
calls `decide`, and folds the events through `evolve`.

**Why `evolve` cannot fail:** an event is a fact, and a replay that throws on a
stored event would fail on every retry. Given events out of order, it leaves the
game unchanged, so a duplicate `MarkPlaced` is harmless. The cost is that a corrupt
log is ignored silently. In production those events would be counted and routed
aside.

**Why it suits stream processing:** keyed by game, `Game` is the per-key state, a
command is an incoming element, events are the output stream, and `MoveError` is
a side output. Both functions are pure, so they test without a cluster and run
unchanged inside an operator such as Flink's `KeyedProcessFunction`.

## 4. Expected failures and defects

| | Expected failure | Defect |
|---|---|---|
| Examples | Occupied cell, `abc`, Ctrl-D, `q`, bad argument | A bug, broken stdin |
| Form | `MoveError`, `InputError`, `None`, `Quit` | A failed `IO` |
| Result | Game continues or ends, exit 0 (2 for bad arguments) | Stack trace, exit 1 |

- **Out-of-range input is an `InputError`**, not a `MoveError`: once parsed, no
  `Position` can be out of range ("parse, don't validate").
- **EOF is `Option`**, not an exception or a `null`, so the loop must handle it and
  it stays apart from real I/O errors. `ConsoleIO` catches only `EOFException`.
- **Defects are not caught.** Nothing can recover from what it did not predict, and
  a stack trace with a non-zero exit is the honest result.

## 5. Effects at the edge

The loop is written against two algebras (Tagless Final):

- **`Console[F]`:** how text gets in and out.
- **`MoveSource[F]`:** who chooses the next move. It takes `Game.InProgress`, so a
  finished game cannot be asked.

They change for different reasons. Parsing and rendering are pure, so they need no
algebra. `GameLoop` asks only for `Monad`, so it cannot sleep, fork or read a clock.
It runs in `IO` in production and in pure `State` in tests.

**Rejected:** hard-coding `IO`, which needs a real terminal to test; one combined
algebra, which would make the AI fake a terminal.

**Stack safety:** retries recurse through `flatMap`, which is safe for `State` and
`IO` (200,000 bad inputs are tested) but not for a strict monad such as `Id`.
`tailRecM` would fix that, at a cost in readability.

## 6. The AI player

`Minimax` searches every continuation through `Game.play`, so it cannot make an
illegal move and there is one copy of the rules. A move scores the free cells left
plus one, positive for a win and negative for a loss, so faster wins rank higher.
Ties go to the earliest cell, so play is repeatable. It returns `None` if no cell
is free, and `MinimaxMoves` turns that into `Quit`.

Adding the AI changed no line of `GameLoop`, which supports the two-algebra design.
There is no memoisation or pruning, because a reply as O takes about 180ms.

## 7. Testing

- **Examples** for every winning line, each error and its priority, and each message.
- **Properties** over random legal games: turns alternate, every free cell is
  playable, and `play` matches a reference built without events. Shrinking is off,
  because deleting moves turns a game into an illegal one.
- **Exhaustive:** the AI never loses as X or as O against every possible reply.
- **Whole program:** `IO` and `State` give identical transcripts for the same input.
- **Checking the tests:** after each step, bugs were introduced by hand, and the
  suite caught them.

## 8. Known limits

- `Game`'s constructors are public, so a `Game` that `play` could never produce can
  still be built. `play` is the intended way in.
- `Main.run` with valid arguments and real stdin are checked by hand.
