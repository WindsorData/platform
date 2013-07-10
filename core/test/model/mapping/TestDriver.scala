package model.mapping

import util.FileManager._
import org.scalatest.FunSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import libt.workflow._
import libt._
import model._
import libt.spreadsheet._
import libt.spreadsheet.reader._

class TestDriver extends FunSpec {

  describe("An importer") {

    ignore("should be able to import an empty company fiscal year") {
      val results = top5.Workflow.readFile("test/input/CompanyValuesAndNotes.xlsx")
      assert(results === Seq())
    }

    it("should be able to import 3 company fiscal years with executives") {

      val results = top5.Workflow.readFile("test/input/FullValuesOnly.xlsx").get

      assert(results.size === 4)

      assert(results.head('ticker) === Value("BV"))
      assert(results.head('name) === Value("Bazaarvoice Inc"))

      (1 to 3).zip(Seq(2012, 2011, 2010)).foreach {
        case (index, year) =>
          assert(results(index)('disclosureFiscalYear) === Value(year))
      }
      validateExecutive(results(1).applySeq('executives).take(1).head.asModel)
    }

    def validateExecutive(exec: Model) {
      import exec._

      assert(apply('lastName) === Value("Hurt"))
      assert(apply('title) === Value("Chief Executive Officer and President"))

      assert(exec(Path('functionalMatches, 'primary)) === Value("CEO (Chief Executive Officer)"))
      assert(exec(Path('functionalMatches, 'level)) === Value("President"))
      assert(exec(Path('functionalMatches, 'bod)) === Value("Director"))

      assert(exec(Path('cashCompensations, 'baseSalary)) === Value(309))
      assert(exec(Path('cashCompensations, 'targetBonus)) === Value(0.75: BigDecimal))
      assert(exec(Path('cashCompensations, 'nextFiscalYearData, 'targetBonus)) === Value(0.8: BigDecimal))

      assert(exec(Path('optionGrants, 0, 'number)) === Value(200000: BigDecimal))
      assert(exec(Path('optionGrants, 0, 'price)) === Value(6.58: BigDecimal))
      assert(exec(Path('optionGrants, 0, 'value)) === Value(729.74: BigDecimal))
      assert(exec(Path('optionGrants, 0, 'perf)) === Value(false))

      assert(exec(Path('carriedInterest, 'ownedShares, 'beneficialOwnership)) === Value(6480473: BigDecimal))
      assert(exec(Path('carriedInterest, 'ownedShares, 'options)) === Value(66666: BigDecimal))
      assert(exec(Path('carriedInterest, 'ownedShares, 'heldByTrust)) === Value(1114766: BigDecimal))
      assert(exec(Path('carriedInterest, 'outstandingEquityAwards, 'unvestedOptions)) === Value(200000: BigDecimal))
    }

    it("should throw IllegalArgumentException when there's an invalid functional value") {
      intercept[Throwable] {
        top5.Workflow.readFile("test/input/InvalidFunctionalValue.xlsx")
          .get.foreach(company => TCompanyFiscalYear.validate(company))
      }
    }

    it("should throw an Exception when there's a numeric value on string cell") {
      assert(top5.Workflow.readFile("test/input/ExpectedStringButWasNumeric.xlsx").isInvalid)
    }

    it("should throw an Exception when there's no value on any fiscal year") {
      assert(top5.Workflow.readFile("test/input/EmptyFiscalYear.xlsx").isInvalid)
    }

  }

}