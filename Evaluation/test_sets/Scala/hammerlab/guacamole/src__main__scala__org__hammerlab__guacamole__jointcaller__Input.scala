package org.hammerlab.guacamole.jointcaller

import org.hammerlab.guacamole.jointcaller.Input.{Analyte, TissueType}
import org.hammerlab.guacamole.readsets.io.{Input => RSInput}
import org.hammerlab.guacamole.readsets.{SampleId, SampleName}

/**
 * An input BAM to the joint variant caller.
 *
 * The caller can work with any number of normal and tumor BAMs, each of which may be DNA or RNA.
 *
 * @param index Throughout the joint caller, we refer to inputs by index, which is the index (0 <= index < num inputs)
 *              of the input as specified on the commandline
 * @param sampleName arbitrary name for this sample, used in VCF output
 * @param path path to BAM
 * @param tissueType tumor or normal
 * @param analyte rna or dna
 */
case class Input(index: SampleId,
                 override val sampleName: SampleName,
                 override val path: String,
                 tissueType: TissueType.Value,
                 analyte: Analyte.Value)
  extends RSInput(index, sampleName, path) {

  override def toString: String = {
    "<Input #%d '%s' of %s %s at %s >".format(index, sampleName, tissueType, analyte, path)
  }

  // Some convenience properties.
  def normal = tissueType == TissueType.Normal
  def tumor = tissueType == TissueType.Tumor
  def dna = analyte == Analyte.DNA
  def rna = analyte == Analyte.RNA
  def normalDNA = normal && dna
  def tumorDNA = tumor && dna
  def normalRNA = normal && rna
  def tumorRNA = tumor && rna
}
object Input {
  /** Kind of tissue: tumor or normal. */
  object TissueType extends Enumeration {
    val Normal = Value("normal")
    val Tumor = Value("tumor")
  }

  /** Kind of sequencing: RNA or DNA. */
  object Analyte extends Enumeration {
    val DNA = Value("dna")
    val RNA = Value("rna")
  }
}
