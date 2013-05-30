package libt.spreadsheet

import org.scalatest.FlatSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import libt.spreadsheet.util._
import libt.spreadsheet._
import libt._
import libt.spreadsheet.reader.CellReader
import libt.builder.ModelBuilder
import libt.spreadsheet.writer._
import libt.spreadsheet.writer.op._
import libt.spreadsheet.writer.op
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import libt.spreadsheet.reader.Area
import libt.spreadsheet.reader.ColumnOrientedLayout
import libt.reduction._
import java.io.FileOutputStream

@RunWith(classOf[JUnitRunner])
class EnumCalcSpec extends FlatSpec {

  val TOptions = TEnum("A", "B", "C")

  val model = Seq(
    Model('foo -> Model(
      'bar ->
        Col(Value("A"), Value("C"), Value("D")))),
    Model('foo -> Model(
      'bar ->
        Col(Value("B")))),

    Model('foo -> Model(
      'bar ->
        Col(Value("C"), Value("X")))))

  val area = Area(
    TModel(
      'foo -> TModel(
        'bar -> TCol(TOptions))),
    Offset(1, 0),
    None,
    ColumnOrientedLayout,
    Seq(
      EnumCheck(Path('foo, 'bar, *), "A"),
      EnumCheck(Path('foo, 'bar, *), "B"),
      EnumCheck(Path('foo, 'bar, *), "C")))

  def writeSheet = {
    val wb = new HSSFWorkbook()
    val sheet = wb.createSheet()
    sheet.cellAt(0, 0).setCellValue("A")
    sheet.cellAt(0, 1).setCellValue("B")
    sheet.cellAt(0, 2).setCellValue("C")
    area.write(model)(sheet)
    sheet
  }

  it should "check columns properly" in {
    val sheet = writeSheet
    assert(sheet.cellAt(1, 0).getStringCellValue === "X")
    assert(sheet.cellAt(1, 1).getCellType() === Cell.CELL_TYPE_BLANK)
    assert(sheet.cellAt(1, 2).getStringCellValue === "X")
  }
}
