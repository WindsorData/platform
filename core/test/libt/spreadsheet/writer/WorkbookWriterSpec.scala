package libt.spreadsheet.writer

import org.scalatest.{FunSpec, BeforeAndAfter, FlatSpec}
import libt.spreadsheet.reader._
import libt._
import libt.spreadsheet._
import libt.spreadsheet.util._
import libt.spreadsheet.reader.WorkbookMapping
import libt.TModel
import libt.spreadsheet.Offset
import libt.spreadsheet.reader.Area
import libt.spreadsheet.reader.ColumnOrientedLayout
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.{Sheet, Workbook}

class WorkbookWriterSpec extends FunSpec with BeforeAndAfter {

  var wb: Workbook = _
  var schema: TModel = _
  var columns: Seq[Strip] = _
  var mapping: WorkbookMapping = _

  def createMapping(areas: SheetDefinition*) =
    WorkbookMapping(areas)

  val fullModels =
    Seq(
      Model(
        'foo -> Value("bar"),
        'bar -> Value(1)))

  def write(models: Seq[Model] = fullModels) =
    mapping.write(models, wb)

  before {
    wb = new HSSFWorkbook
    schema = TModel('foo -> TString, 'bar-> TInt)
    columns = Seq(Feature(Path('foo)), Feature(Path('bar)))
  }

  describe("Standard Area as a Writer") {

    def createStandardArea =
      Area(
        schema,
        Offset(0, 0),
        None,
        ColumnOrientedLayout(RawValueReader),
        columns)

    describe("with a single sheet") {

      it("should write a single row with raw values") {
        wb.createSheet()
        mapping = createMapping(createStandardArea)
        write()
        assert(wb.getSheetAt(0).cellAt(0,0).getStringCellValue === "bar")
        assert(wb.getSheetAt(0).cellAt(0,1).getNumericCellValue === 1)
      }
    }

    describe("with multiple sheets") {

      it("should write a single row with raw values") {
        wb.createSheet()
        wb.createSheet()
        mapping = createMapping(createStandardArea, createStandardArea)
        write()

        assert(wb.getSheetAt(0).cellAt(0,0).getStringCellValue === "bar")
        assert(wb.getSheetAt(0).cellAt(0,1).getNumericCellValue === 1)
        assert(wb.getSheetAt(1).cellAt(0,0).getStringCellValue === "bar")
        assert(wb.getSheetAt(1).cellAt(0,1).getNumericCellValue === 1)
      }
    }

  }

  describe("Selective Area as a Writer") {

    def createSelectiveArea(writeStrategy: WriteStrategy = FullWriteStrategy) =
      CustomWriteArea(
        schema,
        Offset(0, 0),
        None,
        ColumnOrientedLayout(RawValueReader),
        columns,
        writeStrategy)

      describe("using Full Write Strategy") {
        it("should write full values on each sheet") {
          wb.createSheet()
          wb.createSheet()
          mapping = createMapping(createSelectiveArea(), createSelectiveArea())
          write()
          assert(wb.getSheetAt(0).cellAt(0,0).getStringCellValue === "bar")
          assert(wb.getSheetAt(0).cellAt(0,1).getNumericCellValue === 1)
          assert(wb.getSheetAt(1).cellAt(0,0).getStringCellValue === "bar")
          assert(wb.getSheetAt(1).cellAt(0,1).getNumericCellValue === 1)
        }
      }

      describe("using Custom Write Strategy") {
        val key = 'customKey

        object JustStandardValuesWriteStrategy extends WriteStrategy {
          def write(models: Seq[Model], area: CustomWriteSheetDefinition, sheet: Sheet) =
            area.customWrite(models.filterNot(_.contains(key)), sheet)
        }
        object CustomWriteStrategy extends WriteStrategy {
          def write(models: Seq[Model], area: CustomWriteSheetDefinition, sheet: Sheet) =
            area.customWrite(
              (models.find(_.contains(key)).get / key).asCol.elements.map(_.asModel), sheet)
        }

        def setupAndWrite = {
          wb.createSheet()
          wb.createSheet()
          mapping = createMapping(
            createSelectiveArea(JustStandardValuesWriteStrategy),
            createSelectiveArea(CustomWriteStrategy))

          write(fullModels :+ Model('customKey -> Col(Model('foo -> Value("custom"), 'bar -> Value(10)))))
        }

        it("should write full values on the first sheet") {
          setupAndWrite
          assert(wb.getSheetAt(0).cellAt(0,0).getStringCellValue === "bar")
          assert(wb.getSheetAt(0).cellAt(0,1).getNumericCellValue === 1)
        }

        it("should write specific values on the last sheet") {
          setupAndWrite
          assert(wb.getSheetAt(1).cellAt(0,0).getStringCellValue === "custom")
          assert(wb.getSheetAt(1).cellAt(0,1).getNumericCellValue === 10)
        }
      }
    }

}
