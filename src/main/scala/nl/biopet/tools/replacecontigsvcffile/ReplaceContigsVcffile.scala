package nl.biopet.tools.replacecontigsvcffile

import htsjdk.variant.variantcontext.VariantContextBuilder
import htsjdk.variant.variantcontext.writer.{AsyncVariantContextWriter, VariantContextWriterBuilder}
import htsjdk.variant.vcf.VCFFileReader
import nl.biopet.utils.ngs.FastaUtils
import nl.biopet.utils.tool.ToolCommand

import scala.collection.JavaConversions._

object ReplaceContigsVcffile extends ToolCommand {
  def main(args: Array[String]): Unit = {
    val parser = new ArgsParser(toolName)
    val cmdArgs =
      parser.parse(args, Args()).getOrElse(throw new IllegalArgumentException)

    logger.info("Start")

    if (!cmdArgs.input.exists)
      throw new IllegalStateException("Input file not found, file: " + cmdArgs.input)

    logger.info("Start")

    val dict = FastaUtils.getDictFromFasta(cmdArgs.referenceFile)

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

      val newRecord =
        builder.chr(cmdArgs.contigs.getOrElse(record.getContig, record.getContig)).make()
      writer.write(newRecord)
    }

    reader.close()
    writer.close()

    logger.info("Done")
  }
}
