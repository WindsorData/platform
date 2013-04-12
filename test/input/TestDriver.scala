//package input
//
//import org.scalatest.FunSpec
//import org.junit.runner.RunWith
//import org.scalatest.junit.JUnitRunner
//import org.apache.poi.ss.usermodel.WorkbookFactory
//import org.apache.poi.ss.usermodel.Workbook
//import org.apache.poi.hssf.usermodel.HSSFWorkbook
//import org.apache.poi.ss.util.CellUtil
//import org.apache.poi.ss.usermodel.Sheet
//import util.poi.Cells
//import model._
//import scala.collection.mutable.MapBuilder
//import scala.collection.mutable.{ Map => MutableMap }
//import org.apache.poi.ss.usermodel.Cell
//import com.mongodb.DBObject
//import libt.Value
//
//@RunWith(classOf[JUnitRunner])
//class TestDriver extends FunSpec {
//
//  
//  type DataItemBuilder = MutableMap[Symbol, Value[_]]
//  type DataItem = Map[Symbol, Value[_]]
//  
////  abstract class DataInput
////  case class SingleInput(input: Input[_]) extends DataInput
////  case class Inputs(inputs: Seq[DataItem]) extends DataInput
//
//  implicit def sheet2RichSheet(sheet: Sheet) = new {
//    def cellAt(rowIndex: Int, columnIndex: Int) =
//      CellUtil.getCell(CellUtil.getRow(rowIndex, sheet), columnIndex)
//
//    def rows = Cells.rows(sheet)
//  }
//
//  trait Column {
//    def read(reader: CellReader, dataItemBuilder: DataItemBuilder)
//    def write(writer: CellWriter, item: DataItem)
//  }
//
//  case class Feature[A](fieldName: Symbol, featureType: FeatureType[A]) extends Column {
//    def read(reader: CellReader, dataItemBuilder: DataItemBuilder) =
//      dataItemBuilder += (fieldName -> featureType.read(reader))
//    def write(writer: CellWriter, item: DataItem) =
//      featureType.write(writer, item.get(fieldName).get.asInstanceOf[Input[A]])
//  }
//
//  case object Gap extends Column {
//    override def read(reader: CellReader, dataItemBuilder: DataItemBuilder) =
//      reader.skip(1)
//    override def write(writer: CellWriter, item: DataItem) = writer.skip1
//  }
//
//  case class Mapper(columns: Seq[Column], width: Int = 10, heigth: Int = 10) {
//
//    def read(sheet: Sheet): Seq[DataItem] =
//      sheet.rows.grouped(6).map { inputGroup =>
//        val dataItemBuilder = MutableMap[Symbol, Input[_]]()
//        val reader = new ColumnOrientedReader(inputGroup)
//        for (column <- columns)
//          column.read(reader, dataItemBuilder)
//        dataItemBuilder.toMap
//      }.toSeq
//
//    def write(items: Seq[DataItem]): Sheet = {
//
//      def defineGrid = {
//        val sheet = new HSSFWorkbook createSheet ("foo")
//        for {
//          x <- 0 to width
//          y <- 0 to heigth
//        } sheet.cellAt(x, y).setAsActiveCell()
//        sheet
//      }
//
//      val wb = new HSSFWorkbook
//      val sheet = defineGrid
//      val writer = new CellWriter(sheet)
//      for {
//        item <- items
//        column <- columns
//      } column.write(writer, item)
//
//      writer.getSheet
//    }
//
//  }
//
//  trait FeatureType[A] {
//    def read(reader: CellReader): Input[A]
//    def readWithDefault(reader: CellReader, defaultValue: A): Input[A]
//    def write(writer: CellWriter, item: Input[A]): Unit
//  }
//
//  case object NumericType extends FeatureType[BigDecimal] {
//    def read(reader: CellReader) = reader.numeric
//    def readWithDefault(reader: CellReader, defaultValue: BigDecimal) = reader.numericWithDefault(defaultValue)
//    def write(writer: CellWriter, item: Input[BigDecimal]) = writer.numeric(item)
//  }
//
//  case object StringType extends FeatureType[String] {
//    def read(reader: CellReader) = reader.string
//    def readWithDefault(reader: CellReader, defaultValue: String) = reader.stringWithDefault(defaultValue)
//    def write(writer: CellWriter, item: Input[String]) = writer.string(item)
//  }
//  case class EnumType(values: Traversable[String]) extends FeatureType[String] {
//    //TODO: do validation
//    def validate(input: Input[String]) = {
//      if (input.value.isEmpty || values.exists(_ == input.value.get)) {
//        input
//      } else {
//        throw new IllegalArgumentException
//      }
//    }
//
//    def read(reader: CellReader) = validate(reader.string)
//
//    def readWithDefault(reader: CellReader, defaultValue: String) =
//      validate(reader.stringWithDefault(defaultValue))
//    def write(writer: CellWriter, item: Input[String]) = writer.string(item)
//  }
//
//  case class WithDefault[A](featureType: FeatureType[A], defaultValue: A) extends FeatureType[A] {
//    def read(reader: CellReader) = featureType.readWithDefault(reader, defaultValue)
//    def readWithDefault(reader: CellReader, defaultValue: A) = ???
//    def write(writer: CellWriter, item: Input[A]) = {
//       featureType.write(writer, if(item.value.get == defaultValue) None else item)
//    }
//  }
//
//  object DataItemMock {
//
//    def makeSingleDataItem[T](value: T, calc: String, comment: String, note: String, link: String) = {
//      val wb = new HSSFWorkbook
//      val sheet = wb.createSheet("foo")
//
//      //Hack to deal with different type of cell values
//      value match {
//        case v: Int => sheet.cellAt(0, 1).setCellValue(v)
//        case v: String => sheet.cellAt(0, 1).setCellValue(v)
//      }
//
//      sheet.cellAt(1, 1).setCellValue(calc)
//      sheet.cellAt(2, 1).setCellValue(comment)
//      sheet.cellAt(3, 1).setCellValue(note)
//      sheet.cellAt(4, 1).setCellValue(link)
//      sheet
//    }
//
//    def makeEmptyDataItem = {
//      val sheet = new HSSFWorkbook createSheet ("foo")
//      sheet.cellAt(0, 1).setAsActiveCell()
//      sheet.cellAt(1, 1).setAsActiveCell()
//      sheet.cellAt(2, 1).setAsActiveCell()
//      sheet.cellAt(3, 1).setAsActiveCell()
//      sheet.cellAt(4, 1).setAsActiveCell()
//      sheet
//    }
//  }
//
//  describe("mapper creation") {
//    it("should be able to create empty mappers") {
//      Mapper(Seq())
//    }
//
//    it("should be able to create mappers with a single Integer Feature") {
//      Mapper(Seq(Feature('a, NumericType)))
//    }
//
//    it("should be able to create mappers with a single Integer Feature with default value") {
//      Mapper(Seq(Feature('a, WithDefault(NumericType, 1: BigDecimal))))
//    }
//
//    it("should be able to create mappers with mixed type Features") {
//      Mapper(
//        Seq(Feature('a, WithDefault(NumericType, 1: BigDecimal)),
//          Feature('a, StringType)))
//    }
//
//    it("should be able to describe gaps") {
//      Mapper(
//        Seq(
//          Feature('a, WithDefault(NumericType, 1: BigDecimal)),
//          Gap,
//          Gap,
//          Feature('a, StringType)))
//    }
//
//    it("should be able to describe enumurated Features") {
//      Mapper(Seq(Feature('a, EnumType(Seq("foo", "bar")))))
//    }
//
//  }
//
//  describe("mapper for input parsing") {
//    it("should be able to parse valid files using an empty Feature definition") {
//      import DataItemMock._
//      val sheet = makeSingleDataItem(15, "calc", "comment", "note", "link")
//      val items: Seq[DataItem] = Mapper(Seq(Gap, Feature('aFieldName, NumericType))).read(sheet)
//      assert(items(0)('aFieldName) === Input(Some(15.0), Some("calc"), Some("comment"), Some("note"), Some("link")))
//    }
//
//    it("should be able to parse valid string cells") {
//      import DataItemMock._
//      val sheet = makeSingleDataItem("someValue", "calc", "comment", "note", "link")
//      val items: Seq[DataItem] = Mapper(Seq(Gap, Feature('someValidField, StringType))).read(sheet)
//      assert(items(0)('someValidField) === Input(Some("someValue"), Some("calc"), Some("comment"), Some("note"), Some("link")))
//    }
//
//    it("should be able to parse valid enum type fields") {
//      import DataItemMock._
//      val sheet = makeSingleDataItem("someValue", "as", "as", "as", "as")
//      val items: Seq[DataItem] =
//        Mapper(
//          Seq(
//            Gap,
//            Feature('someValidField,
//              EnumType(Traversable("someKindOfValue", "someValue", "blah"))))).read(sheet)
//    }
//
//    it("should refuse invalid enum type fields") {
//      intercept[IllegalArgumentException] {
//        import DataItemMock._
//        val sheet = makeSingleDataItem("invalidValue", "as", "as", "as", "as")
//        val items: Seq[DataItem] =
//          Mapper(Seq(
//            Gap,
//            Feature('someValidField,
//              EnumType(Traversable("someKindOfValue", "valid", "nana"))))).read(sheet)
//      }
//    }
//
//    it("should be able to take empty fields as valid enum type fields") {
//      import DataItemMock._
//      val sheet = makeEmptyDataItem
//      Mapper(
//        Seq(
//          Gap,
//          Feature('someValidField,
//            EnumType(Traversable("someKindOfValue", "someValue", "blah"))))).read(sheet)
//    }
//
//    it("should be able to parse valid files using a single Feature definition ") {
//      Mapper(Seq(Feature('a, NumericType)))
//    }
//
//    it("should be able to convert blank cells to None") {
//      import DataItemMock._
//      val sheet = makeEmptyDataItem
//      val items: Seq[DataItem] =
//        Mapper(
//          Seq(
//            Gap,
//            Feature('aField,
//              StringType))).read(sheet)
//      assert(items(0)('aField) === Input(None, None, None, None, None))
//    }
//
//    it("should be able to convert blank cells to a given default value") {
//      import DataItemMock._
//      val sheet = makeEmptyDataItem
//      val items: Seq[DataItem] =
//        Mapper(
//          Seq(
//            Gap,
//            Feature('aField,
//              WithDefault(StringType, "BLANK")))).read(sheet)
//      assert(items(0)('aField) === Input(Some("BLANK"), None, None, None, None))
//    }
//
//  }
//
//  describe("mapper for output marshalling") {
//    it("should write a single numeric value") {
//      val mapper = Mapper(Seq(Gap, Feature('aFieldName, NumericType)))
//      val sheetResult = mapper.write(Seq(Map('aFieldName -> Input(Some(15), Some("calc"), Some("comment"), Some("note"), Some("link")))))
//      assert(15 === sheetResult.cellAt(0, 1).toString.toInt)
//      assert("calc" === sheetResult.cellAt(1, 1).toString)
//      assert("comment" === sheetResult.cellAt(2, 1).toString)
//      assert("note" === sheetResult.cellAt(3, 1).toString)
//      assert("link" === sheetResult.cellAt(4, 1).toString)
//    }
//
//    it("should write multiple values") {
//      val mapper = Mapper(Seq(Gap, Feature('numericField, NumericType), Feature('stringField, StringType)))
//      val sheetResult = mapper.write(
//        Seq(
//          Map('numericField -> Input(15),
//            'stringField -> Input("lala"))))
//      assert(15 === sheetResult.cellAt(0, 1).toString.toInt)
//      assert("lala" === sheetResult.cellAt(0, 2).toString)
//    }
//    
//    it("should write blank cell for a numeric default value"){
//      val mapper = Mapper(Seq(Gap, Feature('numericField, WithDefault(NumericType, 0: BigDecimal))))
//      val sheetResult = mapper.write(Seq(Map('numericField -> Input(0))))
//      assert(sheetResult.cellAt(0, 1).getCellType === Cell.CELL_TYPE_BLANK)
//    }
//    
//    it("should write blank cell for a string default value"){
//      val mapper = Mapper(Seq(Gap, Feature('stringField, WithDefault(StringType, "BLANK"))))
//      val sheetResult = mapper.write(Seq(Map('stringField -> Input("BLANK"))))
//      assert(sheetResult.cellAt(0, 1).getCellType === Cell.CELL_TYPE_BLANK)
//    }
//  }
//
//}