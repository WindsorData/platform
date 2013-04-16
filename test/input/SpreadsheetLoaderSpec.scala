package input
import model._
import util._
import org.scalatest.path.FunSpec
import org.junit.runner.RunWith
import scala.math.BigDecimal.int2bigDecimal
import util.Closeables.closeable2RichCloseable
import util.FileManager
import org.scalatest.junit.JUnitRunner
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.util.CellUtil
import org.apache.poi.ss.usermodel.Cell
import org.junit.Assert
import java.util.Date
import java.text.SimpleDateFormat
import org.joda.time.DateTime
import libt.Model
import libt.Value
import libt.Col

@RunWith(classOf[JUnitRunner])
class SpreadsheetLoaderSpec extends FunSpec with TestSpreadsheetLoader {
  describe("An importer") {

    it("should be able to import a company") {

      assert(
        loadSpreadsheet("CompanyValuesAndNotes.xlsx") ===
          Seq(Model(
            'ticker -> Value(Some("ticker"), Some("note ticker"), Some("http://alink.com")),
            'name -> Value(Some("coname"), Some("note coname"), Some("http://anotherlink.com")),
            'disclosureFiscalYear -> Value(2012), 
            'executives -> Col())))
    }

    it("should be able to import a single executive") {
      
      val firstExec = loadSpreadsheet("FullValuesOnly.xlsx").head.c('executives).take(1).head

      import firstExec._
      
      assert(v('firstName) === Value("exec1"))
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
        loadSpreadsheet("InvalidFunctionalValue.xlsx").foreach(TCompanyFiscalYear.validate(_))
      }
    }

    it("should throw an Exception when there's a numeric value on string cell") {
      intercept[IllegalStateException] {
        loadSpreadsheet("ExpectedStringButWasNumeric.xlsx")
      }
    }

    it("should throw an Exception when there's no value on any fiscal year") {
      intercept[NoSuchElementException] {
        loadSpreadsheet("EmptyFiscalYear.xlsx")
      }
    }

  }
}



