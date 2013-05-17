package libt.spreadsheet.reader

import org.scalatest.FunSpec
import org.junit.runner.RunWith
import org.apache.poi.ss.usermodel.Workbook
import libt._
import model._
import model.mapping._
import libt.spreadsheet._
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import libt.spreadsheet.reader._
import libt.spreadsheet.Offset
import libt.spreadsheet.util.sheet2RichSheet
import scala.collection.immutable.Stream.consWrapper
import org.scalatest.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class WorkbookReaderSpec extends FunSpec {

  object MyWorkbookFactory {
    import libt.spreadsheet.util._

    implicit def workbook2RichWorkbook(wb: Workbook) = new {

      def addSingleColumnOrientedValue(colIndex: Int, value: String = null) = {
        val sheet = wb.getSheetAt(0)

        if (value == null)
          sheet.cellAt(0, colIndex)
        else
          sheet.cellAt(0, colIndex).setCellValue(value)

        (1 to 4).foreach(sheet.cellAt(_, colIndex).setAsActiveCell())
        wb
      }

      def addSingleRowOrientedValue(rowIndex: Int, value: String = null) = {
        val sheet = wb.getSheetAt(0)

        if (value == null)
          sheet.cellAt(rowIndex, 0).setAsActiveCell()
        else
          sheet.cellAt(rowIndex, 0).setCellValue(value)

        (1 to 2).foreach(sheet.cellAt(rowIndex, _).setAsActiveCell())
        wb
      }
    }

    def createNewSingleSheetWorkbook = {
      val wb = new HSSFWorkbook
      wb.createSheet()
      wb
    }

  }

  class IdentityCombiner extends Combiner[Seq[Seq[ModelOrErrors]]] {
    def combineReadResult(wb: Workbook, results: Seq[Seq[ModelOrErrors]]) = results
  }

  describe("WorkbookReader creation") {
    it("should be able to create empty WorkbookReader") {
      new WorkbookReader(WorkbookMapping(Seq()), new IdentityCombiner)
    }

    it("should be able to create single sheet workbookreader") {
      new WorkbookReader(
        WorkbookMapping(
          Seq(Area(TModel(), Offset(0, 0), None, ColumnOrientedLayout, Seq()))),
        new IdentityCombiner)
    }

    it("should be able to create multiple sheets workbookreader") {
      new WorkbookReader(
        WorkbookMapping(
          Area(TModel(), Offset(0, 0), None, ColumnOrientedLayout, Seq())
            #::
            Stream.continually(Area(TModel(), Offset(1, 1), None, RowOrientedLayout, Seq()))),
        new IdentityCombiner)
    }

    it("should be able to create multiple sheets workbookreader with gaps") {
      new WorkbookReader(
        WorkbookMapping(
          Area(TModel(), Offset(0, 0), None, ColumnOrientedLayout, Seq())
            #::
            AreaGap
            #::
            AreaGap
            #::
            Area(TModel(), Offset(1, 1), None, RowOrientedLayout, Seq()).continually),
        new IdentityCombiner())
    }
  }

  describe("WorkbookReader usage") {

    it("should be able to read a single column oriented input without combining") {
      import MyWorkbookFactory._
      val workbook = createNewSingleSheetWorkbook.addSingleColumnOrientedValue(2, "a")
      val reader = new WorkbookReader(
        WorkbookMapping(
          Seq(Area(TModel('a -> TString),Offset(0, 2), None, ColumnOrientedLayout, Seq(Feature(Path('a)))))),
        new IdentityCombiner)
      val result = reader.read(workbook)

      assert(result.head === Seq(Right(Model('a -> Value("a")))))
    }

    it("should be able to read two column oriented inputs without combining") {
      import MyWorkbookFactory._
      val result = new WorkbookReader(
        WorkbookMapping(
          Seq(Area(TModel('a -> TString, 'b -> TString),Offset(0, 0), None, ColumnOrientedLayout, Seq(Feature(Path('a)), Feature(Path('b)))))),
        new IdentityCombiner)
        .read(createNewSingleSheetWorkbook
          .addSingleColumnOrientedValue(0, "a")
          .addSingleColumnOrientedValue(1, "b"))

      assert(result.head === Seq(Right(Model('a -> Value("a"), 'b -> Value("b")))))
    }

    it("should be able to read row oriented inputs") {
      import MyWorkbookFactory._
      val workbook = createNewSingleSheetWorkbook.addSingleRowOrientedValue(1, "a")

      val result = new WorkbookReader(
        WorkbookMapping(
          Seq(Area(TModel('a -> TString), Offset(1, 0), None, RowOrientedLayout, Seq(Feature(Path('a)))))),
        new IdentityCombiner).read(workbook)

      import libt.spreadsheet.util._
      assert(result.head === Seq(Right(Model('a -> Value("a")))))
    }

  }
  
 

}