/*
 * Copyright 2015 RONDHUIT Co.,LTD.
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

package org.nlp4l.syn

import scala.io.Source

object SynonymCommon {
  
  def readAllRecords(synFile: String): (Seq[String], Seq[Seq[String]]) = {
    val file = Source.fromFile(synFile, "UTF-8")
    val headerComments = new scala.collection.mutable.ArrayBuffer[String]()
    val records = new scala.collection.mutable.ArrayBuffer[Seq[String]]()
    
    try{
      file.getLines().map(_.trim).foreach{ line =>
        if(line.length == 0 || line.startsWith("#")){
          headerComments.append(line)
        }
        else{
          val words = line.split(",").map(_.trim)
          records.append(words)
        }
      }
      (headerComments, records)
    }
    finally{
      file.close()
    }
  }

  def getUniqueRecords(records: Seq[Seq[String]], out: Seq[Seq[String]]): Seq[Seq[String]] = {
    if(records.isEmpty) out
    else{
      val out2 = checkRecords(records.head, records.tail, List(), out)
      getUniqueRecords(out2._2, out2._1)
    }
  }
  
  def checkRecords(srcRecord: Seq[String], records: Seq[Seq[String]], srcRecords: Seq[Seq[String]], out: Seq[Seq[String]]):
    (Seq[Seq[String]], Seq[Seq[String]]) = {
    if(records.isEmpty) (out :+ srcRecord, srcRecords)
    else{
      unifyRecordsIfNeeded(srcRecord, records.head) match {
        case Some(result) => checkRecords(result, records.tail, srcRecords, out)
        case None => checkRecords(srcRecord, records.tail, srcRecords :+ records.head, out)
      }
    }
  }
  
  def unifyRecordsIfNeeded(left: Seq[String], right: Seq[String]): Option[Seq[String]] = {
    for(l <- left){
      for(r <- right){
        if(l == r){
          //println("%s is going to be merged into %s".format(right, left))
          return Some((left ++ right).toSet.toList.sorted)
        }
      }
    }
    None
  }
}
