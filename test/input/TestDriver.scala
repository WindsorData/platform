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
import libt.export.spreadsheet.CellReader
import libt.export.spreadsheet.util._
import libt.export.spreadsheet.ColumnOrientedReader
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

@RunWith(classOf[JUnitRunner])
class TestDriver extends FunSpec {

//  type DataItemBuilder = Buffer[(Symbol, Element)]
  type DataItem = Model
  //  abstract class DataValue
  //  case class SingleValue(input: Value[_]) extends DataValue
  //  case class Values(inputs: Seq[DataItem]) extends DataValue
  
  implicit def tValue2FeatureReader(tValue : TValue) : FeatureReader[_] = tValue match {
    case TString => StringReader
    case TNumber => NumericReader
    case e : TEnum => EnumReader(e) 
  } 
    
    
  trait FeatureReader[A] {
    def read(reader: CellReader): Value[A]
    def readWithDefault(reader: CellReader, defaultValue: A): Value[A]
  }

  case object NumericReader extends FeatureReader[BigDecimal] {
    def read(reader: CellReader) = reader.numeric
    def readWithDefault(reader: CellReader, defaultValue: BigDecimal) = reader.numericWithDefault(defaultValue)
  }

  case object StringReader extends FeatureReader[String] {
    def read(reader: CellReader) = reader.string
    def readWithDefault(reader: CellReader, defaultValue: String) = reader.stringWithDefault(defaultValue)
  }
  case class EnumReader(enum: TEnum) extends FeatureReader[String] {
//    //TODO: do validation
//    def validate(input: Value[String]) = {
//      if (input.value.isEmpty || enum.exists(_ == input.value.get)) {
//        input
//      } else {
//        throw new IllegalArgumentException
//      }
//    }

    def read(reader: CellReader) = reader.string

    def readWithDefault(reader: CellReader, defaultValue: String) =
      reader.stringWithDefault(defaultValue)
  }

  case class WithDefault[A](featureType: FeatureReader[A], defaultValue: A) extends FeatureReader[A] {
    def read(reader: CellReader) = featureType.readWithDefault(reader, defaultValue)
    def readWithDefault(reader: CellReader, defaultValue: A) = ???
  }
 
  
  case class Mapping(columns: Column*)
  
  trait Column {
    def read(reader: CellReader, schema : TElement, modelBuilder: ModelBuilder)
  }

  case class Feature(path: Path) extends Column {
    def read(reader: CellReader, schema : TElement, modelBuilder: ModelBuilder) = 
		modelBuilder += (path -> readValue(schema, reader))
		
     private def readValue(schema: TElement, reader: CellReader) = schema(path).read(reader) 
  }

  case object Gap extends Column {
    override def read(reader: CellReader, schema : TElement, modelBuilder: ModelBuilder) =
      reader.skip(1)
  }
  
  class TReader(
      mapping: Mapping, 
      schema: TElement, 
      width: Int = 10, 
      heigth: Int = 10) {
    def read(sheet: Sheet): Seq[Model] = 
      sheet.rows.grouped(6).map { inputGroup =>
        val modelBuilder = new ModelBuilder()
        val reader = new ColumnOrientedReader(inputGroup)
        
        for (column <- mapping.columns)
          column.read(reader, schema, modelBuilder)
          
        modelBuilder.build
      }.toSeq
  }
  
  
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
  
  
  
  implicit def tModel2Reader(schema:TModel) = new {
    def read(mapping: Mapping, sheet: Sheet) = new TReader(mapping, schema).read(sheet)
  }

  //==================TEST======================

