package org.hammerlab.guacamole.jointcaller.pileup_summarization

import org.hammerlab.guacamole.jointcaller.InputCollection
import org.hammerlab.guacamole.readsets.PerSample

/**
 * Collection of per-sample PileupStats instances plus pooled normal and tumor DNA PileupStats.
 *
 * Used as a convenient way to pass several PileupStats instances around.
 *
 */
case class MultiplePileupStats(inputs: InputCollection, singleSampleStats: PerSample[PileupStats]) {
  val referenceSequence = singleSampleStats.head.referenceSequence
  val normalDNAPooled = PileupStats(
    inputs.normalDNA.flatMap(input => singleSampleStats(input.index).elements), referenceSequence)
  val tumorDNAPooled = PileupStats(
    inputs.tumorDNA.flatMap(input => singleSampleStats(input.index).elements), referenceSequence)
}
