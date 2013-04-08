package input

import org.scalatest.path.FunSpec
import util.FileManager
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.io.File
import model._
import scala.collection.mutable.ArrayBuffer

@RunWith(classOf[JUnitRunner])
class SpreadsheetWriterSpec extends FunSpec {
  describe("An Exporter") {

    it("should export executives from excel into an excel") {
        val executives = FileManager.loadSpreadsheet("test/input/FullValuesOnly.xlsx").toSeq
    	executives.foreach(FileManager.generateNewFileWith("test/input/outputTest1.xlsx", _))
	    validateFileExistance("outputTest1")
    }
    
    it("should export executives from model into excel"){
      val company =      
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
                    ownedShares = Input(Some(100),None, None, None, Some("lala")),
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
          
      FileManager.generateNewFileWith("test/input/outputTest2.xlsx", company)
      validateFileExistance("outputTest2")
    }
    
    def validateFileExistance(fileName: String) = {
      val fileTest = new File("test/input/" + fileName + ".xlsx")
      assert(fileTest.exists)
      fileTest.delete
    }
  }

}