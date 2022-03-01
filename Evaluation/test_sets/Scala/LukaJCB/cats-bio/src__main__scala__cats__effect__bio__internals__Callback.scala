/*
 * Copyright (c) 2017-2018 The Typelevel Cats-effect Project Developers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cats.effect.bio.internals

import java.util.concurrent.atomic.AtomicBoolean

import cats.effect.bio.internals.IORunLoop.CustomException
import cats.effect.internals.TrampolineEC.immediate

import scala.concurrent.Promise
import scala.util.Left

/**
 * Internal API — utilities for working with `IO.async` callbacks.
 */
private[effect] object Callback {
  type Type[-E, -A] = Either[E, A] => Unit

  /**
   * Builds a callback reference that throws any received
   * error immediately.
   */
  def report[E, A]: Type[E, A] =
    reportRef.asInstanceOf[Type[E, A]]

  private def reportRef[E] = (r: Either[E, _]) =>
    r match {
      case Left(e) => Logger.reportFailure(new CustomException(e))
      case _ => ()
    }

  /** Reusable `Right(())` reference. */
  final val rightUnit = Right(())

  /** Reusable no-op, side-effectful `Function1` reference. */
  final val dummy1: Any => Unit = _ => ()

  /** Builds a callback with async execution. */
  def async[E, A](cb: Type[E, A]): Type[E, A] =
    async(null, cb)

  /**
   * Builds a callback with async execution.
   *
   * Also pops the `Connection` just before triggering
   * the underlying callback.
   */
  def async[E, A](conn: IOConnection, cb: Type[E, A]): Type[E, A] =
    value => immediate.execute(
      new Runnable {
        def run(): Unit = {
          if (conn ne null) conn.pop()
          cb(value)
        }
      })

  /**
   * Callback wrapper used in `IO.async` that:
   *
   *  - guarantees (thread safe) idempotency
   *  - triggers light (trampolined) async boundary for stack safety
   *  - pops the given `Connection` (only if != null)
   *  - logs extraneous errors after callback was already called once
   */
  def asyncIdempotent[E, A](conn: IOConnection, cb: Type[E, A]): Type[E, A] =
    new AsyncIdempotentCallback[E, A](conn, cb)

  /**
   * Builds a callback from a standard Scala `Promise`.
   */
  def promise[E, A](p: Promise[A]): Type[E, A] = {
    case Right(a) => p.success(a)
    case Left(e) => p.failure(new CustomException(e))
  }

  /** Helpers async callbacks. */
  implicit final class Extensions[-E, -A](val self: Type[E, A]) extends AnyVal {
    /**
     * Executes the source callback with a light (trampolined) async
     * boundary, meant to protect against stack overflows.
     */
    def async(value: Either[E, A]): Unit =
      async(null, value)

    /**
     * Executes the source callback with a light (trampolined) async
     * boundary, meant to protect against stack overflows.
     *
     * Also pops the given `Connection` before calling the callback.
     */
    def async(conn: IOConnection, value: Either[E, A]): Unit =
      immediate.execute(new Runnable {
        def run(): Unit = {
          if (conn ne null) conn.pop()
          self(value)
        }
      })

  }

  private final class AsyncIdempotentCallback[-E, -A](
    conn: IOConnection,
    cb: Either[E, A] => Unit)
    extends (Either[E, A] => Unit) {

    private[this] val canCall = new AtomicBoolean(true)

    def apply(value: Either[E, A]): Unit = {
      if (canCall.getAndSet(false)) {
        immediate.execute(new Runnable {
          def run(): Unit = {
            if (conn ne null) conn.pop()
            cb(value)
          }
        })
      } else value match {
        case Right(_) => ()
        case Left(e) =>
          Logger.reportFailure(new CustomException(e))
      }
    }
  }
}
