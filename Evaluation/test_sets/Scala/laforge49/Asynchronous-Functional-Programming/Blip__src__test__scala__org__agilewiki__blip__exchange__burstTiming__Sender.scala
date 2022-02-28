package org.agilewiki.blip
package exchange
package burstTiming

import annotation.tailrec
import messenger._
import java.util.concurrent.Semaphore

class Sender(c: Int, b: Int, threadManager: ThreadManager)
  extends Exchange(threadManager)
  with ExchangeMessengerActor {

  val done = new Semaphore(0)
  val echo = new Echo(threadManager)
  var count = 0
  var i = 0
  var burst = 0
  var j = 0
  var r = 0
  var t0 = 0L

  count = c
  i = c
  burst = b
  r = 0
  t0 = System.currentTimeMillis
  echo.sendReq(echo, new ExchangeRequest(this, processResponse), this)
  flushPendingMsgs

  def finished {
    done.acquire
  }

  override def exchangeMessenger = this

  override protected def processRequest {}

  private def dummy(rsp: Any) {
    processResponse(rsp)
  }

  @tailrec private def processResponse(rsp: Any) {
    if (r < 2 && i < 1) {
      val t1 = System.currentTimeMillis
      if (t1 != t0) println("msgs per sec = " +
        (count * burst * 2L * 1000L / (t1 - t0)))
      threadManager.close
      done.release
      return
    }
    if (r > 1 && j < 1) {
      r -= 1
      return
    }
    if (j < 1) {
      i -= 1
      j = burst
      r = burst
    } else r -= 1
    var async = true
    var sync = false
    var rsp: Any = null
    while (j > 0 && async) {
      j -= 1
      async = false
      echo.sendReq(echo, new ExchangeRequest(this, {
        msg => {
          rsp = msg
          if (async) {
            dummy(rsp)
          } else {
            sync = true
          }
        }
      }), this)
      if (!sync) {
        async = true
      }
    }
    if (async) return
    processResponse(rsp)
  }
}
