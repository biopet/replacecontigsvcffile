package nl.biopet.tools.replacecontigsvcffile

import nl.biopet.utils.test.tools.ToolTest
import org.testng.annotations.Test

class ReplaceContigsVcfFileTest extends ToolTest[Args] {
  @Test
  def testNoArgs(): Unit = {
    intercept[IllegalArgumentException] {
      ReplaceContigsVcfFile.main(Array())
    }
  }
}
