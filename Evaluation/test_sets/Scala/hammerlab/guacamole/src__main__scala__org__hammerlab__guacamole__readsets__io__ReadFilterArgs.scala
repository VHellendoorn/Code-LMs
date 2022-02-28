package org.hammerlab.guacamole.readsets.io

import org.apache.hadoop.conf.Configuration
import org.apache.spark.network.util.JavaUtils
import org.hammerlab.args4s.{ IntOptionHandler, StringOptionHandler }
import org.hammerlab.genomics.loci.args.LociArgs
import org.hammerlab.genomics.loci.parsing.{ All, ParsedLoci }
import org.kohsuke.args4j.{ Option => Args4jOption }

trait ReadFilterArgs extends LociArgs {
  @Args4jOption(
    name = "--min-alignment-quality",
    usage = "Minimum read mapping quality for a read (Phred-scaled)",
    handler = classOf[IntOptionHandler]
  )
  var minAlignmentQualityOpt: Option[Int] = None

  @Args4jOption(
    name = "--include-duplicates",
    usage = "Include reads marked as duplicates"
  )
  var includeDuplicates: Boolean = false

  @Args4jOption(
    name = "--include-failed-quality-checks",
    usage = "Include reads that failed vendor quality checks"
  )
  var includeFailedQualityChecks: Boolean = false

  @Args4jOption(
    name = "--include-single-end",
    usage = "Include single-end reads"
  )
  var includeSingleEnd: Boolean = false

  @Args4jOption(
    name = "--only-mapped-reads",
    usage = "Include only mapped reads",
    forbids = Array("--loci", "--loci-file")
  )
  var onlyMappedReads: Boolean = false

  @Args4jOption(
    name = "--split-size",
    usage = "Maximum HDFS split size",
    handler = classOf[StringOptionHandler]
  )
  var splitSize: Option[String] = None

  def parseConfig(hadoopConfiguration: Configuration): InputConfig = {
    val loci = ParsedLoci.fromArgs(lociStrOpt, lociFileOpt, hadoopConfiguration)
    InputConfig(
      overlapsLociOpt =
        if (onlyMappedReads)
          Some(All)
        else
          loci,
      nonDuplicate = !includeDuplicates,
      passedVendorQualityChecks = !includeFailedQualityChecks,
      isPaired = !includeSingleEnd,
      minAlignmentQualityOpt = minAlignmentQualityOpt,
      maxSplitSizeOpt = splitSize.map(JavaUtils.byteStringAsBytes)
    )
  }
}
