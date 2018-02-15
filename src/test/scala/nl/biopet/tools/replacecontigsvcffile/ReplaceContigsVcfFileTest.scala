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

import java.io.File

import nl.biopet.utils.ngs.intervals.BedRecord
import nl.biopet.utils.test.tools.ToolTest
import org.testng.annotations.Test
import nl.biopet.utils.ngs.vcf

class ReplaceContigsVcfFileTest extends ToolTest[Args] {
  def toolCommand: ReplaceContigsVcfFile.type = ReplaceContigsVcfFile
  @Test
  def testNoArgs(): Unit = {
    intercept[IllegalArgumentException] {
      ReplaceContigsVcfFile.main(Array())
    }
  }

  @Test
  def testMain(): Unit = {
    val inputFile = resourceFile("/test.vcf")
    val outputFile = File.createTempFile("test.", ".vcf")
    outputFile.deleteOnExit()

    ReplaceContigsVcfFile.main(
      Array("-I",
            inputFile.getAbsolutePath,
            "-o",
            outputFile.getAbsolutePath,
            "--contig",
            "chrQ2=chrQ",
            "-R",
            resourcePath("/fake_chrQ.fa")))

    val record =
      vcf.loadRegions(outputFile, Iterator(BedRecord("chrQ", 1, 16000))).next()
    record.getContig shouldBe "chrQ"
  }

  @Test
  def testCase(): Unit = {
    val inputFile = resourceFile("/test2.vcf")
    val outputFile = File.createTempFile("test.", ".vcf")
    outputFile.deleteOnExit()

    ReplaceContigsVcfFile.main(
      Array("-I",
            inputFile.getAbsolutePath,
            "-o",
            outputFile.getAbsolutePath,
            "-R",
            resourcePath("/fake_chrQ.fa")))

    val record =
      vcf.loadRegions(outputFile, Iterator(BedRecord("chrQ", 1, 16000))).next()
    record.getContig shouldBe "chrQ"
  }

  @Test
  def testCorrect(): Unit = {
    val inputFile = resourceFile("/correct.vcf")
    val outputFile = File.createTempFile("test.", ".vcf")
    outputFile.deleteOnExit()

    ReplaceContigsVcfFile.main(
      Array("-I",
            inputFile.getAbsolutePath,
            "-o",
            outputFile.getAbsolutePath,
            "-R",
            resourcePath("/fake_chrQ.fa"),
            "--caseSensitive"))

    val record =
      vcf.loadRegions(outputFile, Iterator(BedRecord("chrQ", 1, 16000))).next()
    record.getContig shouldBe "chrQ"
  }

  @Test
  def testInputNotExist(): Unit = {
    val inputFile = File.createTempFile("test.", ".vcf")
    inputFile.delete()
    val outputFile = File.createTempFile("test.", ".vcf")
    outputFile.deleteOnExit()

    intercept[IllegalArgumentException] {
      ReplaceContigsVcfFile.main(
        Array("-I",
              inputFile.getAbsolutePath,
              "-o",
              outputFile.getAbsolutePath,
              "-R",
              resourcePath("/fake_chrQ.fa")))
    }.getMessage shouldBe s"requirement failed: Input file not found, file: ${inputFile.getAbsolutePath}"
  }

}
