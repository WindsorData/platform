package input

import org.scalatest.FlatSpec
import org.scalatest.FunSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import libt.spreadsheet.reader.CellReader
import libt.spreadsheet.Mapping
import org.apache.poi.ss.usermodel.Workbook
import libt.Model
import libt.util._
import libt.spreadsheet.reader.ColumnOrientedReader
import libt.spreadsheet.Mapping
import java.io.InputStream
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.usermodel.Row
import libt.spreadsheet.reader.RowOrientedReader
import org.apache.poi.ss.usermodel.Sheet
import util.FileManager
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import libt.TElement
import libt.TModel
import libt.spreadsheet.Feature
import libt.TString
import libt.Path
import libt.Value

@RunWith(classOf[JUnitRunner])
class WorkbookReaderSpec extends FunSpec {

  class WorkbookReader[A](schema: TModel, wbMapping: WorkbookMapping, combiner: Combiner[A]) {
    def read(in: InputStream): A = read(WorkbookFactory.create(in))
    def read(wb: Workbook): A = combiner.combineReadResult(wb, wbMapping.read(wb, schema))
  }

  case class WorkbookMapping(areas: Stream[SheetDefinition]) {
    def read(wb: Workbook, schema: TModel): Seq[Seq[Model]] = {
      val sheets = for (sheetIndex <- 0 to wb.getNumberOfSheets() - 1) yield wb.getSheetAt(sheetIndex)
      sheets.zip(areas).map { case (sheet, area) => area.read(sheet, schema) }
    }
  }

  trait Combiner[A] {
    def combineReadResult(wb: Workbook, results: Seq[Seq[Model]]): A
  }
  class MirrorCombiner extends Combiner[Seq[Seq[Model]]] {
    def combineReadResult(wb: Workbook, results: Seq[Seq[Model]]) = results
  }

  type Orientation = Seq[Row] => CellReader
  val RowOrientation: Orientation = new RowOrientedReader(_)
  val ColumnOrientation: Orientation = new ColumnOrientedReader(_)

  sealed trait SheetDefinition {
    def read(sheet: Sheet, schema: TModel): Seq[Model]
  }

  case class Area(initialPoint: (Int, Int), orientation: Orientation, mapper: Mapping) extends SheetDefinition {
    import libt.spreadsheet.util._
    import libt.spreadsheet.reader._

    def read(sheet: Sheet, schema: TModel): Seq[Model] = {
      takeArea(sheet)
      schema.read(mapper, sheet)
    }

    private def takeArea(sheet: Sheet) =
      umatch(initialPoint) {
        case (rowIndex, columnIndex) => {
          (0 to (rowIndex - 1)).foreach(index => sheet.removeRow(sheet.getRow(index)))
          (0 to (columnIndex - 1)).foreach(sheet.rows.map(_.cellIterator).map(_.next))
        }
      }

  }

  object Area {
    def toInfinite(initialPoint: (Int, Int), orientation: Orientation, mapper: Mapping) =
      Stream.continually[SheetDefinition](Area(initialPoint, orientation, mapper))
  }
  object Gap extends SheetDefinition {
    def read(sheet: Sheet, schema: TModel) = Nil
  }

  describe("WorkbookReader creation") {
    it("should be able to create empty WorkbookReader") {
      new WorkbookReader(TModel(), WorkbookMapping(Stream()), new MirrorCombiner)
    }

    it("should be able to create single sheet workbookreader") {
      new WorkbookReader(
        TModel(),
        WorkbookMapping(
          Stream(Area((0, 0), ColumnOrientation, Mapping()))),
        new MirrorCombiner)
    }

    it("should be able to create multiple sheets workbookreader") {
      new WorkbookReader(
        TModel(),
        WorkbookMapping(
          Area((0, 0), ColumnOrientation, Mapping())
            #::
            Stream.continually(Area((1, 1), RowOrientation, Mapping()))),
        new MirrorCombiner)
    }

    it("should be able to create multiple sheets workbookreader with gaps") {
      new WorkbookReader(
        TModel(),
        WorkbookMapping(
          Area((0, 0), ColumnOrientation, Mapping())
            #::
            Gap
            #::
            Gap
            #::
            Area.toInfinite((1, 1), RowOrientation, Mapping())),
        new MirrorCombiner)
    }
  }

  describe("WorkbookReader usage") {

    object WorkbookFactory {
      import libt.spreadsheet.util._
      def makeSingleValueWorkbook = {
        val wb = new HSSFWorkbook
        val sheet = wb.createSheet("foo")
        sheet.cellAt(0, 0).setCellValue("something")
        sheet.cellAt(1, 0).setAsActiveCell()
        sheet.cellAt(2, 0).setAsActiveCell()
        sheet.cellAt(3, 0).setAsActiveCell()
        sheet.cellAt(4, 0).setAsActiveCell()
        wb
      }

    }

    it("should be able to read a single input, on a single sheet workbook, and return results without combining") {
      val result = new WorkbookReader(
        TModel('a -> TString),
        WorkbookMapping(Stream(Area((0, 0), RowOrientation, Mapping(Feature(Path('a)))))),
        new MirrorCombiner).read(WorkbookFactory.makeSingleValueWorkbook)

      assert(result === Seq(Seq(Model('a -> Value("something")))))
    }
  }

}