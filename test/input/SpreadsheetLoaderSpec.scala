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
            ticker = SimpleInput(Some("ticker"), Some("note ticker"), Some("http://alink.com")),
            name = SimpleInput(Some("coname"), Some("note coname"), Some("http://anotherlink.com")),
            disclosureFiscalYear = SimpleInput(None, None, None),
            executives = Seq())))
    }

    it("should be able to import a single executive") {
      Assert.assertEquals(
        loadSpreadsheet("FullValuesOnly.xlsx").head.executives.take(1),
        Seq(
          Executive(
            name = Some("ExecutiveName1"),
            title = Some("ExecutiveTitle1"),
            shortTitle = Some("ExTi1"),
            functionalMatches = List(Some("CAO"),Some("COO"),Some("CEO")),
            founder = Some("lala"),
            carriedInterest = CarriedInterest(
              ownedShares = Some(100: BigDecimal),
              vestedOptions = Input(Some(200), None, None, None, None),
              unvestedOptions = Input(Some(300), None, None, None, None),
              tineVest = Input(Some(400), None, None, None, None),
              perfVest = Input(Some(500), None, None, None, None)),
            equityCompanyValue = EquityCompanyValue(
              optionsValue = Input(Some(1), None, None, None, None),
              options = Input(Some(1), None, None, None, None),
              exPrice = Input(Some(1), None, None, None, None),
              bsPercentage = Input(Some(1), None, None, None, None),
              timeVestRsValue = Input(Some(1), None, None, None, None),
              shares = Input(Some(1), None, None, None, None),
              price = Input(Some(1), None, None, None, None),
              perfRSValue = Input(Some(1), None, None, None, None),
              shares2 = Input(Some(1), None, None, None, None),
              price2 = Input(Some(1), None, None, None, None),
              perfCash = Input(Some(1), None, None, None, None)),
            cashCompensations = AnualCashCompensation(
              Input(Some(1000.0), None, None, None, None),
              Input(Some(1.0), None, None, None, None),
              Input(Some(1.0), None, None, None, None),
              Input(Some(1.0), None, None, None, None),
              Input(Some(1.0), None, None, None, None),
              New8KData(
                Input(Some(1.0), None, None, None, None),
                Input(Some(1.0), None, None, None, None))))))
    }

    it("should import Executives") {
      assert(loadSpreadsheet("FullValuesOnly.xlsx").head.executives.take(2) ===
        Seq(
          Executive(
            name = Input(Some("ExecutiveName1"), None, None, None, None),
            title = Input(Some("ExecutiveTitle1"), None, None, None, None),
            shortTitle = Input(Some("ExTi1"), None, None, None, None),
            functionalMatches = List(Some("CAO"),Some("COO"),Some("CEO")),
            founder = Input(Some("lala"), None, None, None, None),
            carriedInterest = CarriedInterest(
              ownedShares = Input(Some(100), None, None, None, None),
              vestedOptions = Input(Some(200), None, None, None, None),
              unvestedOptions = Input(Some(300), None, None, None, None),
              tineVest = Input(Some(400), None, None, None, None),
              perfVest = Input(Some(500), None, None, None, None)),
            equityCompanyValue = EquityCompanyValue(
              optionsValue = Input(Some(1), None, None, None, None),
              options = Input(Some(1), None, None, None, None),
              exPrice = Input(Some(1), None, None, None, None),
              bsPercentage = Input(Some(1), None, None, None, None),
              timeVestRsValue = Input(Some(1), None, None, None, None),
              shares = Input(Some(1), None, None, None, None),
              price = Input(Some(1), None, None, None, None),
              perfRSValue = Input(Some(1), None, None, None, None),
              shares2 = Input(Some(1), None, None, None, None),
              price2 = Input(Some(1), None, None, None, None),
              perfCash = Input(Some(1), None, None, None, None)),
            cashCompensations = AnualCashCompensation(
              Input(Some(1000.0), None, None, None, None),
              Input(Some(1.0), None, None, None, None),
              Input(Some(1.0), None, None, None, None),
              Input(Some(1.0), None, None, None, None),
              Input(Some(1.0), None, None, None, None),
              New8KData(
                Input(Some(1.0), None, None, None, None),
                Input(Some(1.0), None, None, None, None)))),

          Executive(
            name = Input(Some("ExecutiveName2"), None, None, None, None),
            title = Input(Some("ExecutiveTitle2"), None, None, None, None),
            shortTitle = Input(Some("ExTi2"), None, None, None, None),
            functionalMatches = List(Some("CEO"),Some("COO"),Some("CAO")),
            founder = Input(Some("lala"), None, None, None, None),
            carriedInterest = CarriedInterest(
              ownedShares = Input(Some(100), None, None, None, None),
              vestedOptions = Input(Some(200), None, None, None, None),
              unvestedOptions = Input(Some(300), None, None, None, None),
              tineVest = Input(Some(400), None, None, None, None),
              perfVest = Input(Some(500), None, None, None, None)),
            equityCompanyValue = EquityCompanyValue(
              optionsValue = Input(Some(1), None, None, None, None),
              options = Input(Some(1), None, None, None, None),
              exPrice = Input(Some(1), None, None, None, None),
              bsPercentage = Input(Some(1), None, None, None, None),
              timeVestRsValue = Input(Some(1), None, None, None, None),
              shares = Input(Some(1), None, None, None, None),
              price = Input(Some(1), None, None, None, None),
              perfRSValue = Input(Some(1), None, None, None, None),
              shares2 = Input(Some(1), None, None, None, None),
              price2 = Input(Some(1), None, None, None, None),
              perfCash = Input(Some(1), None, None, None, None)),
            cashCompensations = AnualCashCompensation(
              Input(Some(1000.0), None, None, None, None),
              Input(Some(1.0), None, None, None, None),
              Input(Some(1.0), None, None, None, None),
              Input(Some(1.0), None, None, None, None),
              Input(Some(1.0), None, None, None, None),
              New8KData(
                Input(Some(1.0), None, None, None, None),
                Input(Some(1.0), None, None, None, None))))))
    }

    it("should import Executives with comments") {
      assert(loadSpreadsheet("FullValuesAndComments.xlsx").head.executives.take(1) ===
        Seq(
          Executive(
            name = Input(Some("ExecutiveName1"), None, Some("C1"), None, None),
            title = Input(Some("ExecutiveTitle1"), None, Some("C2"), None, None),
            shortTitle = Input(Some("ExTi1"), None, Some("C3"), None, None),
            functionalMatches = List(Input(Some("CAO"), None, Some("C4"), None, None),
            							Input(Some("CEO"), None, Some("fm1com"), None, None),
            							Input(Some("COO"), None, Some("fm2com"), None, None)),
            founder = Input(Some("lala"), None, Some("C5"), None, None),
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
              bsPercentage = Input(Some(1), None, None, None, None),
              timeVestRsValue = Input(Some(1), None, None, None, None),
              shares = Input(Some(1), None, None, None, None),
              price = Input(Some(1), None, None, None, None),
              perfRSValue = Input(Some(1), None, None, None, None),
              shares2 = Input(Some(1), None, None, None, None),
              price2 = Input(Some(1), None, None, None, None),
              perfCash = Input(Some(1), None, None, None, None)),
            carriedInterest = CarriedInterest(
              ownedShares = Input(Some(100), None, None, None, None),
              vestedOptions = Input(Some(200), None, None, None, None),
              unvestedOptions = Input(Some(300), None, None, None, None),
              tineVest = Input(Some(400), None, None, None, None),
              perfVest = Input(Some(500), None, None, None, None)))))
    }

    it("should throw IllegalArgumentException when there's an invalid functional value") {
      intercept[IllegalArgumentException] {
        loadSpreadsheet("InvalidFunctionalValue.xlsx")
      }
    }

    it("should import Executives with extra information") {
      assert(loadSpreadsheet("FullValuesAndExtraInfo.xls").head.executives.take(1) === Seq(
        Executive(
          name = Input(Some("ExecutiveName1"), None, None, None, None),
          title = Input(Some("ExecutiveTitle1"), None, None, None, None),
          shortTitle = Input(Some("ExTi1"), None, None, None, None),
          functionalMatches = List(Some("CAO"),Some("CEO"),Some("COO")),
          founder = Input(Some("lala"), None, None, None, None),
          carriedInterest = CarriedInterest(
              ownedShares = Input(Some(100), None, None, None, None),
              vestedOptions = Input(Some(200), None, None, None, None),
              unvestedOptions = Input(Some(300), None, None, None, None),
              tineVest = Input(Some(400), None, None, None, None),
              perfVest = Input(Some(500), None, None, None, None)),
          equityCompanyValue = EquityCompanyValue(
            optionsValue = Input(Some(1), Some("optionsValueCalc"), Some("optionsValueComment"), Some("optionsValueNote"), Some("http://optionsvaluelink.com")),
            options = Input(Some(1), None, None, None, None),
            exPrice = Input(Some(1), None, None, None, None),
            bsPercentage = Input(Some(1), None, None, None, None),
            timeVestRsValue = Input(Some(1), None, None, None, None),
            shares = Input(Some(1), None, None, None, None),
            price = Input(Some(1), None, None, None, None),
            perfRSValue = Input(Some(1), None, None, None, None),
            shares2 = Input(Some(1), None, None, None, None),
            price2 = Input(Some(1), None, None, None, None),
            perfCash = Input(Some(1), Some("prefCashCalc"), None, Some("prefCashNote"), Some("http://prefCashLink.com/somethingelse"))),
          cashCompensations = AnualCashCompensation(
            baseSalary = Input(Some(1000.0), None, Some("baseSalaryComment"), None, None),
            actualBonus = Input(Some(1.0), None, Some("actualBonusComment"), None, None),
            targetBonus = Input(Some(1.0), None, None, None, None),
            thresholdBonus = Input(Some(1.0), None, None, None, None),
            maxBonus = Input(Some(1.0), None, None, None, None),
            New8KData(
              Input(Some(1.0), None, None, None, None),
              Input(Some(1.0), None, None, None, None))))))
    }

    it("should import a single company fiscal year with executives") {
      assert(loadSpreadsheet("CompanyFiscalYearAndOneSheet.xlsx") ===
        Seq(
          CompanyFiscalYear(
            ticker = SimpleInput(Some("ticker"), Some("note ticker"), None),
            name = SimpleInput(Some("coname"), Some("note coname"), None),
            disclosureFiscalYear = SimpleInput(Some(2012), None, None),
            executives =
              Seq(
                Executive(
                  name = Input(Some("ExecutiveName1"), None, None, None, None),
                  title = Input(Some("ExecutiveTitle1"), None, None, None, None),
                  shortTitle = Input(Some("ExTi1"), None, None, None, None),
                  functionalMatches = List(Some("CAO"),Some("COO"),Some("CEO")),
                  founder = Input(Some("lala"), None, None, None, None),
                  carriedInterest = CarriedInterest(
                    ownedShares = Input(Some(100), None, None, None, None),
                    vestedOptions = Input(Some(200), None, None, None, None),
                    unvestedOptions = Input(Some(300), None, None, None, None),
                    tineVest = Input(Some(400), None, None, None, None),
                    perfVest = Input(Some(500), None, None, None, None)),
                  equityCompanyValue = EquityCompanyValue(
                    optionsValue = Input(Some(1), None, None, None, None),
                    options = Input(Some(1), None, None, None, None),
                    exPrice = Input(Some(1), None, None, None, None),
                    bsPercentage = Input(Some(1), None, None, None, None),
                    timeVestRsValue = Input(Some(1), None, None, None, None),
                    shares = Input(Some(1), None, None, None, None),
                    price = Input(Some(1), None, None, None, None),
                    perfRSValue = Input(Some(1), None, None, None, None),
                    shares2 = Input(Some(1), None, None, None, None),
                    price2 = Input(Some(1), None, None, None, None),
                    perfCash = Input(Some(1), None, None, None, None)),
                  cashCompensations = AnualCashCompensation(
                    Input(Some(1000.0), None, None, None, None),
                    Input(Some(1.0), None, None, None, None),
                    Input(Some(1.0), None, None, None, None),
                    Input(Some(1.0), None, None, None, None),
                    Input(Some(1.0), None, None, None, None),
                    New8KData(
                      Input(Some(1.0), None, None, None, None),
                      Input(Some(1.0), None, None, None, None)))),
        Executive(
          name = Input(Some("ExecutiveName2"), None, None, None, None),
          title = Input(Some("ExecutiveTitle2"), None, None, None, None),
          shortTitle = Input(Some("ExTi2"), None, None, None, None),
          functionalMatches = List(Some("CEO"),Some("COO"),Some("CAO")),
          founder = Input(Some("lala"), None, None, None, None),
          carriedInterest = CarriedInterest(
            ownedShares = Input(Some(100), None, None, None, None),
            vestedOptions = Input(Some(200), None, None, None, None),
            unvestedOptions = Input(Some(300), None, None, None, None),
            tineVest = Input(Some(400), None, None, None, None),
            perfVest = Input(Some(500), None, None, None, None)),
          equityCompanyValue = EquityCompanyValue(
            optionsValue = Input(Some(1), None, None, None, None),
            options = Input(Some(1), None, None, None, None),
            exPrice = Input(Some(1), None, None, None, None),
            bsPercentage = Input(Some(1), None, None, None, None),
            timeVestRsValue = Input(Some(1), None, None, None, None),
            shares = Input(Some(1), None, None, None, None),
            price = Input(Some(1), None, None, None, None),
            perfRSValue = Input(Some(1), None, None, None, None),
            shares2 = Input(Some(1), None, None, None, None),
            price2 = Input(Some(1), None, None, None, None),
            perfCash = Input(Some(1), None, None, None, None)),
          cashCompensations = AnualCashCompensation(
            Input(Some(1000.0), None, None, None, None),
            Input(Some(1.0), None, None, None, None),
            Input(Some(1.0), None, None, None, None),
            Input(Some(1.0), None, None, None, None),
            Input(Some(1.0), None, None, None, None),
            New8KData(
              Input(Some(1.0), None, None, None, None),
              Input(Some(1.0), None, None, None, None))))))))
    }

    ignore("should import 2 companiesFiscalYears with multiple executives") {
      assert(loadSpreadsheet("MultipleSheets.xls").init ===
        Seq(
          CompanyFiscalYear(
            ticker = SimpleInput(Some("ticker"), Some("note ticker"), None),
            name = SimpleInput(Some("coname"), Some("note coname"), None),
            disclosureFiscalYear = SimpleInput(Some(2011), None, None),
            executives =
              Seq(
                Executive(
                  name = Input(Some("ExecutiveName1"), None, None, None, None),
                  title = Input(Some("ExecutiveTitle1"), None, None, None, None),
                  shortTitle = Input(Some("ExTi1"), None, None, None, None),
                  functionalMatches = List(Some("CAO"),Some("COO"),Some("CEO")),
                  founder = Input(Some("lala"), None, None, None, None),
                  carriedInterest = CarriedInterest(
                    ownedShares = Input(Some(100), None, None, None, None),
                    vestedOptions = Input(Some(200), None, None, None, None),
                    unvestedOptions = Input(Some(300), None, None, None, None),
                    tineVest = Input(Some(400), None, None, None, None),
                    perfVest = Input(Some(500), None, None, None, None)),
                  equityCompanyValue = EquityCompanyValue(
                    optionsValue = Input(Some(1), None, None, None, None),
                    options = Input(Some(1), None, None, None, None),
                    exPrice = Input(Some(1), None, None, None, None),
                    bsPercentage = Input(Some(1), None, None, None, None),
                    timeVestRsValue = Input(Some(1), None, None, None, None),
                    shares = Input(Some(1), None, None, None, None),
                    price = Input(Some(1), None, None, None, None),
                    perfRSValue = Input(Some(1), None, None, None, None),
                    shares2 = Input(Some(1), None, None, None, None),
                    price2 = Input(Some(1), None, None, None, None),
                    perfCash = Input(Some(1), None, None, None, None)),
                  cashCompensations = AnualCashCompensation(
                    Input(Some(1000.0), None, None, None, None),
                    Input(Some(1.0), None, None, None, None),
                    Input(Some(1.0), None, None, None, None),
                    Input(Some(1.0), None, None, None, None),
                    Input(Some(1.0), None, None, None, None),
                    New8KData(
                      Input(Some(1.0), None, None, None, None),
                      Input(Some(1.0), None, None, None, None)))),

                Executive(
                  name = Input(Some("ExecutiveName2"), None, None, None, None),
                  title = Input(Some("ExecutiveTitle2"), None, None, None, None),
                  shortTitle = Input(Some("ExTi2"), None, None, None, None),
                  functionalMatches = List(Some("CEO"),Some("COO"),Some("CAO")),
                  founder = Input(Some("lala"), None, None, None, None),
                  carriedInterest = CarriedInterest(
                    ownedShares = Input(Some(100), None, None, None, None),
                    vestedOptions = Input(Some(200), None, None, None, None),
                    unvestedOptions = Input(Some(300), None, None, None, None),
                    tineVest = Input(Some(400), None, None, None, None),
                    perfVest = Input(Some(500), None, None, None, None)),
                  equityCompanyValue = EquityCompanyValue(
                    optionsValue = Input(Some(1), None, None, None, None),
                    options = Input(Some(1), None, None, None, None),
                    exPrice = Input(Some(1), None, None, None, None),
                    bsPercentage = Input(Some(1), None, None, None, None),
                    timeVestRsValue = Input(Some(1), None, None, None, None),
                    shares = Input(Some(1), None, None, None, None),
                    price = Input(Some(1), None, None, None, None),
                    perfRSValue = Input(Some(1), None, None, None, None),
                    shares2 = Input(Some(1), None, None, None, None),
                    price2 = Input(Some(1), None, None, None, None),
                    perfCash = Input(Some(1), None, None, None, None)),
                  cashCompensations = AnualCashCompensation(
                    Input(Some(1000.0), None, None, None, None),
                    Input(Some(1.0), None, None, None, None),
                    Input(Some(1.0), None, None, None, None),
                    Input(Some(1.0), None, None, None, None),
                    Input(Some(1.0), None, None, None, None),
                    New8KData(
                      Input(Some(1.0), None, None, None, None),
                      Input(Some(1.0), None, None, None, None)))))),
          CompanyFiscalYear(
            ticker = SimpleInput(Some("ticker"), Some("note ticker"), None),
            name = SimpleInput(Some("coname"), Some("note coname"), None),
            disclosureFiscalYear = SimpleInput(Some(2010),None, None),
            executives = Seq(
              Executive(
                name = Input(Some("ExecutiveName1"), None, None, None, None),
                title = Input(Some("ExecutiveTitle1"), None, None, None, None),
                shortTitle = Input(Some("ExTi1"), None, None, None, None),
                functionalMatches = List(Some("CAO"),Some("COO"),Some("CEO")),
                founder = Input(Some("lala"), None, None, None, None),
                carriedInterest = CarriedInterest(
                  ownedShares = Input(Some(600), None, None, None, None),
                  vestedOptions = Input(Some(700), None, None, None, None),
                  unvestedOptions = Input(Some(800), None, None, None, None),
                  tineVest = Input(Some(900), None, None, None, None),
                  perfVest = Input(Some(1000), None, None, None, None)),
                equityCompanyValue = EquityCompanyValue(
                  optionsValue = Input(Some(1), None, None, None, None),
                  options = Input(Some(1), None, None, None, None),
                  exPrice = Input(Some(1), None, None, None, None),
                  bsPercentage = Input(Some(1), None, None, None, None),
                  timeVestRsValue = Input(Some(1), None, None, None, None),
                  shares = Input(Some(1), None, None, None, None),
                  price = Input(Some(1), None, None, None, None),
                  perfRSValue = Input(Some(1), None, None, None, None),
                  shares2 = Input(Some(1), None, None, None, None),
                  price2 = Input(Some(1), None, None, None, None),
                  perfCash = Input(Some(1), None, None, None, None)),
                cashCompensations = AnualCashCompensation(
                  Input(Some(1000.0), None, None, None, None),
                  Input(Some(1.0), None, None, None, None),
                  Input(Some(1.0), None, None, None, None),
                  Input(Some(1.0), None, None, None, None),
                  Input(Some(1.0), None, None, None, None),
                  New8KData(
                    Input(Some(1.0), None, None, None, None),
                    Input(Some(1.0), None, None, None, None)))),

              Executive(
                name = Input(Some("ExecutiveName2"), None, None, None, None),
                title = Input(Some("ExecutiveTitle2"), None, None, None, None),
                shortTitle = Input(Some("ExTi2"), None, None, None, None),
                functionalMatches = List(Some("CEO"),Some("COO"),Some("CAO")),
                founder = Input(Some("lala"), None, None, None, None),
                carriedInterest = CarriedInterest(
                  ownedShares = Input(Some(600), None, None, None, None),
                  vestedOptions = Input(Some(700), None, None, None, None),
                  unvestedOptions = Input(Some(800), None, None, None, None),
                  tineVest = Input(Some(900), None, None, None, None),
                  perfVest = Input(Some(1000), None, None, None, None)),
                equityCompanyValue = EquityCompanyValue(
                  optionsValue = Input(Some(1), None, None, None, None),
                  options = Input(Some(1), None, None, None, None),
                  exPrice = Input(Some(1), None, None, None, None),
                  bsPercentage = Input(Some(1), None, None, None, None),
                  timeVestRsValue = Input(Some(1), None, None, None, None),
                  shares = Input(Some(1), None, None, None, None),
                  price = Input(Some(1), None, None, None, None),
                  perfRSValue = Input(Some(1), None, None, None, None),
                  shares2 = Input(Some(1), None, None, None, None),
                  price2 = Input(Some(1), None, None, None, None),
                  perfCash = Input(Some(1), None, None, None, None)),
                cashCompensations = AnualCashCompensation(
                  Input(Some(1000.0), None, None, None, None),
                  Input(Some(1.0), None, None, None, None),
                  Input(Some(1.0), None, None, None, None),
                  Input(Some(1.0), None, None, None, None),
                  Input(Some(1.0), None, None, None, None),
                  New8KData(
                    Input(Some(1.0), None, None, None, None),
                    Input(Some(1.0), None, None, None, None)))))),

          CompanyFiscalYear(
            ticker = SimpleInput(Some("ticker"), Some("note ticker"), None),
            name = SimpleInput(Some("coname"), Some("note coname"), None),
            disclosureFiscalYear = SimpleInput(Some(2009), None, None),
            executives = Seq(
              Executive(
                name = Input(Some("ExecutiveName1"), None, None, None, None),
                title = Input(Some("ExecutiveTitle1"), None, None, None, None),
                shortTitle = Input(Some("ExTi1"), None, None, None, None),
                functionalMatches = List(Some("CAO"),Some("COO"),Some("CEO")),
                founder = Input(Some("lala"), None, None, None, None),
                carriedInterest = CarriedInterest(
                  ownedShares = Input(Some(1100), None, None, None, None),
                  vestedOptions = Input(Some(1200), None, None, None, None),
                  unvestedOptions = Input(Some(1300), None, None, None, None),
                  tineVest = Input(Some(1400), None, None, None, None),
                  perfVest = Input(Some(1500), None, None, None, None)),
                equityCompanyValue = EquityCompanyValue(
                  optionsValue = Input(Some(1), None, None, None, None),
                  options = Input(Some(1), None, None, None, None),
                  exPrice = Input(Some(1), None, None, None, None),
                  bsPercentage = Input(Some(1), None, None, None, None),
                  timeVestRsValue = Input(Some(1), None, None, None, None),
                  shares = Input(Some(1), None, None, None, None),
                  price = Input(Some(1), None, None, None, None),
                  perfRSValue = Input(Some(1), None, None, None, None),
                  shares2 = Input(Some(1), None, None, None, None),
                  price2 = Input(Some(1), None, None, None, None),
                  perfCash = Input(Some(1), None, None, None, None)),
                cashCompensations = AnualCashCompensation(
                  Input(Some(1000.0), None, None, None, None),
                  Input(Some(1.0), None, None, None, None),
                  Input(Some(1.0), None, None, None, None),
                  Input(Some(1.0), None, None, None, None),
                  Input(Some(1.0), None, None, None, None),
                  New8KData(
                    Input(Some(1.0), None, None, None, None),
                    Input(Some(1.0), None, None, None, None)))),

              Executive(
                name = Input(Some("ExecutiveName2"), None, None, None, None),
                title = Input(Some("ExecutiveTitle2"), None, None, None, None),
                shortTitle = Input(Some("ExTi2"), None, None, None, None),
                functionalMatches = List(Some("CEO"),Some("COO"),Some("CAO")),
                founder = Input(Some("lala"), None, None, None, None),
                carriedInterest = CarriedInterest(
                  ownedShares = Input(Some(1100), None, None, None, None),
                  vestedOptions = Input(Some(1200), None, None, None, None),
                  unvestedOptions = Input(Some(1300), None, None, None, None),
                  tineVest = Input(Some(1400), None, None, None, None),
                  perfVest = Input(Some(1500), None, None, None, None)),
                equityCompanyValue = EquityCompanyValue(
                  optionsValue = Input(Some(1), None, None, None, None),
                  options = Input(Some(1), None, None, None, None),
                  exPrice = Input(Some(1), None, None, None, None),
                  bsPercentage = Input(Some(1), None, None, None, None),
                  timeVestRsValue = Input(Some(1), None, None, None, None),
                  shares = Input(Some(1), None, None, None, None),
                  price = Input(Some(1), None, None, None, None),
                  perfRSValue = Input(Some(1), None, None, None, None),
                  shares2 = Input(Some(1), None, None, None, None),
                  price2 = Input(Some(1), None, None, None, None),
                  perfCash = Input(Some(1), None, None, None, None)),
                cashCompensations = AnualCashCompensation(
                  Input(Some(1000.0), None, None, None, None),
                  Input(Some(1.0), None, None, None, None),
                  Input(Some(1.0), None, None, None, None),
                  Input(Some(1.0), None, None, None, None),
                  Input(Some(1.0), None, None, None, None),
                  New8KData(
                    Input(Some(1.0), None, None, None, None),
                    Input(Some(1.0), None, None, None, None))))))))
    }

  }
}
