package input

import org.scalatest.FunSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.apache.poi.ss.usermodel.Workbook
import libt._
import model._
import libt.spreadsheet._
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import java.io.FileOutputStream


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

  describe("WorkbookReader creation") {
    it("should be able to create empty WorkbookReader") {
      new WorkbookReader(WorkbookMapping(Seq()), new IdentityCombiner)
    }

    it("should be able to create single sheet workbookreader") {
      new WorkbookReader(
        WorkbookMapping(
          Stream(Area(TModel(), Offset(0, 0), ColumnOrientation, Mapping()))),
        new IdentityCombiner)
    }

    it("should be able to create multiple sheets workbookreader") {
      new WorkbookReader(
        WorkbookMapping(
          Area(TModel(), Offset(0, 0), ColumnOrientation, Mapping())
            #::
            Stream.continually(Area(TModel(), Offset(1, 1), RowOrientation, Mapping()))),
        new IdentityCombiner)
    }

    it("should be able to create multiple sheets workbookreader with gaps") {
      new WorkbookReader(
        WorkbookMapping(
          Area(TModel(), Offset(0, 0), ColumnOrientation, Mapping())
            #::
            AreaGap
            #::
            AreaGap
            #::
            Area.toInfinite(TModel(), Offset(1, 1), RowOrientation, Mapping())),
        new IdentityCombiner)
    }
  }

  describe("WorkbookReader usage") {

    it("should be able to read a single column oriented input without combining") {
      import MyWorkbookFactory._
      val workbook = createNewSingleSheetWorkbook.addSingleColumnOrientedValue(2, "a")
      val reader = new WorkbookReader(
        WorkbookMapping(
          Seq(Area(TModel('a -> TString),Offset(0, 2), ColumnOrientation, Mapping(Feature(Path('a)))))),
        new IdentityCombiner)
      val result = reader.read(workbook)

      assert(result.head === Seq(Model('a -> Value("a"))))
    }

    it("should be able to read two column oriented inputs without combining") {
      import MyWorkbookFactory._
      val result = new WorkbookReader(
        WorkbookMapping(
          Seq(Area(TModel('a -> TString, 'b -> TString),Offset(0, 0), ColumnOrientation, Mapping(Feature(Path('a)), Feature(Path('b)))))),
        new IdentityCombiner)
        .read(createNewSingleSheetWorkbook
          .addSingleColumnOrientedValue(0, "a")
          .addSingleColumnOrientedValue(1, "b"))

      assert(result.head === Seq(Model('a -> Value("a"), 'b -> Value("b"))))
    }

    it("should be able to read row oriented inputs") {
      import MyWorkbookFactory._
      val workbook = createNewSingleSheetWorkbook.addSingleRowOrientedValue(1, "a")

      val result = new WorkbookReader(
        WorkbookMapping(
          Seq(Area(TModel('a -> TString), Offset(1, 0), RowOrientation, Mapping(Feature(Path('a)))))),
        new IdentityCombiner).read(workbook)

      import libt.spreadsheet.util._
      assert(result.head === Seq(Model('a -> Value("a"))))
    }

  }
  
  describe("An importer") {

    it("should be able to import an empty company fiscal year") {

      val results = new WorkbookReader(
        WorkbookMapping(
          Seq(Area(TCompanyFiscalYear, Offset(2, 2), RowOrientation,
            Mapping(Feature(Path('ticker)), Feature(Path('name)))))),
        new CompanyFiscalYearCombiner).read("test/input/CompanyValuesAndNotes.xlsx")

      assert(results === Seq())
    }

    it("should be able to import 3 company fiscal years with executives") {

      val results = CompanyFiscalYearReader.read("test/input/FullValuesOnly.xlsx")
      
      assert(results.size === 3)
      
      assert(results.head.v('ticker) === Value("something"))
      assert(results.head.v('name) === Value("something"))
      
      (0 to 2).zip(Seq(2005, 2012, 2013)).foreach{ case (index, year) => 
        validateCompanyYear(results(index), year)
        validateExecutive(results(index).c('executives).take(1).head.asInstanceOf[Model])
      }
    }
    
    def validateCompanyYear(company: Model, year: Int) {
      assert(company.v('disclosureFiscalYear) === Value(year))
      val firstExecFirstCompany = company.c('executives).take(1).head
      validateExecutive(firstExecFirstCompany.asInstanceOf[Model])
    }
    
    def validateExecutive(exec: Model) {
      import exec._

      assert(v('lastName) === Value("exec1Last"))
      assert(v('title) === Value())

      assert(m('functionalMatches).v('primary) === Value("Engineering"))
      assert(m('functionalMatches).v('secondary) === Value())
      assert(m('functionalMatches).v('level) === Value("SVP (Senior Vice President)"))
      assert(m('functionalMatches).v('scope) === Value("Asia"))
      assert(m('functionalMatches).v('bod) === Value())

      assert(m('cashCompensations).v('baseSalary) === Value(1000))
      assert(m('cashCompensations).v('actualBonus) === Value())
      assert(m('cashCompensations).v('targetBonus) === Value(0.1: BigDecimal))
      assert(m('cashCompensations).m('nextFiscalYearData).v('baseSalary) === Value(2000))
      assert(m('cashCompensations).m('nextFiscalYearData).v('targetBonus) === Value(0.5))

      assert(mc('optionGrants)(0).v('number) === Value(1))
      assert(mc('optionGrants)(0).v('price) === Value(2))
      assert(mc('optionGrants)(0).v('value) === Value(3))
      assert(mc('optionGrants)(0).v('perf) === Value(true))
      assert(mc('optionGrants)(0).v('type) === Value("Hire"))

      assert(mc('optionGrants)(1).v('perf) === Value(true))
      assert(mc('optionGrants)(2).v('perf) === Value(true))
      assert(mc('optionGrants)(3).v('perf) === Value(true))
      assert(mc('optionGrants)(4).v('perf) === Value(false))

      assert(mc('optionGrants)(5).v('number) === Value(1))
      assert(mc('optionGrants)(5).v('type) === Value("Annual"))

      assert(mc('timeVestRS)(0).v('number) === Value(100))
      assert(mc('timeVestRS)(0).v('price) === Value(200))
      assert(mc('timeVestRS)(0).v('value) === Value(300))
      assert(mc('timeVestRS)(0).v('type) === Value())

      assert(mc('timeVestRS)(5).v('type) === Value("Other"))

      assert(m('carriedInterest).m('ownedShares).v('beneficialOwnership) === Value(12))
      assert(m('carriedInterest).m('ownedShares).v('disclaimBeneficialOwnership) === Value(13))
      assert(m('carriedInterest).m('outstandingEquityAwards).v('unvestedOptions) === Value(14))
      assert(m('carriedInterest).m('outstandingEquityAwards).v('perfVestRS) === Value(15))
    }

    it("should throw IllegalArgumentException when there's an invalid functional value") {
      intercept[Throwable] {
        CompanyFiscalYearReader.read("test/input/InvalidFunctionalValue.xlsx").foreach(TCompanyFiscalYear.validate(_))
      }
    }

    it("should throw an Exception when there's a numeric value on string cell") {
      intercept[IllegalStateException] {
        CompanyFiscalYearReader.read("test/input/ExpectedStringButWasNumeric.xlsx")
      }
    }

    it("should throw an Exception when there's no value on any fiscal year") {
      intercept[IllegalStateException] {
        CompanyFiscalYearReader.read("test/input/EmptyFiscalYear.xlsx")
      }
    }

  }

}