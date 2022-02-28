package org.allenai.ike.ml.subsample

import org.allenai.blacklab.search.lucene.{ BLSpansWrapper, SpanQueryBase }
import org.apache.lucene.index.{ AtomicReaderContext, Term, TermContext }
import org.apache.lucene.search.spans.{ SpanQuery, Spans }
import org.apache.lucene.util.Bits

import java.util

/** Modifies a SpanQuery so that the returned Spans only occur after the given document and token
  *
  * @param query SpanQuery to modify
  * @param startDoc document to start from, returned hits have doc >= startFromDoc
  * @param startToken token to start from, returned hits have doc > startFromDoc or
  * start >= startFromToken
  */
class SpanQueryStartAt(query: SpanQuery, startDoc: Int, startToken: Int)
    extends SpanQueryBase(query) {
  override def getSpans(
    atomicReaderContext: AtomicReaderContext,
    bits: Bits,
    map: util.Map[Term, TermContext]
  ): Spans = {
    val clauseSpans = BLSpansWrapper.optWrap(clauses(0).getSpans(atomicReaderContext, bits, map))
    new SpansStartAt(clauseSpans, startDoc, startToken)
  }

  override def toString(s: String): String = s"${query.toString(s)}<AFTER " +
    s"DOC=$startDoc TOKEN=$startToken>"
}
