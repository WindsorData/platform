package libt.spreadsheet

import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import libt.spreadsheet.reader._
import libt.spreadsheet._
import libt.spreadsheet.util._
import libt.reduction._
import libt._
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import scala.math.BigDecimal.int2bigDecimal
import org.scalatest.junit.JUnitRunner

class CalcSpec extends FlatSpec {

  val model = Seq(
    Model(
      'a -> Value(Some("hello"), Some("calc"), Some("comment"), Some("note"), Some("link")),
      'b -> Col(Value(2: BigDecimal), Value(4: BigDecimal))),
    Model(
      'a -> Value("world"),
      'b -> Col(Value(1: BigDecimal), Value(0: BigDecimal))))

  val area = Area(
    TModel(
      'a -> TString,
      'b -> TCol(TNumber)),
    Offset(0, 0),
    None,
    ColumnOrientedLayout,
    Seq(
      Feature('a),
      Calc(Sum(Path('b, *)))))

  def writeSheet = {
    val wb = new HSSFWorkbook()
    val sheet = wb.createSheet()
    area.write(model)(sheet)
    sheet
  }

  it should "calculate using the given reduction" in {
    val sheet = writeSheet
    assert(sheet.cellAt(0, 0).getStringCellValue === "hello")
    assert(sheet.cellAt(0, 1).getNumericCellValue === (2 + 4))
    assert(sheet.cellAt(6, 0).getStringCellValue === "world")
    assert(sheet.cellAt(6, 1).getNumericCellValue === (1 + 0))
  }

  it should "have empty metadata" in {
    val sheet = writeSheet
    assert(sheet.cellAt(1, 0).getStringCellValue === "calc")
    assert(sheet.cellAt(1, 1).getStringCellValue === "Calculated")
    assert(sheet.cellAt(2, 0).getStringCellValue === "comment")
    assert(sheet.cellAt(2, 1).getCellType === Cell.CELL_TYPE_BLANK)
    assert(sheet.cellAt(3, 0).getStringCellValue === "note")
    assert(sheet.cellAt(3, 1).getCellType === Cell.CELL_TYPE_BLANK)
    assert(sheet.cellAt(4, 0).getStringCellValue === "link")
    assert(sheet.cellAt(4, 1).getCellType === Cell.CELL_TYPE_BLANK)
  }

}