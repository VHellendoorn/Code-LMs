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

package bismo

import Chisel._
import fpgatidbits.synthutils.PrintableParam

// a popcount module with configurable pipelining
// note: retiming must be enabled for pipelining to work as intended
// e.g. the following in Xilinx Vivado:
//set_property STEPS.SYNTH_DESIGN.ARGS.RETIMING true [get_runs synth_1]

class PopCountUnitParams(
  val numInputBits: Int, // popcount bits per cycle
  val extraPipelineRegs: Int = 0 // extra I/O registers for retiming
) extends PrintableParam {
  def headersAsList(): List[String] = {
    return List("PopCWidth", "PopCLatency")
  }

  def contentAsList(): List[String] = {
    return List(numInputBits, getLatency()).map(_.toString)
  }

  def getNumCompressors(): Int = {
    return (1 << log2Ceil(math.ceil(numInputBits / 36.0).toInt))
  }

  def getPadBits(): Int = {
    return getNumCompressors() * 36 - numInputBits
  }

  def padInput(in: Bits): Bits = {
    if (getPadBits() == 0) { return in }
    else { return Cat(Bits(0, width = getPadBits()), in) }
  }

  // levels of I/O regs inserted by default
  val defaultInputRegs: Int = 1
  val defaultOutputRegs: Int = 1
  val adderTreeRegs: Int = log2Ceil(getNumCompressors())
  def getLatency(): Int = {
    return defaultInputRegs + defaultOutputRegs + adderTreeRegs + extraPipelineRegs
  }
}

// optimized 36-to-6 popcount as described on Jan Gray's blog:
// http://fpga.org/2014/09/05/quick-fpga-hacks-population-count/
class PopCount6to3() extends Module {
  val io = new Bundle {
    val in = Bits(INPUT, width = 6)
    val out = UInt(OUTPUT, width = 3)
  }
  def int_popcount(b: Int, nbits: Int): Int = {
    var ret = 0
    for (i ← 0 until nbits) {
      ret += (b >> i) & 1
    }
    return ret
  }
  val lut_entires = 1 << 6
  val lookup = Vec.tabulate(lut_entires) {
    i: Int ⇒ UInt(int_popcount(i, 6), width = 3)
  }
  io.out := lookup(io.in)
}

class PopCount36to6() extends Module {
  val io = new Bundle {
    val in = Bits(INPUT, width = 36)
    val out = UInt(OUTPUT, width = 6)
  }
  val stage1 = Vec.fill(6) { Module(new PopCount6to3()).io }
  for (i ← 0 until 6) {
    stage1(i).in := io.in((i + 1) * 6 - 1, i * 6)
  }
  val stage2 = Vec.fill(3) { Module(new PopCount6to3()).io }
  for (i ← 0 until 3) {
    stage2(i).in := Cat(stage1.map(_.out(i)))
  }

  val contrib2 = Cat(UInt(0, width = 1), stage2(2).out << 2)
  val contrib1 = Cat(UInt(0, width = 2), stage2(1).out << 1)
  val contrib0 = Cat(UInt(0, width = 3), stage2(0).out << 0)

  io.out := contrib0 + contrib1 + contrib2
  //printf("36to6 in: %x out %d \n", io.in, io.out)
}

class PopCountUnit(
  val p: PopCountUnitParams) extends Module {
  val io = new Bundle {
    // input vector
    val in = Bits(INPUT, width = p.numInputBits)
    // number of set bits in input vector
    // why the +1 here? let's say we have a 2-bit input 11, with two
    // bits set. log2Up(2) is 1, but 1 bit isn't enough to represent
    // the number 2.
    val out = UInt(OUTPUT, width = log2Up(p.numInputBits + 1))
  }
  val pcs = Vec.fill(p.getNumCompressors()) { Module(new PopCount36to6()).io }
  val inWire = p.padInput(io.in)
  val inReg = ShiftRegister(inWire, p.defaultInputRegs + p.extraPipelineRegs)
  val outWire = UInt(width = log2Up(p.numInputBits + 1))

  for (i ← 0 until p.getNumCompressors()) {
    pcs(i).in := inReg((i + 1) * 36 - 1, i * 36)
  }

  outWire := RegAdderTree(pcs.map(_.out))

  val outReg = UInt(width = log2Up(p.numInputBits + 1))
  outReg := ShiftRegister(outWire, p.defaultOutputRegs)
  io.out := outReg
}

object RegAdderTree {
  def apply(in: Iterable[UInt]): UInt = {
    if (in.size == 0) {
      UInt(0)
    } else if (in.size == 1) {
      in.head
    } else {
      Predef.assert(in.size % 2 == 0)
      Reg(next = apply(in.slice(0, in.size / 2)) + Cat(UInt(0), apply(in.slice(in.size / 2, in.size))))
    }
  }
}
