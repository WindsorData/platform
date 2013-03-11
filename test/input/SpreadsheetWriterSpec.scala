package input

import org.scalatest.path.FunSpec
import util.FileManager
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.io.File

@RunWith(classOf[JUnitRunner])
class SpreadsheetWriterSpec extends FunSpec {

  describe("An Exporter") {

    it("should export executives into an excel") {
        val executives = FileManager.loadSpreadsheet("test/input/FullValuesOnly.xlsx")
        FileManager.generateNewFileWith("test/input/outputExecutivesTest.xlsx", executives.head)
        val fileTest = new File("test/input/outputExecutivesTest.xlsx")
        assert(fileTest.exists)
        fileTest.delete
    }
  }

}