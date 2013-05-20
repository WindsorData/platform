package libt.spreadsheet.reader

import org.scalatest.FunSpec
import libt.spreadsheet.util._
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
class ReadTElementSpec extends FunSpec with BeforeAndAfter {

  import MyWorkbookFactory._
  var workbook: Workbook = _
  var sheet: Sheet = _

  def createWbReader[A](tValue: TValue[A]) =
    new WorkbookReader(
      WorkbookMapping(
        Seq(
          Area(
            TModel('key -> tValue), Offset(0, 0), None, ColumnOrientedLayout, Seq(Feature(Path('key)))))),
      new IdentityCombiner)

  before {
    workbook = createNewSingleSheetWorkbook
    sheet = workbook.getSheetAt(0)
    (1 to 4).foreach(sheet.cellAt(_, 0).setAsActiveCell())
  }

  describe("TAny") {

    val reader = createWbReader(TAny)

    it("should read numeric values as a string") {
      sheet.cellAt(0, 0).setCellValue(22)
      val result = reader.read(workbook)
      assert(result.head.toList === Seq(Right(Model('key -> Value("22.0")))))
    }

    it("should read boolean values as a string") {
      sheet.cellAt(0, 0).setCellValue(true)
      val result = reader.read(workbook)
      assert(result.head.toList === Seq(Right(Model('key -> Value("true")))))
    }

    it("should read string values as a string") {
      sheet.cellAt(0, 0).setCellValue("something")
      val result = reader.read(workbook)
      assert(result.head.toList === Seq(Right(Model('key -> Value("something")))))
    }

  }

  describe("TEnum") {
    val schema = TEnum("a","b","c")
    val reader = createWbReader(schema)

    it("should read valid entries") {
      sheet.cellAt(0, 0).setCellValue("b")
      val result = reader.read(workbook)
      assert(result.head.toList === Seq(Right(Model('key -> Value("b")))))
    }

    it("should not let read invalid entries") {
      intercept[Throwable] {
        sheet.cellAt(0, 0).setCellValue("x")
        val result = reader.read(workbook)
        schema.validate(result.head.head.right.get)
      }
    }
  }

}