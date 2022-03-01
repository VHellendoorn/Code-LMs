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
import sys.process._
import fpgatidbits.PlatformWrapper._
import fpgatidbits.TidbitsMakeUtils
import fpgatidbits.hlstools.TemplatedHLSBlackBox
import fpgatidbits.synthutils.VivadoSynth

// call this object's Main to generate Verilog and characterize it in terms of
// the hardware resources required and achievable Fmax
object CharacterizeMain {
  def makeParamSpace_DPA(): Seq[DotProductArrayParams] = {
    val spatial_dim = 1 to 8
    val popcount_dim = for (i ← 4 to 8) yield (1 << i)
    val ret = for {
      m ← spatial_dim
      n ← spatial_dim
      k ← popcount_dim
    } yield new DotProductArrayParams(
      m = m, n = n, dpuParams = new DotProductUnitParams(
        inpWidth = k, accWidth = 32))
    // (m, n) resource-wise equivalent, so keep half the cases to avoid duplicates
    return ret.filter(x ⇒ (x.m >= x.n))
  }
  val instFxn_DPA = { p: DotProductArrayParams ⇒ Module(new DotProductArray(p)) }

  def makeParamSpace_PC(): Seq[PopCountUnitParams] = {
    return for {
      extra_regs ← 0 to 1
      i ← for (i ← 5 to 10) yield (1 << i)
    } yield new PopCountUnitParams(
      numInputBits = i, extraPipelineRegs = extra_regs)
  }
  val instFxn_PC = { p: PopCountUnitParams ⇒ Module(new PopCountUnit(p)) }

  def makeParamSpace_NewDPU(): Seq[DotProductUnitParams] = {
    return for {
      popc ← for (i ← 5 to 10) yield (1 << i)
    } yield new DotProductUnitParams(inpWidth = popc, accWidth = 32)
  }
  val instFxn_NewDPU = { p: DotProductUnitParams ⇒ Module(new DotProductUnit(p)) }

  def makeParamSpace_Main(): Seq[BitSerialMatMulParams] = {
    return for {
      lhs ← for (i ← 1 to 5) yield (2 * i)
      rhs ← for (i ← 1 to 5) yield (2 * i)
      z ← 64 to 64
      lmem ← 1024 to 1024
      rmem ← 1024 to 1024
    } yield new BitSerialMatMulParams(
      dpaDimLHS = lhs, dpaDimRHS = rhs, dpaDimCommon = z,
      lhsEntriesPerMem = lmem, rhsEntriesPerMem = rmem,
      mrp = PYNQZ1Params.toMemReqParams())
  }
  val instFxn_Main = { p: BitSerialMatMulParams ⇒ Module(new BitSerialMatMulAccel(p, PYNQZ1Params)) }

  def makeParamSpace_ResultStage(): Seq[ResultStageParams] = {
    return for {
      wc ← 1 to 1
      sd ← for (i ← 1 to 5) yield (2 * i)
    } yield new ResultStageParams(
      accWidth = 32, dpa_rhs = sd, dpa_lhs = sd,
      mrp = PYNQZ1Params.toMemReqParams(), resMemReadLatency = 0)
  }
  val instFxn_ResultStage = { p: ResultStageParams ⇒ Module(new ResultStage(p)) }

  def makeParamSpace_ResultBuf(): Seq[ResultBufParams] = {
    return for {
      a ← 1 to 4
      d ← Seq(16, 32, 64)
      r ← Seq(0, 1)
    } yield new ResultBufParams(
      addrBits = a, dataBits = d, regIn = r, regOut = r)
  }
  val instFxn_ResultBuf = { p: ResultBufParams ⇒ Module(new ResultBuf(p)) }

  def makeParamSpace_FetchStage(): Seq[FetchStageParams] = {
    return for {
      c ← 1 to 4
      n ← for (i ← 1 to 8) yield c * i
    } yield new FetchStageParams(
      numLHSMems = n, numRHSMems = n,
      numAddrBits = 10, mrp = PYNQZ1Params.toMemReqParams())
  }
  val instFxn_FetchStage = { p: FetchStageParams ⇒ Module(new FetchDecoupledStage(p)) }

