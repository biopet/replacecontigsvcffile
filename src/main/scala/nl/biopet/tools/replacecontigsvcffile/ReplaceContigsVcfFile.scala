/*
 * Copyright (c) 2017 Biopet
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

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
       |$toolName needs a reference fasta file and an input VCF file.
       |The reference fasta is needed to validate the contigs. The renaming
       |of contigs can be specified in a contig mapping file.
       |The contig mapping file should be in the following format.
       |
       |    chr1    1;I;one
       |    chr2    2;II;two
       |
       |Any contigs found in the input VCF that have a contig name in the second column will be renamed
       |with the contig name in the corresponding first column.
       |
       |Alternatively, options can be specified on the command line. For example '1=chr1' will
       |convert all contigs named '1' to 'chr1'.
       |
       |Mappings are NOT case sensitive by default. If you need case sensitivity use the `--caseSensitive` flag.
       |
     """.stripMargin

  def exampleText: String =
    s"""
       |To convert the contig names in a vcf file with case sensitivity run:
       |
       |${example("-I",
                  "input.vcf",
                  "-o",
                  "output.vcf",
                  "-R",
                  "reference.fasta",
                  "--contigMappingFile",
                  "contignames.tsv",
                  "--caseSensitive")}
       |
       |To convert the contig names using command line options, similar
       |to the example contig mapping file given in the manual:
       |
       |${example(
         "-I",
         "input.vcf",
         "-o",
         "output.vcf",
         "-R",
         "reference.fasta",
         "--contig",
         "1=chr1",
         "--contig",
         "I=chr1",
         "--contig",
         "one=chr1",
         "--contig",
         "2=chr2",
         "--contig",
         "II=chr2",
         "--contig",
         "two=chr2"
       )}
       |
       | A contig mapping file and contigs can be used together:
       |${example("-I",
                  "input.vcf",
                  "-o",
                  "output.vcf",
                  "-R",
                  "reference.fasta",
                  "--contigMappingFile",
                  "contignames.tsv",
                  "--contig",
                  "3=chr3",
                  "--contig",
                  "III=chr3")}
     """.stripMargin
}
