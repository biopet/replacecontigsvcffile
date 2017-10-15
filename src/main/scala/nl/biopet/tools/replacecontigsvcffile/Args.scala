package nl.biopet.tools.replacecontigsvcffile

import java.io.File

case class Args(input: File = null,
                output: File = null,
                referenceFile: File = null,
                contigs: Map[String, String] = Map())