  def makeParamSpace_BlackBoxCompressor(): Seq[BlackBoxCompressorParams] = {
    return for {
      n ← for (i ← 6 to 8) yield 1 << i
      d ← 0 to 0
      aw <- Seq(2, 3)// 2, 4, 8, 16)
      bw <- Seq(1, 2, 3)// 2, 4, 8, 16)
    } yield new BlackBoxCompressorParams(
      N = n, D = d, WD = aw, WC = bw)
  }

  val instFxn_BlackBoxCompressor = { p: BlackBoxCompressorParams ⇒ Module(new BlackBoxCompressor(p)) }

  val instFxn_BBC = { p: BlackBoxCompressorParams ⇒ Module(new CharacterizationBBCompressor(p)) }

  def makeParamSpace_P2SAccel(): Seq[StandAloneP2SParams] = {
    return for {
      mbw ← Seq(8, 16, 32, 64)
      nxw ← Seq(64 / mbw, 128 / mbw, 256 / mbw, 512 / mbw)
      fast ← Seq(false, true)
    } yield new StandAloneP2SParams(
      maxInBw = mbw, nInElemPerWord = nxw, outStreamSize = mbw * nxw, mrp = PYNQZ1Params.toMemReqParams(),
      fastMode = fast)
  }
  val instFxn_P2SAccel = { p: StandAloneP2SParams ⇒ Module(new StandAloneP2SAccel(p, TesterWrapperParams)) }

  def makeParamSpace_SU(): Seq[SerializerUnitParams] = {
    return for {

      inBW <- Seq(4, 8, 16, 32)
      maxCounterBW <- Seq(inBW, 32)
      rows <- 8 to 8
      cols <- 8 to 8
      static <- Seq(true,false)

    } yield new SerializerUnitParams(
    inPrecision = inBW, matrixRows = rows, matrixCols = cols, staticCounter = static, maxCounterPrec = maxCounterBW
    )
  }

  val instFxn_SU = {p: SerializerUnitParams => Module(new SerializerUnit(p))}

  def main(args: Array[String]): Unit = {
    val chName: String = args(0)
    val chPath: String = args(1)
    val platform: String = args(2)
    val chLog = chName + ".log"
    val fpgaPart: String = TidbitsMakeUtils.fpgaPartMap(platform)

    if (chName == "CharacterizePC") {
      VivadoSynth.characterizeSpace(makeParamSpace_PC(), instFxn_PC, chPath, chLog, fpgaPart)
    } else if (chName == "CharacterizeNewDPU") {
      VivadoSynth.characterizeSpace(makeParamSpace_NewDPU(), instFxn_NewDPU, chPath, chLog, fpgaPart)
    } else if (chName == "CharacterizeDPA") {
      VivadoSynth.characterizeSpace(makeParamSpace_DPA(), instFxn_DPA, chPath, chLog, fpgaPart)
    } else if (chName == "CharacterizeMain") {
      VivadoSynth.characterizeSpace(makeParamSpace_Main(), instFxn_Main, chPath, chLog, fpgaPart)
    } else if (chName == "CharacterizeResultStage") {
      VivadoSynth.characterizeSpace(makeParamSpace_ResultStage(), instFxn_ResultStage, chPath, chLog, fpgaPart)
    } else if (chName == "CharacterizeResultBuf") {
      VivadoSynth.characterizeSpace(makeParamSpace_ResultBuf(), instFxn_ResultBuf, chPath, chLog, fpgaPart)
    } else if (chName == "CharacterizeFetchStage") {
      VivadoSynth.characterizeSpace(makeParamSpace_FetchStage(), instFxn_FetchStage, chPath, chLog, fpgaPart)
    } else if (chName == "CharacterizeSU") {
      VivadoSynth.characterizeSpace(makeParamSpace_SU(), instFxn_SU, chPath, chLog, fpgaPart)
    } else if (chName == "CharacterizeBBCompressor") {
      VivadoSynth.characterizeSpace(makeParamSpace_BlackBoxCompressor(), instFxn_BlackBoxCompressor, chPath, chLog, fpgaPart)
    }else if (chName == "CharacterizeBBC"){
      VivadoSynth.characterizeSpace(makeParamSpace_BlackBoxCompressor(), instFxn_BBC, chPath, chLog, fpgaPart)
    } else if (chName == "CharacterizeP2SAccel") {
      VivadoSynth.characterizeSpace(makeParamSpace_P2SAccel(), instFxn_P2SAccel, chPath, chLog, fpgaPart)
    } else {
      println("Unrecognized target for characterization")
    }
  }
}
