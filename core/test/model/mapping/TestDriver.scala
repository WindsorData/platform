package model.mapping

import util.FileManager._
import org.scalatest.FunSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import libt.error._
import libt._
import model._
import libt.spreadsheet._
import libt.spreadsheet.reader._
import model.mapping.ExecutivesTop5Mapping._

@RunWith(classOf[JUnitRunner])
class TestDriver extends FunSpec {
  
   describe("An importer") {

    it("should be able to import an empty company fiscal year") {

      val results = new WorkbookReader(
        WorkbookMapping(
          Seq(Area(TCompanyFiscalYear, Offset(2, 2), None, RowOrientedLayout,
            Seq(Feature(Path('ticker)), Feature(Path('name)))))),
        companyFiscalYearCombiner).read("test/input/CompanyValuesAndNotes.xlsx")

      assert(results === Seq())
    }

    it("should be able to import 3 company fiscal years with executives") {

      val results = CompanyFiscalYearReader.read("test/input/FullValuesOnly.xlsx").map(_.get)
      
      assert(results.size === 3)
      
      assert(results.head('ticker) === Value("something"))
      assert(results.head('name) === Value("something"))
      
      (0 to 2).zip(Seq(2005, 2012, 2013)).foreach{ case (index, year) => 
        validateCompanyYear(results(index), year)
        validateExecutive(results(index).c('executives).take(1).head.asModel)
      }
    }
    
    def validateCompanyYear(company: Model, year: Int) {
      assert(company('disclosureFiscalYear) === Value(year))
      val firstExecFirstCompany = company.c('executives).take(1).head
      validateExecutive(firstExecFirstCompany.asModel)
    }
    
    def validateExecutive(exec: Model) {
      import exec._

      assert(apply('lastName) === Value("exec1Last"))
      assert(apply('title) === Value())

      assert(exec(Path('functionalMatches, 'primary)) === Value("Engineering"))
      assert(exec(Path('functionalMatches, 'secondary)) === Value())
      assert(exec(Path('functionalMatches, 'level)) === Value("SVP (Senior Vice President)"))
      assert(exec(Path('functionalMatches, 'scope)) === Value("Asia"))
      assert(exec(Path('functionalMatches, 'bod)) === Value())

      assert(exec(Path('cashCompensations, 'baseSalary)) === Value(1000))
      assert(exec(Path('cashCompensations, 'actualBonus)) === Value())
      assert(exec(Path('cashCompensations, 'targetBonus)) === Value(0.1: BigDecimal))
      assert(exec(Path('cashCompensations, 'nextFiscalYearData, 'baseSalary)) === Value(2000))
      assert(exec(Path('cashCompensations, 'nextFiscalYearData, 'targetBonus)) === Value(0.5))

      assert(exec(Path('optionGrants, 0, 'number)) === Value(1))
      assert(exec(Path('optionGrants,0,'price)) === Value(2))
      assert(exec(Path('optionGrants,0,'value)) === Value(3))
      assert(exec(Path('optionGrants,0,'perf)) === Value(true))
      assert(exec(Path('optionGrants,0,'type)) === Value("Hire"))

      assert(exec(Path('optionGrants,1,'perf)) === Value(true))
      assert(exec(Path('optionGrants,2,'perf)) === Value(true))
      assert(exec(Path('optionGrants,3,'perf)) === Value(true))
      assert(exec(Path('optionGrants,4,'perf)) === Value(false))

      assert(exec(Path('optionGrants,5,'number)) === Value(1))
      assert(exec(Path('optionGrants,5,'type)) === Value("Annual"))

      assert(exec(Path('timeVestRS,0,'number)) === Value(100))
      assert(exec(Path('timeVestRS,0,'price)) === Value(200))
      assert(exec(Path('timeVestRS,0,'value)) === Value(300))
      assert(exec(Path('timeVestRS,0,'type)) === Value())
      assert(exec(Path('timeVestRS, 5,'type)) === Value("Other"))

      assert(exec(Path('carriedInterest, 'ownedShares, 'beneficialOwnership)) === Value(12))
      assert(exec(Path('carriedInterest, 'ownedShares , 'disclaimBeneficialOwnership)) === Value(13))
      assert(exec(Path('carriedInterest, 'outstandingEquityAwards, 'unvestedOptions)) === Value(14))
      assert(exec(Path('carriedInterest, 'outstandingEquityAwards, 'perfVestRS)) === Value(15))
    }

    it("should throw IllegalArgumentException when there's an invalid functional value") {
      intercept[Throwable] {
        CompanyFiscalYearReader.read("test/input/InvalidFunctionalValue.xlsx")
        .foreach(company => TCompanyFiscalYear.validate(company.get))
      }
    }

    it("should throw an Exception when there's a numeric value on string cell") {
        assert(CompanyFiscalYearReader.read("test/input/ExpectedStringButWasNumeric.xlsx").hasErrors)
    }

    it("should throw an Exception when there's no value on any fiscal year") {
        assert(CompanyFiscalYearReader.read("test/input/EmptyFiscalYear.xlsx").hasErrors)
    }

  }

}