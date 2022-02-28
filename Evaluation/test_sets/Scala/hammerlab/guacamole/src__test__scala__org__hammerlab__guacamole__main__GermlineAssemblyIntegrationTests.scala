package org.hammerlab.guacamole.main

import org.apache.spark.SparkContext
import org.hammerlab.guacamole.commands.GermlineAssemblyCaller.Arguments
import org.hammerlab.guacamole.commands.GuacCommand
import org.hammerlab.guacamole.data.NA12878TestUtil
import org.hammerlab.guacamole.util.TestUtil.resourcePath
import org.hammerlab.guacamole.variants.VariantComparisonTest

/**
 * Germline assembly caller integration "tests" that output various statistics to stdout.
 *
 * To run:
 *
 *   mvn package -DskipTests -Pguac,test
 *   scripts/guacamole-test GermlineAssemblyIntegrationTests
 */
object GermlineAssemblyIntegrationTests extends GuacCommand[Arguments] with VariantComparisonTest {

  override val name: String = "germline-assembly-integration-test"
  override val description: String = "output various statistics to stdout"

  def main(args: Array[String]): Unit =
    run(
      "--reads", NA12878TestUtil.subsetBam,
      "--reference", NA12878TestUtil.chr1PrefixFasta,
      "--loci", "chr1:0-6700000",
      "--out", "/tmp/germline-assembly-na12878-guacamole-tests.vcf",
      "--partition-accuracy", "0",
      "--parallelism", "0",
      "--kmer-size", "31",
      "--assembly-window-range", "41",
      "--min-area-vaf", "40",
      "--shortcut-assembly",
      "--min-likelihood", "70",
      "--min-occurrence", "3"
    )

  override def run(args: Arguments, sc: SparkContext): Unit = {

    println("Germline assembly calling on subset of illumina platinum NA12878")

    val resultFile = args.variantOutput + "/part-r-00000"
    println("************* GUACAMOLE GermlineAssembly *************")
    compareToVCF(resultFile, NA12878TestUtil.expectedCallsVCF)

    println("************* UNIFIED GENOTYPER *************")
    compareToVCF(resourcePath(
      "illumina-platinum-na12878/unified_genotyper.vcf"),
      NA12878TestUtil.expectedCallsVCF)

    println("************* HaplotypeCaller *************")
    compareToVCF(resourcePath(
      "illumina-platinum-na12878/haplotype_caller.vcf"),
      NA12878TestUtil.expectedCallsVCF)
  }
}
