package nl.biopet.tools.replacecontigsvcffile

import htsjdk.variant.variantcontext.VariantContextBuilder
import htsjdk.variant.variantcontext.writer.{
  AsyncVariantContextWriter,
  VariantContextWriterBuilder
}
import htsjdk.variant.vcf.VCFFileReader
import nl.biopet.utils.ngs.fasta
import nl.biopet.utils.tool.ToolCommand

import scala.collection.JavaConversions._

object ReplaceContigsVcfFile extends ToolCommand[Args] {
  def emptyArgs: Args = Args()
  def argsParser = new ArgsParser(this)
  def main(args: Array[String]): Unit = {
    val cmdArgs: Args = cmdArrayToArgs(args)

    require(cmdArgs.input.exists,
            s"Input file not found, file: ${cmdArgs.input}")

    logger.info("Start")

    val dict = fasta.getDictFromFasta(cmdArgs.referenceFile)

    val contigMap = {
      val caseSensitive = cmdArgs.contigMapFile
        .map(fasta.readContigMapReverse)
        .getOrElse(Map()) ++ cmdArgs.contigs
      if (cmdArgs.caseSensitive) caseSensitive
      else {
        caseSensitive.map(x => x._1.toLowerCase -> x._2) ++ caseSensitive ++
          dict.getSequences
            .filter(x => x.getSequenceName.toLowerCase != x.getSequenceName)
            .map(x => x.getSequenceName.toLowerCase -> x.getSequenceName)
      }
    }

    val reader = new VCFFileReader(cmdArgs.input, false)
    val header = reader.getFileHeader
    header.setSequenceDictionary(dict)
    val writer = new AsyncVariantContextWriter(
      new VariantContextWriterBuilder()
        .setOutputFile(cmdArgs.output)
        .setReferenceDictionary(dict)
        .build)
    writer.writeHeader(header)

    for (record <- reader) {
      val builder = new VariantContextBuilder(record)

      val newRecord = {
        if (contigMap.contains(record.getContig))
          builder.chr(contigMap(record.getContig)).make()
        else if (!cmdArgs.caseSensitive && contigMap.contains(
                   record.getContig.toLowerCase))
          builder.chr(contigMap(record.getContig.toLowerCase)).make()
        else record
      }
      writer.write(newRecord)
    }

    reader.close()
    writer.close()

    logger.info("Done")
  }

  def descriptionText: String =
    """
      |This tool takes an input VCF file and outputs a VCF file with renamed contigs.
      |For example chr1 -> 1. This can be useful in a pipeline where tools have different
      |naming standards for contigs.
    """.stripMargin

  def manualText: String =
    s"""
       |$toolName needs a reference fasta file, an input VCF file and a contig mappig file.
       |The contig mapping file should be in the following format.
       |
       |    chr1    1;I;one
       |    chr2    2;II;two
       |
       |Any contigs found in the input VCF that have a contig name in the second column will be renamed
       |with the contig name in the corresponding first column.
       |
     """.stripMargin

  def exampleText: String =
    s"""
       |To convert the contig names in a vcf file run:
       |
       |${example("-I", "input.vcf", "-o", "output.vcf", "-R", "reference.fasta", "--contigMappingFile", "contignames.tsv")}
       |
       |The reference fasta is needed to validate the contigs.
     """.stripMargin
}
