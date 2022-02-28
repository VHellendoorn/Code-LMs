/*
 * Copyright 2010 M.Naji
 *
 * This file is part of AgileWiki and is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License (LGPL) as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 * or navigate to the following url http://www.gnu.org/licenses/lgpl-2.1.txt
 *
 * Note however that only Scala, Java and JavaScript files are being covered by LGPL.
 * All other files are covered by the Common Public License (CPL).
 * A copy of this license is also included and can be
 * found as well at http://www.opensource.org/licenses/cpl1.0.txt
 */

package org.agilewiki
package util
package sequence
package composit

import java.util.TreeSet
import org.agilewiki.util.Timestamp
import org.specs.SpecificationWithJUnit
import basic.{NavigableSequence, EmptySequence}

class InvertedTimestampSequenceTest extends SpecificationWithJUnit {
  "Inverted Timestamp Sequence" should {

    "Pass the initialization test" in {
      new InvertedTimestampSequence(null) must throwA(
        new IllegalArgumentException(
          "requirement failed: The wrapped sequence cannot be null"
          )
        )

      var seq = new InvertedTimestampSequence(new EmptySequence)
      seq.current must beNull
    }

    var set = new TreeSet[String]
    for (i <- 0 to 10) set.add(Timestamp.timestamp)

    "Pass the Timestamp inverting test" in {
      var it1 = set.iterator
      var iSeq = new NavigableSequence(set)
      var seq = new InvertedTimestampSequence(iSeq)
      var pk = seq.current
      pk mustNot beNull
      while (pk != null) {
        pk must be equalTo Timestamp.invert(iSeq.current)
        pk = seq.next(pk)
      }

      var it = set.iterator
      while (it.hasNext) {
        var nxt = it.next
        seq current Timestamp.invert(nxt) mustNot beNull
        Timestamp.invert(seq.current) must be equalTo nxt
      }
    }

    "Pass the Timestamp reverse test" in {
      var seq = new InvertedTimestampSequence(new NavigableSequence(set))
      seq.isReverse must be equalTo true
      var pk = seq.current
      while (pk != null) {
        val nxt = seq next pk
        if (nxt != null)
          nxt < pk must be equalTo true
        pk = nxt
      }
      seq.current must beNull
      seq = new InvertedTimestampSequence(new NavigableSequence(set, true))
      seq.isReverse must be equalTo false
      pk = seq.current
      while (pk != null) {
        val nxt = seq next pk
        if (nxt != null)
          nxt > pk must be equalTo true
        pk = nxt
      }
      seq.current must beNull
    }

  }
}