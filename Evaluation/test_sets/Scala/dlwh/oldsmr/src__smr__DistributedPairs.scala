/*
* Copyright (c) 2008, David Hall
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*     * Redistributions of source code must retain the above copyright
*       notice, this list of conditions and the following disclaimer.
*     * Redistributions in binary form must reproduce the above copyright
*       notice, this list of conditions and the following disclaimer in the
*       documentation and/or other materials provided with the distribution.
*
* THIS SOFTWARE IS PROVIDED BY DAVID HALL ``AS IS'' AND ANY
* EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
* WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED. IN NO EVENT SHALL DAVID HALL BE LIABLE FOR ANY
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
* LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
* (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package smr;

import scala.reflect.Manifest;

/**
 * Represents a Distributed Iterable over Pairs. Distinct from a 
 * DistributedIterable[(K,V)] because it's designed for the MapReduce framework
 * and hence only supports operations that yield pairs.
 */
trait DistributedPairs[+K,+V] { self =>
  def elements : Iterator[(K,V)];
  def map[J,U](f : ((K,V))=>(J,U))(implicit m : Manifest[J], mU:Manifest[U]): DistributedPairs[J,U];
  def flatMap[J,U](f : ((K,V))=>Iterable[(J,U)])(implicit m: Manifest[J], mU:Manifest[U]) : DistributedPairs[J,U]
  def filter(f : ((K,V))=>Boolean) : DistributedPairs[K,V];

  /**
   * Process any computations that have been cached and return a new
   * DistributedPairs with those results.
   */
  def force() : DistributedPairs[K,V];

  /**
   * Models MapReduce style reduce more exactly.
   */
  def flatReduce[J,U](f : (K,Iterator[V])=>Iterator[(J,U)])(implicit m : Manifest[J], mU:Manifest[U]): DistributedPairs[J,U];

  /**
  * For a slightly more "classic" reduce that outputs exactly one item for each input. Still not Scala's reduce.
  */
  def reduce[J,U](f : (K,Iterator[V])=>(J,U))(implicit m : Manifest[J], mU:Manifest[U]): DistributedPairs[J,U];

  def mapFirst[J](f: K=>J)(implicit m:Manifest[J]) : DistributedPairs[J,V];
  def mapSecond[U](f: V=>U)(implicit m:Manifest[U]) : DistributedPairs[K,U];

  def toIterable : Iterable[(K,V)] = new Iterable[(K,V)] {
    def elements = self.elements;
  }

  /**
  * Checkpoints a chain of operations, saving the output for later use (in say, a future run)
  */
  def asStage(name : String) : DistributedPairs[K,V];

  /**
  * Appends two pairs together.
  */
  def ++[SK>:K,SV>:V](other : DistributedPairs[SK,SV])(implicit mSK : Manifest[SK], mSV:Manifest[SV]) : DistributedPairs[SK,SV];
}


