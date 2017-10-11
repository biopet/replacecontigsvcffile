package nl.biopet.tools.replacecontigsvcffile

import nl.biopet.test.BiopetTest
import org.testng.annotations.Test

class ReplaceContigsVcffileTest extends BiopetTest {
  @Test
  def testNoArgs(): Unit = {
    intercept[IllegalArgumentException] {
      ReplaceContigsVcffile.main(Array())
    }
  }
}
