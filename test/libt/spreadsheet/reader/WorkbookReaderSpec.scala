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
          Seq(Area(TModel(), Offset(0, 0), ColumnOrientedLayout, Seq()))),
        new IdentityCombiner)
    }

    it("should be able to create multiple sheets workbookreader") {
      new WorkbookReader(
        WorkbookMapping(
          Area(TModel(), Offset(0, 0), ColumnOrientedLayout, Seq())
            #::
            Stream.continually(Area(TModel(), Offset(1, 1), RowOrientedLayout, Seq()))),
        new IdentityCombiner)
    }

    it("should be able to create multiple sheets workbookreader with gaps") {
      new WorkbookReader(
        WorkbookMapping(
          Area(TModel(), Offset(0, 0), ColumnOrientedLayout, Seq())
            #::
            AreaGap
            #::
            AreaGap
            #::
            Area(TModel(), Offset(1, 1), RowOrientedLayout, Seq()).continually),
        new IdentityCombiner())
    }
  }

  describe("WorkbookReader usage") {

    it("should be able to read a single column oriented input without combining") {
      import MyWorkbookFactory._
      val workbook = createNewSingleSheetWorkbook.addSingleColumnOrientedValue(2, "a")
      val reader = new WorkbookReader(
        WorkbookMapping(
          Seq(Area(TModel('a -> TString),Offset(0, 2), ColumnOrientedLayout, Seq(Feature(Path('a)))))),
        new IdentityCombiner)
      val result = reader.read(workbook)

      assert(result.head === Seq(Right(Model('a -> Value("a")))))
    }

    it("should be able to read two column oriented inputs without combining") {
      import MyWorkbookFactory._
      val result = new WorkbookReader(
        WorkbookMapping(
          Seq(Area(TModel('a -> TString, 'b -> TString),Offset(0, 0), ColumnOrientedLayout, Seq(Feature(Path('a)), Feature(Path('b)))))),
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
          Seq(Area(TModel('a -> TString), Offset(1, 0), RowOrientedLayout, Seq(Feature(Path('a)))))),
        new IdentityCombiner).read(workbook)

      import libt.spreadsheet.util._
      assert(result.head === Seq(Right(Model('a -> Value("a")))))
    }

  }
  
 

}