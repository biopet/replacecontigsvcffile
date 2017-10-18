package nl.biopet.tools.replacecontigsvcffile

import java.io.File

import nl.biopet.utils.ngs.fasta
import nl.biopet.utils.tool.AbstractOptParser

class ArgsParser(cmdName: String) extends AbstractOptParser[Args](cmdName) {
  opt[File]('I', "input") required () valueName "<file>" action { (x, c) =>
    c.copy(input = x)
  } text "Input vcf file"
  opt[File]('o', "output") required () unbounded () valueName "<file>" action { (x, c) =>
    c.copy(output = x)
  } text "Output vcf file"
  opt[File]('R', "referenceFile") required () unbounded () valueName "<file>" action { (x, c) =>
    c.copy(referenceFile = x)
  } text "Reference fasta file"
  opt[Map[String, String]]("contig") unbounded () action { (x, c) =>
    c.copy(contigs = c.contigs ++ x)
  }
  opt[File]("contigMappingFile") unbounded () action { (x, c) =>
    c.copy(contigs = c.contigs ++ fasta.readContigMapReverse(x))
  } text "File how to map contig names, first column is the new name, second column is semicolon separated list of alternative names"
}
