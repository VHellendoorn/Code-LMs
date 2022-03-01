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

import org.nlp4l.core._
import org.nlp4l.core.analysis._
import org.nlp4l.stats._

val index = "/tmp/index-brown"

val schema = SchemaLoader.loadFile("examples/schema/brown.conf")
val reader = IReader(index, schema)

val docSetGOV = reader.subset(TermFilter("cat", "government"))
val docSetNEW = reader.subset(TermFilter("cat", "news"))
val docSetROM = reader.subset(TermFilter("cat", "romance"))
val docSetSF = reader.subset(TermFilter("cat", "science_fiction"))

val words = List("can", "could", "may", "might", "must", "will")

val wcGOV = WordCounts.count(reader, "body", words.toSet, docSetGOV)
val wcNEW = WordCounts.count(reader, "body", words.toSet, docSetNEW)
val wcROM = WordCounts.count(reader, "body", words.toSet, docSetROM)
val wcSF = WordCounts.count(reader, "body", words.toSet, docSetSF)

println("\n\n\nword counts")
println("========================================")
println("word\tgov\tnews\tromance\tSF")
words.foreach{ e =>
  println("%8s%,6d\t%,6d\t%,6d\t%,6d".format(e, wcGOV.getOrElse(e, 0), wcNEW.getOrElse(e, 0), wcROM.getOrElse(e, 0), wcSF.getOrElse(e, 0)))
}

val lj = List( ("gov", wcGOV), ("news", wcNEW), ("romance", wcROM), ("SF", wcSF) )
println("\n\n\nCorrelation Coefficient")
println("================================")
println("\tgov\tnews\tromance\tSF")
lj.foreach{ ej =>
  print("%s".format(ej._1))
  lj.foreach{ ei =>
    print("\t%5.3f".format(Stats.correlationCoefficient(words.map(ej._2.getOrElse(_, 0.toLong)), words.map(ei._2.getOrElse(_, 0.toLong)))))
  }
  println
}

reader.close
