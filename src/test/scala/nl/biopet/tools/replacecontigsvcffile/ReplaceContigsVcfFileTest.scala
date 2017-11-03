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

    ReplaceContigsVcfFile.main(Array("-I", inputFile.getAbsolutePath,
      "-o", outputFile.getAbsolutePath, "--contig", "chrQ2=chrQ", "-R", resourcePath("/fake_chrQ.fa")))

    val record = vcf.loadRegions(outputFile, Iterator(BedRecord("chrQ", 1, 16000))).next()
    record.getContig shouldBe "chrQ"
  }

  @Test
  def testCase(): Unit = {
    val inputFile = resourceFile("/test2.vcf")
    val outputFile = File.createTempFile("test.", ".vcf")
    outputFile.deleteOnExit()

    ReplaceContigsVcfFile.main(Array("-I", inputFile.getAbsolutePath,
      "-o", outputFile.getAbsolutePath, "-R", resourcePath("/fake_chrQ.fa")))

    val record = vcf.loadRegions(outputFile, Iterator(BedRecord("chrQ", 1, 16000))).next()
    record.getContig shouldBe "chrQ"
  }

  @Test
  def testCorrect(): Unit = {
    val inputFile = resourceFile("/correct.vcf")
    val outputFile = File.createTempFile("test.", ".vcf")
    outputFile.deleteOnExit()

    ReplaceContigsVcfFile.main(Array("-I", inputFile.getAbsolutePath,
      "-o", outputFile.getAbsolutePath, "-R", resourcePath("/fake_chrQ.fa"), "--caseSensitive"))

    val record = vcf.loadRegions(outputFile, Iterator(BedRecord("chrQ", 1, 16000))).next()
    record.getContig shouldBe "chrQ"
  }

  @Test
  def testInputNotExist(): Unit = {
    val inputFile = File.createTempFile("test.", ".vcf")
    inputFile.delete()
    val outputFile = File.createTempFile("test.", ".vcf")
    outputFile.deleteOnExit()

    intercept[IllegalArgumentException] {
      ReplaceContigsVcfFile.main(Array("-I", inputFile.getAbsolutePath,
        "-o", outputFile.getAbsolutePath, "-R", resourcePath("/fake_chrQ.fa")))
    }.getMessage shouldBe s"requirement failed: Input file not found, file: ${inputFile.getAbsolutePath}"
  }

}
