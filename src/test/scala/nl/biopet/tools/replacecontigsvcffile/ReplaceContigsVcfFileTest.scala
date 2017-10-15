package nl.biopet.tools.replacecontigsvcffile

import nl.biopet.test.BiopetTest
import org.testng.annotations.Test

class ReplaceContigsVcfFileTest extends BiopetTest {
  @Test
  def testNoArgs(): Unit = {
    intercept[IllegalArgumentException] {
      ReplaceContigsVcfFile.main(Array())
    }
  }
}
