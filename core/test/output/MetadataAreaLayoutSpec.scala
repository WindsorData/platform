package output

import org.scalatest.BeforeAndAfter
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import libt._
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Sheet
import libt.spreadsheet.reader.WorkbookMapping
import output._
import libt.spreadsheet._
import libt.spreadsheet.util._
import scala.math.BigDecimal.int2bigDecimal
import org.scalatest.junit.JUnitRunner

class MetadataAreaLayoutSpec extends FlatSpec with BeforeAndAfter {

  val TSchema = TModel(
    'key -> TNumber,
    'values -> TCol(
      TModel(
        'foo -> TModel('value -> TString),
        'bar -> TNumber)))

  val models = Seq(
    Model(
      'key -> Value(1000: BigDecimal),
      'values -> Col(
        Model(
          'foo ->
            Model('value ->
              Value(Some("someValue"), Some("value1"), Some("value2"), Some("value3"), Some("value4"))),
          'bar -> Value(Some(2000: BigDecimal), Some("value5"), Some("value6"), Some("value7"), Some("value8"))),
        Model(
          'foo ->
            Model('value ->
              Value(Some("otherValue"), Some("value9"), Some("value10"), Some("value11"), Some("value12"))),
          'bar -> Value(Some(2000: BigDecimal), Some("value13"), Some("value14"), Some("value15"), Some("value16"))))))

  val wb = new HSSFWorkbook
  var sheet: Sheet = _

  before {
    wb.createSheet()
    sheet = wb.getSheetAt(0)
    writeModel
  }

  after {
    wb.removeSheetAt(0)
  }

  def writeModel =
    WorkbookMapping(
      Seq(
        FlattedArea(
          PK(Path('key)),
          PK(Path('foo, 'value)),
          Path('values, *),
          TSchema,
          MetadataAreaLayout(Offset(0, 0)),
          Seq(
            Feature('foo, 'value),
            Feature('bar)))))
      .write(models, wb)

  it should "write keys on metadata" in {
    assert(sheet.cellAt(0, 0).getNumericCellValue === 1000)
    assert(sheet.cellAt(0, 1).getStringCellValue === "someValue")
    assert(sheet.cellAt(3, 0).getNumericCellValue === 1000)
    assert(sheet.cellAt(3, 1).getStringCellValue === "otherValue")
  }
  
  it should "write titles on metadata" in {
    assert(sheet.cellAt(0, 2).getStringCellValue === "Foo")
    assert(sheet.cellAt(0, 3).getStringCellValue === "Value")
    assert(sheet.cellAt(1, 3).getStringCellValue === "Bar")
  }
  
  it should "write metadata itself" in {
    assert(sheet.cellAt(0, 4).getStringCellValue === "value1")
    assert(sheet.cellAt(0, 5).getStringCellValue === "value2")
    assert(sheet.cellAt(0, 6).getStringCellValue === "value3")
    assert(sheet.cellAt(0, 7).getStringCellValue === "value4")
    
    assert(sheet.cellAt(3, 4).getStringCellValue === "value13")
    assert(sheet.cellAt(3, 5).getStringCellValue === "value14")
    assert(sheet.cellAt(3, 6).getStringCellValue === "value15")
    assert(sheet.cellAt(3, 7).getStringCellValue === "value16")
  }
  
}
