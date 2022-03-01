// Copyright (c) 2018 Norwegian University of Science and Technology (NTNU)
// Copyright (c) 2019 Xilinx
//
// BSD v3 License
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// * Redistributions of source code must retain the above copyright notice, this
//   list of conditions and the following disclaimer.
//
// * Redistributions in binary form must reproduce the above copyright notice,
//   this list of conditions and the following disclaimer in the documentation
//   and/or other materials provided with the distribution.
//
// * Neither the name of BISMO nor the names of its
//   contributors may be used to endorse or promote products derived from
//   this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
// CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

import Chisel._
import bismo._
import org.scalatest.junit.JUnitSuite
import org.junit.Test
import BISMOTestHelpers._

// Tester-derived class to give stimulus and observe the outputs for the
// Module to be tested
class MultiSeqGenTester(c: MultiSeqGen) extends Tester(c) {
  val r = scala.util.Random

  poke(c.io.in.bits.init, 0)
  poke(c.io.in.bits.count, 10)
  poke(c.io.in.bits.step, 1)
  poke(c.io.in.valid, 1)
  step(1)
  poke(c.io.in.valid, 0)

  var ni: Int = 0
  for (i ← 0 until 30) {
    poke(c.io.out.ready, r.nextInt(2))
    if ((peek(c.io.out.valid) & peek(c.io.out.ready)) == 1) {
      expect(c.io.out.bits, ni)
      ni += 1
    }
    step(1)
  }

}

class TestMultiSeqGen extends JUnitSuite {
  @Test def MultiSeqGenModuleTest {
    // Chisel arguments to pass to chiselMainTest
    def testArgs = BISMOTestHelpers.stdArgs
    // function that instantiates the Module to be tested
    def testModuleInstFxn = () ⇒ {
      Module(new MultiSeqGen(
        new MultiSeqGenParams(64, 10)
      ))
    }
    // function that instantiates the Tester to test the Module
    def testTesterInstFxn = (c: MultiSeqGen) ⇒ new MultiSeqGenTester(c)

    // actually run the test
    chiselMainTest(
      testArgs,
      testModuleInstFxn
    ) {
        testTesterInstFxn
      }
  }
}
