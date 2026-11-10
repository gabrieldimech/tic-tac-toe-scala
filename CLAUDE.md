# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

- `sbt run`, or `sbt "run --vs-ai"` to play the computer.
- `sbt test`; one suite with `sbt "testOnly tictactoe.domain.MinimaxSuite"`.
- `sbt scalafmtSbtCheck scalafmtCheckAll test` is the full check: formatting, then all tests.

## Architecture

Four layers under `src/main/scala/tictactoe`, with dependencies pointing inwards:
`domain` (pure rules, no imports) ← `input` (parsing) ← `app` (game loop,
the `Console[F]` and `MoveSource[F]` algebras, rendering) ← `interpreters`.
`Main` wires them together. See `README.md` and `docs/DESIGN.md`.

# Tic-Tac-Toe in Scala — working rules

This is a take-home assignment for a Data Engineer (Real-Time) role.
Reviewers judge FP discipline, error modelling, test quality and git history.

## Style
- Scala 3 syntax. Immutable data only: no var, no mutable collections.
- Closed types as `enum` or `sealed trait`; data as `case class`.
- Exhaustive `match`; never a wildcard `case _` that hides a new case.
- Recursion and higher-order functions where they fit; no while loops.
- No `throw`, `null`, `.get`, `.head`, `asInstanceOf`, or `Try` that discards errors.
- Expected failures are values (`Either`, ADTs). Only defects may crash.
- Pure domain: no effects, no Cats Effect imports under `domain/`.
- Concise and readable over clever. No feature without a test that needs it.

## Explanations (assignment requirement)
- Every public function gets a short Scaladoc: what it returns, why it
  exists, how it works if non-obvious. No comment-per-line.
- Append a section to `docs/DESIGN.md` for each step: one entry per new
  function/type with What / Why / How, plus alternatives rejected.

## Workflow
- Do exactly one step per prompt. Do not start the next step.
- Write tests alongside code. Run `sbt scalafmtAll test` before finishing.
- When done: summarise the change, show the DESIGN.md entry, propose a
  Conventional Commit message (subject + body explaining why).
- Do NOT commit until I reply `commit`.
- Verify library versions and compiler flags against current docs;
  say which you checked.
