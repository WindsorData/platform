package model.mapping

import util.FileManager._
import org.scalatest.FunSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import libt._
import model._
import input._
import libt.spreadsheet._
import libt.spreadsheet.reader._

@RunWith(classOf[JUnitRunner])
class TestDriver extends FunSpec {
  
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