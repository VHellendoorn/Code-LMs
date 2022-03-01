/*
 * Copyright 2015 org.NLP4L
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

import org.apache.lucene.search.spell.LuceneLevenshteinDistance
import org.nlp4l.core.RawReader
import org.nlp4l.framework.models._
import org.nlp4l.framework.processors.{Processor, ProcessorFactory, DictionaryAttributeFactory}
import org.nlp4l.lm.{HmmTokenizer, HmmModel}
import org.slf4j.LoggerFactory

import scala.collection.mutable.ListBuffer
import scala.util.matching.Regex

class LoanWordsDictionaryAttributeFactory(settings: Map[String, String]) extends DictionaryAttributeFactory(settings) {
  override def getInstance: DictionaryAttribute = {

    val list = Seq[CellAttribute](
      CellAttribute("word", CellType.StringType, true, true),
      CellAttribute("synonym", CellType.StringType, false, true)
    )
    new DictionaryAttribute("loanWords", list)
  }
}

class LoanWordsProcessorFactory(settings: Map[String, String]) extends ProcessorFactory(settings) {

  val DEF_THRESHOLD = 0.8F
  val DEF_MIN_DOCFREQ = 3

  override def getInstance: Processor = {
    val index = getStrParamRequired("index")
    val field = getStrParamRequired("field")
    val modelIndex = getStrParamRequired("modelIndex")
    val threshold = getFloatParam("threshold", DEF_THRESHOLD)
    val minDocFreq = getIntParam("minDocFreq", DEF_MIN_DOCFREQ)
    new LoanWordsProcessor(index, field, modelIndex, threshold, minDocFreq)
  }
}

class LoanWordsProcessor(val index: String, val field: String, val modelIndex: String,
                          val threshold: Float, val minDocFreq: Int) extends Processor {

  override def execute(data: Option[Dictionary]): Option[Dictionary] = {
    val logger = LoggerFactory.getLogger(this.getClass)
    val reader = RawReader(index)
    val trModel = new TransliterationModelIndex(modelIndex)

    val pattern: Regex = """([a-z]+) ([\u30A0-\u30FF]+)""".r
    val lld = new LuceneLevenshteinDistance()

    val records = ListBuffer.empty[Record]
    try{
      var progress = 0
      val fi = reader.field(field)
      fi match {
        case Some(f) => {
          val len = f.uniqTerms
          f.terms.foreach { t =>
            progress = progress + 1
            if((progress % 10000) == 0){
              val percent = ((progress.toFloat / len) * 100).toInt
              logger.info(s"$percent % done ($progress / $len) term is ${t.text}")
            }
            if (t.docFreq >= minDocFreq) {
              t.text match {
                case pattern(a, b) => {
                  val predWord = trModel.predict(b)
                  if (lld.getDistance(a, predWord) > threshold) {
                    records += Record(Seq(Cell("word", a), Cell("synonym", b)))
                  }
                }
                case _ => {}
              }
            }
          }
          Some(Dictionary(records))
        }
        case _ => throw new RuntimeException(s"""field "$field" you specified in conf file doesn't exist in the index "$index""")
      }
    }
    finally{
      if(reader != null) reader.close
    }
  }
}

class TransliterationModelIndex(index: String){

  private val model = HmmModel(index)

  private val tokenizer = HmmTokenizer(model)

  def predict(katakana: String): String = {
    tokenizer.tokens(katakana).map(_.cls).mkString
  }
}
