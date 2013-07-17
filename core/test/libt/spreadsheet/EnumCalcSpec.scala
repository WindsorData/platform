package libt.spreadsheet

import org.scalatest.FlatSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import libt.spreadsheet.util._
import libt.spreadsheet._
import libt._
import libt.spreadsheet.reader._
import libt.builder.ModelBuilder
import libt.spreadsheet.writer._
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import libt.reduction._
import java.io.FileOutputStream
import libt.TModel
import libt.spreadsheet.Offset
import libt.spreadsheet.ComplexEnumCheck
import libt.spreadsheet.EnumCheck
import libt.TCol
import libt.Col
import libt.spreadsheet.reader.Area
import libt.spreadsheet.reader.ColumnOrientedLayout

class EnumCalcSpec extends FlatSpec {

  val TOptions = TStringEnum("A", "B", "C")
  var mapping: Seq[Strip] = _
  val model = Seq(
    Model('foo -> Model(
      'bar ->
        Col(
          Model(
            'a -> Value(1: BigDecimal),
            'b -> Value("A")),
          Model(
            'a -> Value(2: BigDecimal),
            'b -> Value("C"))))),

    Model('foo -> Model(
      'bar ->
        Col(
          Model(
            'a -> Value(3: BigDecimal),
            'b -> Value("A")),
          Model(
            'a -> Value(4: BigDecimal),
            'b -> Value("B"))))))

  def area = Area(
    TModel(
      'foo -> TModel(
        'bar -> TCol(TModel(
            'a -> TNumber,
            'b -> TOptions)))),
    Offset(1, 0),
    None,
    ColumnOrientedLayout(WithMetadataValueReader),
    mapping)

  def writeSheet = {
    val wb = new HSSFWorkbook()
    val sheet = wb.createSheet()
    area.write(model)(sheet)
    sheet
  }

  it should "check simple TEnum" in {
    mapping = Seq(
      EnumCheck(Path('foo, 'bar, *, 'b), "A"),
      EnumCheck(Path('foo, 'bar, *, 'b), "B"),
      EnumCheck(Path('foo, 'bar, *, 'b), "C"))
    val sheet = writeSheet
    assert(sheet.cellAt(1, 0).getStringCellValue === "X")
    assert(sheet.cellAt(1, 1).getCellType() === Cell.CELL_TYPE_BLANK)
    assert(sheet.cellAt(1, 2).getStringCellValue === "X")
  }

  it should "check complex TEnum based schema" in {
	mapping = Seq(
      ComplexEnumCheck(Path('foo, 'bar, *), Path('b), Path('a), "A"),
      ComplexEnumCheck(Path('foo, 'bar, *), Path('b), Path('a), "B"),
      ComplexEnumCheck(Path('foo, 'bar, *), Path('b), Path('a), "C"))
    val sheet = writeSheet
    assert(sheet.cellAt(1, 0).getNumericCellValue === 1)
    assert(sheet.cellAt(1, 1).getCellType() === Cell.CELL_TYPE_BLANK)
    assert(sheet.cellAt(1, 2).getNumericCellValue === 2)
  }
}
