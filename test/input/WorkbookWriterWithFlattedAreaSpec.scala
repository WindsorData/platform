package input

import org.scalatest.FunSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Sheet
import libt.spreadsheet.reader._
import libt.spreadsheet._
import libt.spreadsheet.util._
import libt._
import org.scalatest.BeforeAndAfter
import java.io.FileOutputStream
import output._
import org.scalatest.FlatSpec

@RunWith(classOf[JUnitRunner])
class WorkbookWriterWithFlattedAreaSpec extends FlatSpec with BeforeAndAfter {

  val TSimpleSchema = TModel(
    'key1 -> TNumber,
    'key2 -> TString,
    'values -> TCol(
      TModel(
        'c -> TString,
        'd -> TEnum("foo", "bar"),
        'e -> TXBool)))

  val models = Seq(
    Model(
      'key1 -> Value(1000: BigDecimal),
      'key2 -> Value("value1"),
      'values -> Col(
        Model(
          'c -> Value("someValue"),
          'd -> Value("foo"),
          'e -> Value(true)),
        Model(
          'c -> Value("otherValue"),
          'd -> Value(),
          'e -> Value()))))

  val wb = new HSSFWorkbook
  var sheet: Sheet = _

  def writeModel = WorkbookMapping(
    Seq(
      FlattedArea(
        PK(Path('key1), Path('key2)),
        Path('values),
        TSimpleSchema,
        Seq(
          Feature('c),
          Feature('d),
          Feature('e)))))
    .write(models, wb)

  behavior of "workbook writer using a flatted area"

  before {
    wb.createSheet()
    sheet = wb.getSheetAt(0)
  }

  after {
    wb.removeSheetAt(0)
  }

  it should "keys" in {
    writeModel
    assert(sheet.cellAt(0, 0).getNumericCellValue() === 1000)
    assert(sheet.cellAt(0, 1).getStringCellValue() === "value1")
  }

  it should "write values of flattened models" in {
    writeModel
    assert(sheet.cellAt(0, 2).getStringCellValue() === "someValue")
    assert(sheet.cellAt(0, 3).getStringCellValue() === "foo")
    assert(sheet.cellAt(0, 4).getBooleanCellValue())
  }

  it should "write a row for each flatted model" in {
    writeModel
    assert(sheet.cellAt(0, 0).getNumericCellValue() === 1000)
    assert(sheet.cellAt(0, 2).getStringCellValue() === "someValue")
    assert(sheet.cellAt(1, 0).getNumericCellValue() === 1000)
    assert(sheet.cellAt(1, 2).getStringCellValue() === "otherValue")
  }

  it should "write a row for each root model and flattened model" in {
    fail("pending test")
  }

  it should "leave an empty row for the headers" in {
    fail("unimplemented")
  }
}
