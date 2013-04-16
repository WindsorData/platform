package input

import org.scalatest.FunSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.util.CellUtil
import org.apache.poi.ss.usermodel.Sheet
import model._
import scala.collection.mutable.MapBuilder
import org.apache.poi.ss.usermodel.Cell
import com.mongodb.DBObject
import libt.Value
import libt.Model
import libt.spreadsheet.reader.CellReader
import libt.spreadsheet.util._
import libt.spreadsheet.reader._
import libt.spreadsheet._
import scala.collection.mutable.Buffer
import libt.TModel
import libt.TModel
import libt.TString
import libt.Element
import libt.TElement
import libt.Element
import libt.Col
import libt.Path
import libt.TValue
import libt.TNumber
import libt.TEnum
import libt.Route
import libt.Index
import libt.builder.ModelBuilder
import libt.TWithDefault
import libt.TCol

@RunWith(classOf[JUnitRunner])
class TestDriver extends FunSpec {

  //==================TEST UTILS====================

  object WorkBookFactory {

    def makeSingleDataItem[T](value: T, calc: String, comment: String, note: String, link: String) = {
      val wb = new HSSFWorkbook
      val sheet = wb.createSheet("foo")

      //Hack to deal with different type of cell values
      value match {
        case v: Int => sheet.cellAt(0, 1).setCellValue(v)
        case v: String => sheet.cellAt(0, 1).setCellValue(v)
      }

      sheet.cellAt(1, 1).setCellValue(calc)
      sheet.cellAt(2, 1).setCellValue(comment)
      sheet.cellAt(3, 1).setCellValue(note)
      sheet.cellAt(4, 1).setCellValue(link)
      sheet
    }
    
    def makeSingleColOfModels = {
      val sheet = new HSSFWorkbook createSheet ("foo")
      
      for (x <- 0 to 7; y <- 0 to 4)
        sheet.cellAt(y, x).setAsActiveCell()
      
      sheet.cellAt(0, 0).setCellValue("model1-value1")
      sheet.cellAt(0, 1).setCellValue("model1-value2")
      sheet.cellAt(0, 2).setCellValue("model1-value3")
      sheet.cellAt(0, 3).setCellValue("model1-value4")
      
      sheet.cellAt(0, 4).setCellValue("model2-value1")
      sheet.cellAt(0, 5).setCellValue("model2-value2")
      sheet.cellAt(0, 6).setCellValue("model2-value3")
      sheet.cellAt(0, 7).setCellValue("model2-value4")
      sheet
    }

    def makeEmptyDataItem = {
      val sheet = new HSSFWorkbook createSheet ("foo")
      sheet.cellAt(0, 1).setAsActiveCell()
      sheet.cellAt(1, 1).setAsActiveCell()
      sheet.cellAt(2, 1).setAsActiveCell()
      sheet.cellAt(3, 1).setAsActiveCell()
      sheet.cellAt(4, 1).setAsActiveCell()
      sheet
    }
  }

  //==================TEST======================

