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
    	executives.foreach(FileManager.generateNewFileWith("test/input/outputTest.xlsx", _))
	    validateFileExistance
    }
    
    it("should export executivos from model into excel"){
      val company = CompanyFiscalYear(Input(Some("IBM"), None, None, None, None), Input(Some("International Business Machine"), None, None, None, None), Input(Some(2005), None, None, None, None), ArrayBuffer(Executive(Input(Some("ExecutiveName1"), None, None, None, None), Input(Some("ExecutiveTitle1"), None, None, None, None), Input(Some("ExTi1"), None, None, None, None), List(), Input(Some("lala"), None, None, None, None), AnualCashCompensation(Input(Some(1000.0), None, None, None, None), Input(Some(1.0), None, None, None, None), Input(Some(1.0), None, None, None, None), Input(Some(1.0), None, None, None, None), Input(Some(1.0), None, None, None, None), New8KData(Input(Some(1.0), None, None, None, None), Input(None, None, None, None, None))), EquityCompanyValue(Input(Some(1.0), None, None, None, None), Input(Some(1.0), None, None, None, None), Input(Some(1.0), None, None, None, None), Input(Some(1.0), None, None, None, None), Input(None, None, None, None, None), Input(Some(1.0), None, None, None, None), Input(Some(1.0), None, None, None, None), Input(Some(1.0), None, None, None, None), Input(Some(1.0), None, None, None, None), Input(Some(1.0), None, None, None, None), Input(Some(1.0), None, None, None, None)), CarriedInterest(Input(Some(100.0), None, None, None, None), Input(Some(200.0), None, None, None, None), Input(Some(300.0), None, None, None, None), Input(Some(400.0), None, None, None, None), Input(Some(500.0), None, None, None, None))), Executive(Input(Some("ExecutiveName2"), None, None, None, None), Input(Some("ExecutiveTitle2"), None, None, None, None), Input(Some("ExTi2"), None, None, None, None), List() , Input(Some("lala"), None, None, None, None), AnualCashCompensation(Input(Some(1000.0), None, None, None, None), Input(Some(1.0), None, None, None, None), Input(Some(1.0), None, None, None, None), Input(Some(1.0), None, None, None, None), Input(Some(1.0), None, None, None, None), New8KData(Input(Some(1.0), None, None, None, None), Input(None, None, None, None, None))), EquityCompanyValue(Input(Some(1.0), None, None, None, None), Input(Some(1.0), None, None, None, None), Input(Some(1.0), None, None, None, None), Input(Some(1.0), None, None, None, None), Input(None, None, None, None, None), Input(Some(1.0), None, None, None, None), Input(Some(1.0), None, None, None, None), Input(Some(1.0), None, None, None, None), Input(Some(1.0), None, None, None, None), Input(Some(1.0), None, None, None, None), Input(Some(1.0), None, None, None, None)), CarriedInterest(Input(Some(100.0), None, None, None, None), Input(Some(200.0), None, None, None, None), Input(Some(300.0), None, None, None, None), Input(Some(400.0), None, None, None, None), Input(Some(500.0), None, None, None, None)))))
      FileManager.generateNewFileWith("test/input/outputTest.xlsx", company)
      validateFileExistance
    }
    
    def validateFileExistance= {
      val fileTest = new File("test/input/outputTest.xlsx")
      assert(fileTest.exists)
      fileTest.delete
    }
  }

}