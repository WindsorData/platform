package libt.spreadsheet.reader

import util.FileManager
import org.scalatest.FunSpec
import libt.spreadsheet.util._
import libt.workflow._
import libt._

import libt.spreadsheet.Offset
import libt.spreadsheet.Feature
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.BeforeAndAfter
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.Sheet
import java.util.Date
import java.io.FileOutputStream
import libt.error.Valid
import org.apache.poi.ss.usermodel.WorkbookFactory

@RunWith(classOf[JUnitRunner])
class ReadTElementSpec extends FunSpec with BeforeAndAfter {

  import MyWorkbookFactory._

  var workbook: Workbook = _
  var sheet: Sheet = _

  def createWbReader[A](tValue: TValue[A]) =
    WorkbookMapping(
      Seq(
        Area(
          TModel('key -> tValue), Offset(0, 0), None, ColumnOrientedLayout, Seq(Feature(Path('key))))))


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
      assert(result.head.toList === Seq(Valid(Model('key -> Value("22.0")))))
    }

    it("should read formulas as a string") {
      sheet.cellAt(0, 0).setCellFormula("2+2")
      val result = reader.read(workbook)
      assert(result.head.toList === Seq(Valid(Model('key -> Value("2+2")))))
    }


    it("should read boolean values as a string") {
      sheet.cellAt(0, 0).setCellValue(true)
      val result = reader.read(workbook)
      assert(result.head.toList === Seq(Valid(Model('key -> Value("true")))))
    }

    it("should read string values as a string") {
      sheet.cellAt(0, 0).setCellValue("something")
      val result = reader.read(workbook)
      assert(result.head.toList === Seq(Valid(Model('key -> Value("something")))))
    }

  }

  describe("TNumber") {

    val reader = createWbReader(TNumber)

    it("should read numeric values") {
      sheet.cellAt(0, 0).setCellValue(10)
      val result = reader.read(workbook)
      assert(result.head.toList === Seq(Valid(Model('key -> Value(10)))))
    }

    it("should read percentage values as a number") {
      FileManager.loadResource("input/Percentage.xlsx") {  //TODO bad test
        x =>
          workbook = WorkbookFactory.create(x)
          sheet = workbook.getSheetAt(0)
          (1 to 4).foreach(sheet.cellAt(_, 0).setAsActiveCell())
      }
      val result = reader.read(workbook)
      assert(result.head.toList === Seq(Valid(Model('key -> Value(0.1: BigDecimal)))))
    }

  }

  describe("TGenericEnum") {

    it("should read valid Strings") {
      val reader = createWbReader(TStringEnum("a", "b", "c"))
      sheet.cellAt(0, 0).setCellValue("b")
      val result = reader.read(workbook)
      assert(result.head.toList === Seq(Valid(Model('key -> Value("b")))))
    }

    ignore("should read valid Numbers") {
      val reader = createWbReader(TNumberEnum(1, 2, 3))
      sheet.cellAt(0, 0).setCellValue(1)
      val result = reader.read(workbook)
      assert(result.head.toList === Seq(Valid(Model('key -> Value(1)))))
    }

    it("should not let read invalid entries") {
      intercept[Throwable] {
        val schema = TStringEnum("a", "b", "c")
        val reader = createWbReader(schema)
        sheet.cellAt(0, 0).setCellValue("x")
        val result = reader.read(workbook)
        schema.validate(result.head.head.get)
      }
    }
  }

}