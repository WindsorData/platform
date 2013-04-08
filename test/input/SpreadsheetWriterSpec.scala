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