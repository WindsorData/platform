package libt.spreadsheet.reader

import org.scalatest.FunSpec
import libt.spreadsheet.util._
import libt.error._
import libt._
import libt.spreadsheet.Offset
import libt.spreadsheet.Feature
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.BeforeAndAfter
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.Sheet
import java.util.Date

@RunWith(classOf[JUnitRunner])
class ReadAnyTypeSpec extends FunSpec with BeforeAndAfter {

  import MyWorkbookFactory._
  var workbook: Workbook = _
  var sheet: Sheet = _

  describe("TAny") {
    val reader = new WorkbookReader(
      WorkbookMapping(
        Seq(
          Area(
            TModel('a -> TAny), Offset(0, 0), None, ColumnOrientedLayout, Seq(Feature(Path('a)))))),
      new IdentityCombiner)

    before {
      workbook = createNewSingleSheetWorkbook
      sheet = workbook.getSheetAt(0)
      (1 to 4).foreach(sheet.cellAt(_, 0).setAsActiveCell())
    }

    it("should read numeric values as a string") {
      sheet.cellAt(0, 0).setCellValue(22)
      val result = reader.read(workbook)
      assert(result.head.toList === Seq(Valid(Model('a -> Value("22.0")))))
    }

    it("should read boolean values as a string") {
      sheet.cellAt(0, 0).setCellValue(true)
      val result = reader.read(workbook)
      assert(result.head.toList === Seq(Valid(Model('a -> Value("true")))))
    }
    
    it("should read string values as a string") {
      sheet.cellAt(0, 0).setCellValue("something")
      val result = reader.read(workbook)
      assert(result.head.toList === Seq(Valid(Model('a -> Value("something")))))
    }

  }
}