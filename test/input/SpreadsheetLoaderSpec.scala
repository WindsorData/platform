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
            'originalCurrency -> Value(), 
            'currencyConversionDate -> Value(),
            'executives -> Col())))
    }

    it("should be able to import a single executive") {
      Assert.assertEquals(
        loadSpreadsheet("FullValuesOnly.xlsx").head.c('executives).take(1),
        Seq(
          Model(
            'name -> Value("ExecutiveName1"),
            'title -> Value("ExecutiveTitle1"),
            'shortTitle -> Value("ExTi1"),
            'functionalMatches -> Model(
              'scope -> Value(),
              'secondary -> Value(),
              'primary -> Value(),
              'level -> Value(),
              'bod -> Value()),
            'founder -> Value("lala"),
            'transitionPeriod -> Value(),
            'carriedInterest -> Model(
              'ownedShares -> Value(100),
              'vestedOptions -> Value(200),
              'unvestedOptions -> Value(300),
              'tineVest -> Value(400),
              'perfVest -> Value(500)),
            'equityCompanyValue -> Model(
              'optionsValue -> Value(1),
              'options -> Value(1),
              'exPrice -> Value(1),
              'bsPercentage -> Value(1),
              'timeVestRsValue -> Value(1),
              'shares -> Value(1),
              'price -> Value(1),
              'perfRSValue -> Value(1),
              'shares2 -> Value(1),
              'price2 -> Value(1),
              'perfCash -> Value(1)),
            'cashCompensations -> Model(
              'baseSalary -> Value(1000.0),
              'actualBonus -> Value(1.0),
              'targetBonus -> Value(1.0),
              'thresholdBonus -> Value(1.0),
              'maxBonus -> Value(1.0),
              'new8KData -> Model(
                'baseSalary -> Value(1.0),
                'targetBonus -> Value(1.0))  ))))
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


