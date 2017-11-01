package nl.biopet.tools.replacecontigsvcffile

import nl.biopet.utils.test.tools.ToolTest
import org.testng.annotations.Test

class ReplaceContigsVcfFileTest extends ToolTest[Args] {
  def toolCommand: ReplaceContigsVcfFile.type = ReplaceContigsVcfFile
  @Test
  def testNoArgs(): Unit = {
    intercept[IllegalArgumentException] {
      ReplaceContigsVcfFile.main(Array())
    }
  }
}
