package input
import model._
import util._
import org.scalatest.path.FunSpec
import org.junit.runner.RunWith
import java.io.FileInputStream
import scala.math.BigDecimal.int2bigDecimal
import util.Closeables.closeable2RichCloseable
import util.FileManager
import org.scalatest.junit.JUnitRunner
import java.io.InputStream
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.usermodel.Row
import scala.collection.mutable.ListBuffer
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.util.CellUtil
import org.apache.poi.ss.usermodel.Cell
import org.junit.Assert
import java.util.Date
import java.text.SimpleDateFormat
import org.joda.time.DateTime

@RunWith(classOf[JUnitRunner])
class SpreadsheetLoaderSpec extends FunSpec with TestSpreadsheetLoader {

  describe("An importer") {

    it("should be able to import a company") {

      assert(
        loadSpreadsheet("CompanyValuesAndNotes.xlsx") ===
          Seq(CompanyFiscalYear(
            ticker = Input(Some("ticker"), Some("note ticker"), Some("http://alink.com")),
            name = Input(Some("coname"), Some("note coname"), Some("http://anotherlink.com")),
            disclosureFiscalYear = Some(2012),
            originalCurrency = None,
            currencyConversionDate = None,
            executives = Seq())))
    }

    it("should be able to import a single executive") {
      Assert.assertEquals(
        loadSpreadsheet("FullValuesOnly.xlsx").head.executives.take(1),
        Seq(
          Executive(
            name = Some("ExecutiveName1"),
            title = Input("ExecutiveTitle1"),
            shortTitle = Some("ExTi1"),
            functionalMatches = FunctionalMatch(),
            founder = Input("lala"), transitionPeriod = None,
            carriedInterest = CarriedInterest(
              ownedShares = Input(100),
              vestedOptions = Input(200),
              unvestedOptions = Input(300),
              tineVest = Input(400),
              perfVest = Input(500)),
            equityCompanyValue = EquityCompanyValue(
              optionsValue = Input(1),
              options = Input(1),
              exPrice = Input(1),
              bsPercentage = Input(1),
              timeVestRsValue = Input(1),
              shares = Input(1),
              price = Input(1),
              perfRSValue = Input(1),
              shares2 = Input(1),
              price2 = Input(1),
              perfCash = Input(1)),
            cashCompensations = AnualCashCompensation(
              Input(1000.0),
              Input(1.0),
              Input(1.0),
              Input(1.0),
              Input(1.0),
              New8KData(
                Input(1.0),
                Input(1.0))))))
    }

    it("should import Executives") {
      assert(loadSpreadsheet("FullValuesOnly.xlsx").head.executives.take(2) ===
        Seq(
          Executive(
            name = Input("ExecutiveName1"),
            title = Input("ExecutiveTitle1"),
            shortTitle = Input("ExTi1"),
            functionalMatches = FunctionalMatch(),
            founder = Input("lala"), transitionPeriod = None,
            carriedInterest = CarriedInterest(
              ownedShares = Input(100),
              vestedOptions = Input(200),
              unvestedOptions = Input(300),
              tineVest = Input(400),
              perfVest = Input(500)),
            equityCompanyValue = EquityCompanyValue(
              optionsValue = Input(1),
              options = Input(1),
              exPrice = Input(1),
              bsPercentage = Input(1),
              timeVestRsValue = Input(1),
              shares = Input(1),
              price = Input(1),
              perfRSValue = Input(1),
              shares2 = Input(1),
              price2 = Input(1),
              perfCash = Input(1)),
            cashCompensations = AnualCashCompensation(
              Input(1000.0),
              Input(1.0),
              Input(1.0),
              Input(1.0),
              Input(1.0),
              New8KData(
                Input(1.0),
                Input(1.0)))),

          Executive(
            name = Input("ExecutiveName2"),
            title = Input("ExecutiveTitle2"),
            shortTitle = Input("ExTi2"),
            functionalMatches = FunctionalMatch(),
            founder = Input("lala"), transitionPeriod = None,
            carriedInterest = CarriedInterest(
              ownedShares = Input(100),
              vestedOptions = Input(200),
              unvestedOptions = Input(300),
              tineVest = Input(400),
              perfVest = Input(500)),
            equityCompanyValue = EquityCompanyValue(
              optionsValue = Input(1),
              options = Input(1),
              exPrice = Input(1),
              bsPercentage = Input(1),
              timeVestRsValue = Input(1),
              shares = Input(1),
              price = Input(1),
              perfRSValue = Input(1),
              shares2 = Input(1),
              price2 = Input(1),
              perfCash = Input(1)),
            cashCompensations = AnualCashCompensation(
              Input(1000.0),
              Input(1.0),
              Input(1.0),
              Input(1.0),
              Input(1.0),
              New8KData(
                Input(1.0),
                Input(1.0))))))
    }

    it("should import Executives with comments") {
      assert(loadSpreadsheet("FullValuesAndComments.xlsx").head.executives.take(1) ===
        Seq(
          Executive(
            name = Input(Some("ExecutiveName1"), None, Some("C1"), None, None),
            title = Input(Some("ExecutiveTitle1"), None, Some("C2"), None, None),
            shortTitle = Input(Some("ExTi1"), None, Some("C3"), None, None),
            functionalMatches = FunctionalMatch(
              	Input(Some("Other"), None, Some("C4"), None, None),
                Input(Some("Other"), None, Some("fm1com"), None, None),
                Input(Some("President"), None, Some("fm2com"), None, None), None, None),
            founder = Input(Some("lala"), None, Some("C5"), None, None),transitionPeriod = None,
            cashCompensations = AnualCashCompensation(
              Input(Some(1000.0), None, Some("C6"), None, None),
              Input(Some(1.0), None, Some("C7"), None, None),
              Input(Some(1.0), None, Some("C8"), None, None),
              Input(Some(1.0), None, Some("C9"), None, None),
              Input(Some(1.0), None, Some("C10"), None, None),
              New8KData(
                Input(Some(1.0), None, Some("C11"), None, None),
                Input(Some(1.0), None, Some("C12"), None, None))),
            equityCompanyValue = EquityCompanyValue(
              optionsValue = Input(Some(1), None, Some("C13"), None, None),
              options = Input(Some(1), None, Some("C14"), None, None),
              exPrice = Input(Some(1), None, Some("C15"), None, None),
              bsPercentage = Input(1),
              timeVestRsValue = Input(1),
              shares = Input(1),
              price = Input(1),
              perfRSValue = Input(1),
              shares2 = Input(1),
              price2 = Input(1),
              perfCash = Input(1)),
            carriedInterest = CarriedInterest(
              ownedShares = Input(100),
              vestedOptions = Input(200),
              unvestedOptions = Input(300),
              tineVest = Input(400),
              perfVest = Input(500)))))
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
      assert(loadSpreadsheet("FullValuesAndExtraInfo.xls").head.executives.take(1) === Seq(
        Executive(
          name = Input("ExecutiveName1"),
          title = Input("ExecutiveTitle1"),
          shortTitle = Input("ExTi1"),
          functionalMatches = FunctionalMatch(),
          founder = Input("lala"), transitionPeriod = None,
          carriedInterest = CarriedInterest(
            ownedShares = Input(100),
            vestedOptions = Input(200),
            unvestedOptions = Input(300),
            tineVest = Input(400),
            perfVest = Input(500)),
          equityCompanyValue = EquityCompanyValue(
            optionsValue = Input(Some(1), Some("optionsValueCalc"), Some("optionsValueComment"), Some("optionsValueNote"), Some("http://optionsvaluelink.com")),
            options = Input(1),
            exPrice = Input(1),
            bsPercentage = Input(1),
            timeVestRsValue = Input(1),
            shares = Input(1),
            price = Input(1),
            perfRSValue = Input(1),
            shares2 = Input(1),
            price2 = Input(1),
            perfCash = Input(Some(1), Some("prefCashCalc"), None, Some("prefCashNote"), Some("http://prefCashLink.com/somethingelse"))),
          cashCompensations = AnualCashCompensation(
            baseSalary = Input(Some(1000.0), None, Some("baseSalaryComment"), None, None),
            actualBonus = Input(Some(1.0), None, Some("actualBonusComment"), None, None),
            targetBonus = Input(1.0),
            thresholdBonus = Input(1.0),
            maxBonus = Input(1.0),
            New8KData(
              Input(1.0),
              Input(1.0))))))
    }

    it("should import a single company fiscal year with executives") {
      assert(loadSpreadsheet("CompanyFiscalYearAndOneSheet.xlsx") ===
        Seq(
          CompanyFiscalYear(
            ticker = Input(Some("ticker"), Some("note ticker"), None),
            name = Input(Some("coname"), Some("note coname"), None),
            disclosureFiscalYear = Input(Some(2012), None, None),
            originalCurrency = None,
            currencyConversionDate = None,
            executives =
              Seq(
                Executive(
                  name = Input("ExecutiveName1"),
                  title = Input("ExecutiveTitle1"),
                  shortTitle = Input("ExTi1"),
                  functionalMatches = FunctionalMatch(),
                  founder = Input("lala"), transitionPeriod = None,
                  carriedInterest = CarriedInterest(
                    ownedShares = Input(100),
                    vestedOptions = Input(200),
                    unvestedOptions = Input(300),
                    tineVest = Input(400),
                    perfVest = Input(500)),
                  equityCompanyValue = EquityCompanyValue(
                    optionsValue = Input(1),
                    options = Input(1),
                    exPrice = Input(1),
                    bsPercentage = Input(1),
                    timeVestRsValue = Input(1),
                    shares = Input(1),
                    price = Input(1),
                    perfRSValue = Input(1),
                    shares2 = Input(1),
                    price2 = Input(1),
                    perfCash = Input(1)),
                  cashCompensations = AnualCashCompensation(
                    Input(1000.0),
                    Input(1.0),
                    Input(1.0),
                    Input(1.0),
                    Input(1.0),
                    New8KData(
                      Input(1.0),
                      Input(1.0)))),
                Executive(
                  name = Input("ExecutiveName2"),
                  title = Input("ExecutiveTitle2"),
                  shortTitle = Input("ExTi2"),
                  functionalMatches = FunctionalMatch(),
                  founder = Input("lala"), transitionPeriod = None,
                  carriedInterest = CarriedInterest(
                    ownedShares = Input(100),
                    vestedOptions = Input(200),
                    unvestedOptions = Input(300),
                    tineVest = Input(400),
                    perfVest = Input(500)),
                  equityCompanyValue = EquityCompanyValue(
                    optionsValue = Input(1),
                    options = Input(1),
                    exPrice = Input(1),
                    bsPercentage = Input(1),
                    timeVestRsValue = Input(1),
                    shares = Input(1),
                    price = Input(1),
                    perfRSValue = Input(1),
                    shares2 = Input(1),
                    price2 = Input(1),
                    perfCash = Input(1)),
                  cashCompensations = AnualCashCompensation(
                    Input(1000.0),
                    Input(1.0),
                    Input(1.0),
                    Input(1.0),
                    Input(1.0),
                    New8KData(
                      Input(1.0),
                      Input(1.0))))))))
    }

    ignore("should import 2 companiesFiscalYears with multiple executives") {
      assert(loadSpreadsheet("MultipleSheets.xls").init ===
        Seq(
          CompanyFiscalYear(
            ticker = Input(Some("ticker"), Some("note ticker"), None),
            name = Input(Some("coname"), Some("note coname"), None),
            disclosureFiscalYear = Input(Some(2011), None, None),
            originalCurrency = None,
            currencyConversionDate = None,
            executives =
              Seq(
                Executive(
                  name = Input("ExecutiveName1"),
                  title = Input("ExecutiveTitle1"),
                  shortTitle = Input("ExTi1"),
                  functionalMatches = FunctionalMatch(Input("CAO"), Input("COO"), Input("CEO"), None, None),
                  founder = Input("lala"), transitionPeriod = None,
                  carriedInterest = CarriedInterest(
                    ownedShares = Input(100),
                    vestedOptions = Input(200),
                    unvestedOptions = Input(300),
                    tineVest = Input(400),
                    perfVest = Input(500)),
                  equityCompanyValue = EquityCompanyValue(
                    optionsValue = Input(1),
                    options = Input(1),
                    exPrice = Input(1),
                    bsPercentage = Input(1),
                    timeVestRsValue = Input(1),
                    shares = Input(1),
                    price = Input(1),
                    perfRSValue = Input(1),
                    shares2 = Input(1),
                    price2 = Input(1),
                    perfCash = Input(1)),
                  cashCompensations = AnualCashCompensation(
                    Input(1000.0),
                    Input(1.0),
                    Input(1.0),
                    Input(1.0),
                    Input(1.0),
                    New8KData(
                      Input(1.0),
                      Input(1.0)))),

                Executive(
                  name = Input("ExecutiveName2"),
                  title = Input("ExecutiveTitle2"),
                  shortTitle = Input("ExTi2"),
                  functionalMatches = FunctionalMatch(Input("CEO"), Input("COO"), Input("CAO"), None, None),
                  founder = Input("lala"), transitionPeriod = None,
                  carriedInterest = CarriedInterest(
                    ownedShares = Input(100),
                    vestedOptions = Input(200),
                    unvestedOptions = Input(300),
                    tineVest = Input(400),
                    perfVest = Input(500)),
                  equityCompanyValue = EquityCompanyValue(
                    optionsValue = Input(1),
                    options = Input(1),
                    exPrice = Input(1),
                    bsPercentage = Input(1),
                    timeVestRsValue = Input(1),
                    shares = Input(1),
                    price = Input(1),
                    perfRSValue = Input(1),
                    shares2 = Input(1),
                    price2 = Input(1),
                    perfCash = Input(1)),
                  cashCompensations = AnualCashCompensation(
                    Input(1000.0),
                    Input(1.0),
                    Input(1.0),
                    Input(1.0),
                    Input(1.0),
                    New8KData(
                      Input(1.0),
                      Input(1.0)))))),
          CompanyFiscalYear(
            ticker = Input(Some("ticker"), Some("note ticker"), None),
            name = Input(Some("coname"), Some("note coname"), None),
            disclosureFiscalYear = Input(Some(2010), None, None),
            originalCurrency = None,
            currencyConversionDate = None,
            executives = Seq(
              Executive(
                name = Input("ExecutiveName1"),
                title = Input("ExecutiveTitle1"),
                shortTitle = Input("ExTi1"),
                functionalMatches = FunctionalMatch(Input("CAO"), Input("COO"), Input("CEO"), None, None),
                founder = Input("lala"), transitionPeriod = None,
                carriedInterest = CarriedInterest(
                  ownedShares = Input(600),
                  vestedOptions = Input(700),
                  unvestedOptions = Input(800),
                  tineVest = Input(900),
                  perfVest = Input(1000)),
                equityCompanyValue = EquityCompanyValue(
                  optionsValue = Input(1),
                  options = Input(1),
                  exPrice = Input(1),
                  bsPercentage = Input(1),
                  timeVestRsValue = Input(1),
                  shares = Input(1),
                  price = Input(1),
                  perfRSValue = Input(1),
                  shares2 = Input(1),
                  price2 = Input(1),
                  perfCash = Input(1)),
                cashCompensations = AnualCashCompensation(
                  Input(1000.0),
                  Input(1.0),
                  Input(1.0),
                  Input(1.0),
                  Input(1.0),
                  New8KData(
                    Input(1.0),
                    Input(1.0)))),

              Executive(
                name = Input("ExecutiveName2"),
                title = Input("ExecutiveTitle2"),
                shortTitle = Input("ExTi2"),
                functionalMatches = FunctionalMatch(Input("CEO"), Input("COO"), Input("CAO"), None, None),
                founder = Input("lala"), transitionPeriod = None,
                carriedInterest = CarriedInterest(
                  ownedShares = Input(600),
                  vestedOptions = Input(700),
                  unvestedOptions = Input(800),
                  tineVest = Input(900),
                  perfVest = Input(1000)),
                equityCompanyValue = EquityCompanyValue(
                  optionsValue = Input(1),
                  options = Input(1),
                  exPrice = Input(1),
                  bsPercentage = Input(1),
                  timeVestRsValue = Input(1),
                  shares = Input(1),
                  price = Input(1),
                  perfRSValue = Input(1),
                  shares2 = Input(1),
                  price2 = Input(1),
                  perfCash = Input(1)),
                cashCompensations = AnualCashCompensation(
                  Input(1000.0),
                  Input(1.0),
                  Input(1.0),
                  Input(1.0),
                  Input(1.0),
                  New8KData(
                    Input(1.0),
                    Input(1.0)))))),

          CompanyFiscalYear(
            ticker = Input(Some("ticker"), Some("note ticker"), None),
            name = Input(Some("coname"), Some("note coname"), None),
            disclosureFiscalYear = Input(Some(2009), None, None),
            originalCurrency = None,
            currencyConversionDate = None,
            executives = Seq(
              Executive(
                name = Input("ExecutiveName1"),
                title = Input("ExecutiveTitle1"),
                shortTitle = Input("ExTi1"),
                functionalMatches = FunctionalMatch(Input("CAO"), Input("COO"), Input("CEO"), None, None),
                founder = Input("lala"), transitionPeriod = None,
                carriedInterest = CarriedInterest(
                  ownedShares = Input(1100),
                  vestedOptions = Input(1200),
                  unvestedOptions = Input(1300),
                  tineVest = Input(1400),
                  perfVest = Input(1500)),
                equityCompanyValue = EquityCompanyValue(
                  optionsValue = Input(1),
                  options = Input(1),
                  exPrice = Input(1),
                  bsPercentage = Input(1),
                  timeVestRsValue = Input(1),
                  shares = Input(1),
                  price = Input(1),
                  perfRSValue = Input(1),
                  shares2 = Input(1),
                  price2 = Input(1),
                  perfCash = Input(1)),
                cashCompensations = AnualCashCompensation(
                  Input(1000.0),
                  Input(1.0),
                  Input(1.0),
                  Input(1.0),
                  Input(1.0),
                  New8KData(
                    Input(1.0),
                    Input(1.0)))),

              Executive(
                name = Input("ExecutiveName2"),
                title = Input("ExecutiveTitle2"),
                shortTitle = Input("ExTi2"),
                functionalMatches = FunctionalMatch(Input("CEO"), Input("COO"), Input("CAO"), None, None),
                founder = Input("lala"), transitionPeriod = None,
                carriedInterest = CarriedInterest(
                  ownedShares = Input(1100),
                  vestedOptions = Input(1200),
                  unvestedOptions = Input(1300),
                  tineVest = Input(1400),
                  perfVest = Input(1500)),
                equityCompanyValue = EquityCompanyValue(
                  optionsValue = Input(1),
                  options = Input(1),
                  exPrice = Input(1),
                  bsPercentage = Input(1),
                  timeVestRsValue = Input(1),
                  shares = Input(1),
                  price = Input(1),
                  perfRSValue = Input(1),
                  shares2 = Input(1),
                  price2 = Input(1),
                  perfCash = Input(1)),
                cashCompensations = AnualCashCompensation(
                  Input(1000.0),
                  Input(1.0),
                  Input(1.0),
                  Input(1.0),
                  Input(1.0),
                  New8KData(
                    Input(1.0),
                    Input(1.0))))))))
    }

  }
}