  describe("reader usage") {
    it("should let read empty sheets using empty mappings ") {
      val schema = TModel()
      val sheet: Sheet = WorkBookFactory.makeEmptyDataItem
      val mapping = Mapping()
      val result: Seq[Model] = schema.read(mapping, sheet)
      assert(result === Seq(Model()))
    }

    it("should let read empty sheets using non-empty mappings ") {
      val schema = TModel('foo -> TString)
      val sheet: Sheet = WorkBookFactory.makeEmptyDataItem
      val mapping = Mapping(Feature(Path('foo)))
      val result = schema.read(mapping, sheet)
      assert(result === Seq(Model('foo -> Value())))
    }

    it("should let read non-empty sheets using non-empty mappings ") {
      val schema = TModel('foo -> TString)
      val sheet: Sheet = WorkBookFactory.makeSingleDataItem("value", "calc", "comment", "not", "link")
      val mapping = Mapping(Gap, Feature(Path('foo)))
      val result = schema.read(mapping, sheet)
      assert(result === Seq(Model('foo ->
        Value(
          Some("value"),
          Some("calc"),
          Some("comment"),
          Some("not"),
          Some("link")))))
    }

    it("should let read sheets with enum values") {
      val schema = TModel('foo -> TEnum("foo", "value"))
      val sheet: Sheet = WorkBookFactory.makeSingleDataItem("foo", "calc", "comment", "not", "link")
      val mapping = Mapping(Gap, Feature(Path('foo)))
      schema.read(mapping, sheet)
    }
  }

  describe("mapper creation") {

    it("should be able to create empty mappers") {
      Mapping()
    }

    it("should be able to create mappers with a single Integer Feature") {
      Mapping(Feature(Path('a)))
    }

    it("should be able to describe gaps") {
      Mapping(
        Feature(Path('a)),
        Gap,
        Gap)
    }
  }

  describe("mapper for input parsing") {
    import WorkBookFactory._

    it("should be able to convert blank cells to None") {
      val sheet = makeEmptyDataItem
      val schema = TModel('aField -> TString)
      val mapping = Mapping(
        Gap,
        Feature(Path('aField)))

      val result = schema.read(mapping, sheet)
      assert(result === Seq(Model('aField -> Value())))

    }
    
    it("should be able to parse Cols") {
      val sheet = makeSingleColOfModels
      val schema = TModel('models -> TCol(TModel(
          'value1 -> TString,
          'value2 -> TString,
          'value3 -> TString,
          'value4 -> TString
          )))
          
      val mapping = Mapping(
          Feature(Path('models, 0, 'value1)),
          Feature(Path('models, 0, 'value2)),
          Feature(Path('models, 0, 'value3)),
          Feature(Path('models, 0, 'value4)),
          Feature(Path('models, 1, 'value1)),
          Feature(Path('models, 1, 'value2)),
          Feature(Path('models, 1, 'value3)),
          Feature(Path('models, 1, 'value4))
          )
          
      val result = schema.read(mapping, sheet)
      assert(result === Seq(Model('models -> Col(
          Model(
          'value1 -> Value("model1-value1"),
          'value2 -> Value("model1-value2"),
          'value3 -> Value("model1-value3"),
          'value4 -> Value("model1-value4")
              ),
          Model(
          'value1 -> Value("model2-value1"),
          'value2 -> Value("model2-value2"),
          'value3 -> Value("model2-value3"),
          'value4 -> Value("model2-value4")
              )    
              
          ))))
    }

    it("should be able to convert blank cells to a given default value") {
      val sheet = makeEmptyDataItem
      val schema = TModel('aField -> TWithDefault(TString, "BLANK"))
      val mapping = Mapping(
          Gap,
          Feature(Path('aField)))
      val result = schema.read(mapping, sheet)
      assert(result === Seq(Model('aField -> Value("BLANK"))))
    }
  }

  //  describe("mapper for output marshalling") {
  //    it("should write a single numeric value") {
  //      val mapper = Mapping(Seq(Gap, Feature('aFieldName, NumericType)))
  //      val sheetResult = mapper.write(Seq(Map('aFieldName -> Value(Some(15), Some("calc"), Some("comment"), Some("note"), Some("link")))))
  //      assert(15 === sheetResult.cellAt(0, 1).toString.toInt)
  //      assert("calc" === sheetResult.cellAt(1, 1).toString)
  //      assert("comment" === sheetResult.cellAt(2, 1).toString)
  //      assert("note" === sheetResult.cellAt(3, 1).toString)
  //      assert("link" === sheetResult.cellAt(4, 1).toString)
  //    }
  //
  //    it("should write multiple values") {
  //      val mapper = Mapping(Seq(Gap, Feature('numericField, NumericType), Feature('stringField, StringType)))
  //      val sheetResult = mapper.write(
  //        Seq(
  //          Map('numericField -> Value(15),
  //            'stringField -> Value("lala"))))
  //      assert(15 === sheetResult.cellAt(0, 1).toString.toInt)
  //      assert("lala" === sheetResult.cellAt(0, 2).toString)
  //    }
  //
  //    it("should write blank cell for a numeric default value") {
  //      val mapper = Mapping(Seq(Gap, Feature('numericField, WithDefault(NumericType, 0: BigDecimal))))
  //      val sheetResult = mapper.write(Seq(Map('numericField -> Value(0))))
  //      assert(sheetResult.cellAt(0, 1).getCellType === Cell.CELL_TYPE_BLANK)
  //    }
  //
  //    it("should write blank cell for a string default value") {
  //      val mapper = Mapping(Seq(Gap, Feature('stringField, WithDefault(StringType, "BLANK"))))
  //      val sheetResult = mapper.write(Seq(Map('stringField -> Value("BLANK"))))
  //      assert(sheetResult.cellAt(0, 1).getCellType === Cell.CELL_TYPE_BLANK)
  //    }
  //  }
}