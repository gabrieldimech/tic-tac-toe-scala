package tictactoe.interpreters

import cats.Monad
import cats.syntax.all.*
import tictactoe.app.{Action, Console, MoveSource, Render}
import tictactoe.domain.Game
import tictactoe.input.Parser

/** A [[tictactoe.app.MoveSource]] that asks a person through a
  * [[tictactoe.app.Console]].
  *
  * The retry after bad input is recursion through `flatMap`, so it needs an `F`
  * whose `flatMap` is stack-safe. Cats `State` and `IO` are. A strict monad
  * such as `Id` is not, and enough bad input would overflow the stack.
  */
final class HumanMoves[F[_]: Monad](console: Console[F]) extends MoveSource[F]:
  /** Prompts until the player gives a cell, types `q` or closes the input. */
  def next(game: Game.InProgress): F[Action] =
    console
      .print(Render.prompt(game.next))
      .flatMap(_ => console.readLine)
      .flatMap(line => line.fold(Action.Quit.pure[F])(handle(game, _)))

  /** Quits on `q`, plays a valid cell, or explains the problem and asks again.
    */
  private def handle(game: Game.InProgress, raw: String): F[Action] =
    if raw.trim == "q" then Action.Quit.pure[F]
    else
      Parser.position(raw) match
        case Right(pos)  => Action.Play(pos).pure[F]
        case Left(error) =>
          console
            .print(Render.inputError(error) + "\n")
            .flatMap(_ => next(game))
