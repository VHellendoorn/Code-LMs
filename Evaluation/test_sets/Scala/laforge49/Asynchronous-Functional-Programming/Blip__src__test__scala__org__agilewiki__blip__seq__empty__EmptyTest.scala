package org.agilewiki.blip
package seq
package empty

import org.specs.SpecificationWithJUnit
import bind._

class EmptyTest extends SpecificationWithJUnit {
  "EmptySeq" should {
    "support first, current and next" in {
      val emptySeq = new EmptySeq
      println(Future(emptySeq, First()))
      println(Future(emptySeq, Current(3)))
      println(Future(emptySeq, Next("abc")))
    }
  }
}
