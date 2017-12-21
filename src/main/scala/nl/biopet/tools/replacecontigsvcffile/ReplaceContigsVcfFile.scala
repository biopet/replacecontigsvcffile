/*
 * Copyright (c) 2017 Sequencing Analysis Support Core - Leiden University Medical Center
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
      |This tool takes an input VCF file and outputs a VCF file. It can rename contigs:
      |For example chr1 -> 1. This can be useful in a pipeline where tools have different
      |naming standards for contigs. It can also select which contigs are included in the
      |output file.
      |
    """.stripMargin

  def manualText: String =
    s"""
       |$toolName needs a reference fasta file and an input VCF file.
       |To rename contigs it needs a contig mappig file.
       |The contig mapping file should be in the following format.
       |
       |    chr1    1;I;one
       |    chr2    2;II;two
       |
       |Any contigs found in the input VCF that have a contig name in the second column will be renamed
       |with the contig name in the corresponding first column.
       |By default this is NOT case sensitive. Case sensitivity can be set with the `--caseSensitive` flag.
       |
     """.stripMargin

  def exampleText: String =
    s"""
       |To convert the contig names in a caseSensitive way run:
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
       |The reference fasta is needed to validate the contigs.
       |
       |To output a VCF with only a few contigs of interest:
       |
       |${example("-I",
                  "input.vcf",
                  "-o",
                  "output.vcf",
                  "-R",
                  "reference.fasta",
                  "--contig",
                  "chr1",
                  "--contig",
                  "chr13",
                  "--contig",
                  "chrY")}
       |
     """.stripMargin
}
