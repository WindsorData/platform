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

@RunWith(classOf[JUnitRunner])
class WorkbookWriterSpec extends FunSpec with BeforeAndAfter {

  val wb = new HSSFWorkbook
  var sheet: Sheet = _

  before {
    wb.createSheet()
    sheet = wb.getSheetAt(0)
  }

  after {
    wb.removeSheetAt(0)
  }

  describe("WorkbookWriter usage") {

    ignore("should write Data with a single model") {
      val TSimpleSchema = TModel('ffoo -> TNumber, 'foo -> TModel('bar -> TString))
      val simpleModel = Model(
        'ffoo -> Value(2: BigDecimal),
        'foo -> Model(
          'bar -> Value("SimpleModel")))
      WorkbookMapping(Seq(
        FlattedArea(
          PK(),
          Path(),
          TSimpleSchema,
          Seq(Feature('ffoo),
            Feature('foo, 'bar))))).write(Seq(simpleModel), wb)
      assert(sheet.cellAt(0, 0).getNumericCellValue() === 2)
      assert(sheet.cellAt(0, 1).getStringCellValue() === "SimpleModel")
    }

    ignore("should write Data with multiple models") {
      val TSimpleSchema = TModel('ffoo -> TNumber, 'foo -> TModel('bar -> TString))
      val SimpleModel1 = Model('ffoo -> Value(1: BigDecimal), 'foo -> Model('bar -> Value("SimpleModel1")))
      val SimpleModel2 = Model('ffoo -> Value(2: BigDecimal), 'foo -> Model('bar -> Value("SimpleModel2")))
      val SimpleModel3 = Model('ffoo -> Value(3: BigDecimal), 'foo -> Model('bar -> Value("SimpleModel3")))

      WorkbookMapping(Seq(
        FlattedArea(
          PK(),
          Path(),
          TSimpleSchema,
          Seq(Feature('ffoo),
            Feature('foo, 'bar))))).write(Seq(SimpleModel1, SimpleModel2, SimpleModel3), wb)

//      wb.write(new FileOutputStream("test/input/lala.xlsx"))

      assert(sheet.cellAt(0, 0).getNumericCellValue() === 1)
      assert(sheet.cellAt(0, 1).getStringCellValue() === "SimpleModel1")

      assert(sheet.cellAt(1, 0).getNumericCellValue() === 2)
      assert(sheet.cellAt(1, 1).getStringCellValue() === "SimpleModel2")

      assert(sheet.cellAt(2, 0).getNumericCellValue() === 3)
      assert(sheet.cellAt(2, 1).getStringCellValue() === "SimpleModel3")
    }

    it("should write Data with flattened models") {
      val TSimpleSchema = TModel(
          'key1 -> TNumber, 
          'key2 -> TString,
          'values -> TCol(
              TModel(
                  'c -> TString, 
                  'd -> TEnum("foo", "bar"), 
                  'e -> TXBool)))
                  
      val nestedModels = Seq(
          Model(
              'c -> Value("someValue"),
              'd -> Value("foo"),
              'e -> Value(true)))
              
      val models = Seq(
          Model(
              'key1 -> Value(1000: BigDecimal),
              'key2 -> Value("value1"),
              'values -> Col(nestedModels:_*)),
          Model(
              'key1 -> Value(2000: BigDecimal),
              'key2 -> Value("value2"),
              'values -> Col(nestedModels:_*))
          )
              
       WorkbookMapping(Seq(
        FlattedArea(
          PK(Path('key1), Path('key2)),
          Path('values),
          TSimpleSchema,
          Seq(Feature('c), Feature('d), Feature('e)))))
          .write(models, wb)
    }
  }
}
