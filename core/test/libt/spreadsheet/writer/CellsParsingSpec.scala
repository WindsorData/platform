package libt.spreadsheet.writer

import util.FileManager
import output.mapping.TestSpreadsheetLoader

import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.apache.poi.ss.usermodel.WorkbookFactory
import libt.spreadsheet.util._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CellsParsingSpec extends FunSpec with TestSpreadsheetLoader {

  describe("cells parsing") {
    it("should not ommit blanks") {
      loadSheet("MatrixWithBlanks.xlsx") {
        x =>
          {
            val wb = WorkbookFactory.create(x)
            val sheet = wb.getSheetAt(0)

            val r =
              for (
                row <- sheet.rows;
                cell <- row.cells.take(6)
              ) yield blankToNone(_.getStringCellValue)(cell).getOrElse("_")

            assert(r.toSeq ===
              List("_", "a", "b", "c", "_", "d",
                "e", "_", "f", "g", "_", "h",
                "I", "j", "_", "k", "_", "l",
                "_", "_", "_", "_", "_", "_",
                "m", "n", "_", "_", "_", "o"))
          }
      }
    }
  }
}