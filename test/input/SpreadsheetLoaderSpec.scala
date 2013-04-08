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

    
    it("should import Executives") {
      assert(loadSpreadsheet("FullValuesOnly.xlsx").head.c('executives).take(2) ===
        Seq(
          Model(
            'name -> Value("ExecutiveName1"),
            'title -> Value("ExecutiveTitle1"),
            'shortTitle -> Value("ExTi1"),
            'functionalMatches -> Col(Value(), Value(), Value()),
            'founder -> Value("lala"),
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
                'targetBonus -> Value(1.0)))),
          Model(
            'name -> Value("ExecutiveName2"),
            'title -> Value("ExecutiveTitle2"),
            'shortTitle -> Value("ExTi2"),
            'functionalMatches -> Model(),
            'founder -> Value("lala"),
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
                'targetBonus -> Value(1.0)) 
            ))))
    }

    it("should import Executives with comments") {
      assert(loadSpreadsheet("FullValuesAndComments.xlsx").head.c('executives).take(1) ===
        Seq(
          Model(
            'name -> Value(Some("ExecutiveName1"), None, Some("C1"), None, None),
            'title -> Value(Some("ExecutiveTitle1"), None, Some("C2"), None, None),
            'shortTitle -> Value(Some("ExTi1"), None, Some("C3"), None, None),
            'functionalMatches -> Col(Value(Some("Other"), None, Some("C4"), None, None),
              Value(Some("Other"), None, Some("fm1com"), None, None),
              Value(Some("Other"), None, Some("fm2com"), None, None)),
            'founder -> Value(Some("lala"), None, Some("C5"), None, None),
            'cashCompensations -> Model(
              'baseSalary -> Value(Some(1000.0), None, Some("C6"), None, None),
              'actualBonus -> Value(Some(1.0), None, Some("C7"), None, None),
              'targetBonus -> Value(Some(1.0), None, Some("C8"), None, None),
              'thresholdBonus -> Value(Some(1.0), None, Some("C9"), None, None),
              'maxBonus -> Value(Some(1.0), None, Some("C10"), None, None),
              'new8KData ->Model(
                'baseSalary -> Value(Some(1.0), None, Some("C11"), None, None),
                'targetBonus ->Value(Some(1.0), None, Some("C12"), None, None))),
            'equityCompanyValue -> Model(
              'optionsValue -> Value(Some(1), None, Some("C13"), None, None),
              'options -> Value(Some(1), None, Some("C14"), None, None),
              'exPrice -> Value(Some(1), None, Some("C15"), None, None),
              'bsPercentage -> Value(1),
              'timeVestRsValue -> Value(1),
              'shares -> Value(1),
              'price -> Value(1),
              'perfRSValue -> Value(1),
              'shares2 -> Value(1),
              'price2 -> Value(1),
              'perfCash -> Value(1)),
            'carriedInterest -> Model(
              'ownedShares -> Value(100),
              'vestedOptions -> Value(200),
              'unvestedOptions -> Value(300),
              'tineVest -> Value(400),
              'perfVest -> Value(500)))))
    }

    it("should throw IllegalArgumentException when there's an invalid functional value") {
      intercept[IllegalArgumentException] {
        loadSpreadsheet("InvalidFunctionalValue.xlsx")
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

    it("should import Executives with extra information") {
      assert(loadSpreadsheet("FullValuesAndExtraInfo.xls").head.c('executives).take(1) === Seq(
        Model(
          'name -> Value("ExecutiveName1"),
          'title -> Value("ExecutiveTitle1"),
          'shortTitle -> Value("ExTi1"),
          'functionalMatches -> Col(Value(), Value(), Value()),
          'founder -> Value("lala"),
          'carriedInterest -> Model(
            'ownedShares -> Value(100),
            'vestedOptions -> Value(200),
            'unvestedOptions -> Value(300),
            'tineVest -> Value(400),
            'perfVest -> Value(500)),
          'equityCompanyValue -> Model(
            'optionsValue -> Value(Some(1), Some("optionsValueCalc"), Some("optionsValueComment"), Some("optionsValueNote"), Some("http://optionsvaluelink.com")),
            'options -> Value(1),
            'exPrice -> Value(1),
            'bsPercentage -> Value(1),
            'timeVestRsValue -> Value(1),
            'shares -> Value(1),
            'price -> Value(1),
            'perfRSValue -> Value(1),
            'shares2 -> Value(1),
            'price2 -> Value(1),
            'perfCash -> Value(Some(1), Some("prefCashCalc"), None, Some("prefCashNote"), Some("http://prefCashLink.com/somethingelse"))),
          'cashCompensations -> Model(
            'baseSalary -> Value(Some(1000.0), None, Some("baseSalaryComment"), None, None),
            'actualBonus -> Value(Some(1.0), None, Some("actualBonusComment"), None, None),
            'targetBonus -> Value(1.0),
            'thresholdBonus -> Value(1.0),
            'maxBonus -> Value(1.0),
            'new8KData -> Model(
              'baseSalary -> Value(1.0),
              'targetBonus ->Value(1.0))))))
    }

//    it("should import a single company fiscal year with executives") {
//      assert(loadSpreadsheet("CompanyFiscalYearAndOneSheet.xlsx") ===
//        Seq(
//          Model(
//            'ticker -> Value(Some("ticker"), Some("note ticker"), None),
//            'name -> Value(Some("coname"), Some("note coname"), None),
//            'disclosureFiscalYear -> Value(Some(2012), None, None),
//            'originalCurrency -> Value(), 
//            'currencyConversionDate -> Value(),
//            'executives ->
//              Seq(
//                Model(
//                  'name ->  Value("ExecutiveName1"),
//                  'title ->  Value("ExecutiveTitle1"),
//                  'shortTitle ->  Value("ExTi1"),
//                  'functionalMatches ->  Col(Value(), Value(), Value()),
//                  'founder ->  Value("lala"),
//                  'carriedInterest ->  Model(
//                    'ownedShares ->  Value(100),
//                    'vestedOptions ->  Value(200),
//                    'unvestedOptions ->  Value(300),
//                    'tineVest ->  Value(400),
//                    'perfVest ->  Value(500)),
//                  'equityCompanyValue ->  Model(
//                    'optionsValue ->  Value(1),
//                    'options ->  Value(1),
//                    'exPrice ->  Value(1),
//                    'bsPercentage ->  Value(1),
//                    'timeVestRsValue ->  Value(1),
//                    'shares ->  Value(1),
//                    'price ->  Value(1),
//                    'perfRSValue ->  Value(1),
//                    'shares2 ->  Value(1),
//                    'price2 ->  Value(1),
//                    'perfCash ->  Value(1)),
//	            'cashCompensations -> Model(
//	              'baseSalary -> Value(1000.0),
//	              'actualBonus -> Value(1.0),
//	              'targetBonus -> Value(1.0),
//	              'thresholdBonus -> Value(1.0),
//	              'maxBonus -> Value(1.0),
//	              'new8KData -> Model(
//	                'baseSalary -> Value(1.0),
//	                'targetBonus -> Value(1.0))),
//                Model(
//                  'name ->  Value("ExecutiveName2"),
//                  'title ->  Value("ExecutiveTitle2"),
//                  'shortTitle ->  Value("ExTi2"),
//                  'functionalMatches ->  Col(Value(), Value(), Value()),
//                  'founder ->  Value("lala"),
//                  'carriedInterest ->  Model(
//                    'ownedShares ->  Value(100),
//                    'vestedOptions ->  Value(200),
//                    'unvestedOptions ->  Value(300),
//                    'tineVest ->  Value(400),
//                    'perfVest ->  Value(500)),
//                  'equityCompanyValue ->  Model(
//                    'optionsValue ->  Value(1),
//                    'options ->  Value(1),
//                    'exPrice ->  Value(1),
//                    'bsPercentage ->  Value(1),
//                    'timeVestRsValue ->  Value(1),
//                    'shares ->  Value(1),
//                    'price ->  Value(1),
//                    'perfRSValue ->  Value(1),
//                    'shares2 ->  Value(1),
//                    'price2 ->  Value(1),
//                    'perfCash ->  Value(1)),
//                  'cashCompensations ->  Model(
//                    Value(1000.0),
//                    Value(1.0),
//                    Value(1.0),
//                    Value(1.0),
//                    Value(1.0),
//                    Model(
//                      Value(1.0),
//                      Value(1.0))))))))
//    }
  }
}