  describe("reader usage") {
    it("should let read empty sheets using empty mappings ") {
      val schema = TModel()
      val sheet: Sheet = WorkBookFactory.makeEmptyDataItem
      val mapping = Mapping()
      val result : Seq[Model] = schema.read(mapping, sheet)
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
      val mapping = Mapping(Feature(Path('foo)))
      val result = schema.read(mapping, sheet)
      assert(result === Seq(Model('foo ->
        Value(
          Some("value"),
          Some("calc"),
          Some("comment"),
          Some("not"),
          Some("link")))))
    }
  }
   

  describe("mapper creation") {

    it("should be able to create empty mappers") {
      Mapping()
    }

//    it("should be able to create mappers with a single Integer Feature") {
//      Mapping(Seq(Feature('a, NumericType)))
//    }
//
//    it("should be able to create mappers with a single Integer Feature with default value") {
//      Mapping(Seq(Feature('a, WithDefault(NumericType, 1: BigDecimal))))
//    }
//
//    it("should be able to create mappers with mixed type Features") {
//      Mapping(
//        Seq(Feature('a, WithDefault(NumericType, 1: BigDecimal)),
//          Feature('a, StringType)))
//    }
//
//    it("should be able to describe gaps") {
//      Mapping(
//        Seq(
//          Feature('a, WithDefault(NumericType, 1: BigDecimal)),
//          Gap,
//          Gap,
//          Feature('a, StringType)))
//    }
//
//    it("should be able to describe enumurated Features") {
//      Mapping(Seq(Feature('a, EnumType(Seq("foo", "bar")))))
//    }

  }

  describe("mapper for input parsing") {
    import WorkBookFactory._
//    it("should be able to parse valid files using an empty Feature definition") {
//      val sheet = makeSingleDataItem(15, "calc", "comment", "note", "link")
//      val items: Seq[DataItem] = Mapping(Seq(Gap, Feature('aFieldName, NumericType))).read(sheet)
//      assert(items(0)('aFieldName) === Value(Some(15.0), Some("calc"), Some("comment"), Some("note"), Some("link")))
//    }
//
//    it("should be able to parse valid string cells") {
//      val sheet = makeSingleDataItem("someValue", "calc", "comment", "note", "link")
//      val items: Seq[DataItem] = Mapping(Seq(Gap, Feature('someValidField, StringType))).read(sheet)
//      assert(items(0)('someValidField) === Value(Some("someValue"), Some("calc"), Some("comment"), Some("note"), Some("link")))
//    }
//
//    it("should be able to parse valid enum type fields") {
//      val sheet = makeSingleDataItem("someValue", "as", "as", "as", "as")
//      val items: Seq[DataItem] =
//        Mapping(
//          Seq(
//            Gap,
//            Feature('someValidField,
//              EnumType(Traversable("someKindOfValue", "someValue", "blah"))))).read(sheet)
//    }
//
//    it("should refuse invalid enum type fields") {
//      intercept[IllegalArgumentException] {
//        val sheet = makeSingleDataItem("invalidValue", "as", "as", "as", "as")
//        val items: Seq[DataItem] =
//          Mapping(Seq(
//            Gap,
//            Feature('someValidField,
//              EnumType(Traversable("someKindOfValue", "valid", "nana"))))).read(sheet)
//      }
//    }
//
//    it("should be able to take empty fields as valid enum type fields") {
//      val sheet = makeEmptyDataItem
//      Mapping(
//        Seq(
//          Gap,
//          Feature('someValidField,
//            EnumType(Traversable("someKindOfValue", "someValue", "blah"))))).read(sheet)
//    }
//
//    it("should be able to parse valid files using a single Feature definition ") {
//      Mapping(Seq(Feature('a, NumericType)))
//    }

    it("should be able to convert blank cells to None") {
      val sheet = makeEmptyDataItem
      val schema = TModel('aField -> TString)
      val mapping  = Mapping(
            Gap,
            Feature(Path('aField)))
      
      val result = schema.read(mapping, sheet)
      assert(result === Model('aField -> Value()))
      
    }

//    it("should be able to convert blank cells to a given default value") {
//	  val sheet = makeEmptyDataItem
//      val schema = TModel('aField -> WithDefault(TString, "BLANK"))
//      val mapping  =
//        Mapping(
//          Seq(
//            Gap,
//            Feature(Path('aField))))
//      
//      val result : Element = schema.read(mapping, sheet)
//      assert(result === Model('aField -> Value("BLANK")))
//    }
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