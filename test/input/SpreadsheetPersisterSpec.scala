package input
import model._
import org.scalatest.FunSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.io.ByteArrayOutputStream
import com.mongodb.DBObject
import com.mongodb.casbah.MongoCollection
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.MongoClient
import junit.framework.Assert
import scala.collection.mutable.ArrayBuffer
import util.Closeables
import org.scalatest.BeforeAndAfter


@RunWith(classOf[JUnitRunner])
class SpreadsheetPersisterSpec extends FunSpec with TestSpreadsheetLoader with BeforeAndAfter {
  
  before{
    MongoClient()("test")("companies").drop
  }
  
  describe("SpreadsheetPersister") {
//    it("should output companies") {
//      import Closeables._
//      new ByteArrayOutputStream().processWith { out =>
//      	SpreadsheetPersister.persist(Seq(Company("Z", 2.0, 2009)), out)
//      	assert(out.size > 0)
//      }
//    }
    
    it("should persist companyFiscalYears and its executives from excel") {
      import persistence._
      implicit val companiesCollection = MongoClient()("test")("companies")
      
      loadSpreadsheet("3CompanyFiscalYearsWithExecutives.xls").foreach(_.save)
      
        val company2001 = CompanyFiscalYear(
            ticker = SimpleInput(Some("IBM"), None, None),
            name = SimpleInput(Some("International Business Machine"), None, None),
            disclosureFiscalYear = SimpleInput(Some(2001), None, None),
            executives = ArrayBuffer(
                Executive(
                  name = Input(Some("ExecutiveName23"), None, None, None, None),
                  title = Input(Some("ExecutiveTitle23"), None, None, None, None),
                  shortTitle = Input(Some("ExTi23"), None, None, None, None),
                  functionalMatch = Input(Some("CAO"), None, None, None, None),
                  functionalMatch1 = Input(Some("COO"), None, None, None, None),
                  functionalMatch2 = Input(Some("CEO"), None, None, None, None),
                  founder = Input(Some("lala"), None, None, None, None),
                  carriedInterest = CarriedInterest(
                    ownedShares = Input(Some(100.0), None, None, None, None),
                    vestedOptions = Input(Some(200.0), None, None, None, None),
                    unvestedOptions = Input(Some(300.0), None, None, None, None),
                    tineVest = Input(Some(400.0), None, None, None, None),
                    perfVest = Input(Some(500.0), None, None, None, None)),
                  equityCompanyValue = EquityCompanyValue(
                    optionsValue = Input(Some(1.0), None, None, None, None),
                    options = Input(Some(1.0), None, None, None, None),
                    exPrice = Input(Some(1.0), None, None, None, None),
                    bsPercentage = Input(Some(1.0), None, None, None, None),
                    timeVestRsValue = Input(Some(1.0), None, None, None, None),
                    shares = Input(Some(1.0), None, None, None, None),
                    price = Input(Some(1.0), None, None, None, None),
                    perfRSValue = Input(Some(1.0), None, None, None, None),
                    shares2 = Input(Some(1.0), None, None, None, None),
                    price2 = Input(Some(1.0), None, None, None, None),
                    perfCash = Input(Some(1.0), None, None, None, None)),
                  cashCompensations = AnualCashCompensation(
                    Input(Some(1000.0), None, None, None, None),
                    Input(Some(1.0), None, None, None, None),
                    Input(Some(1.0), None, None, None, None),
                    Input(Some(1.0), None, None, None, None),
                    Input(Some(1.0), None, None, None, None),
                    New8KData(
                      Input(Some(1.0), None, None, None, None),
                      Input(Some(1.0), None, None, None, None))))))
      
      val company2010 = CompanyFiscalYear(
            ticker = SimpleInput(Some("IBM"), None, None),
            name = SimpleInput(Some("International Business Machine"), None, None),
            disclosureFiscalYear = SimpleInput(Some(2010), None, None),
            executives = ArrayBuffer(
                Executive(
                  name = Input(Some("ExecutiveName12"), None, None, None, None),
                  title = Input(Some("ExecutiveTitle12"), None, None, None, None),
                  shortTitle = Input(Some("ExTi12"), None, None, None, None),
                  functionalMatch = Input(Some("CAO"), None, None, None, None),
                  functionalMatch1 = Input(Some("COO"), None, None, None, None),
                  functionalMatch2 = Input(Some("CEO"), None, None, None, None),
                  founder = Input(Some("lala"), None, None, None, None),
                  carriedInterest = CarriedInterest(
                    ownedShares = Input(Some(100.0), None, None, None, None),
                    vestedOptions = Input(Some(200.0), None, None, None, None),
                    unvestedOptions = Input(Some(300.0), None, None, None, None),
                    tineVest = Input(Some(400.0), None, None, None, None),
                    perfVest = Input(Some(500.0), None, None, None, None)),
                  equityCompanyValue = EquityCompanyValue(
                    optionsValue = Input(Some(1.0), None, None, None, None),
                    options = Input(Some(1.0), None, None, None, None),
                    exPrice = Input(Some(1.0), None, None, None, None),
                    bsPercentage = Input(Some(1.0), None, None, None, None),
                    timeVestRsValue = Input(Some(1.0), None, None, None, None),
                    shares = Input(Some(1.0), None, None, None, None),
                    price = Input(Some(1.0), None, None, None, None),
                    perfRSValue = Input(Some(1.0), None, None, None, None),
                    shares2 = Input(Some(1.0), None, None, None, None),
                    price2 = Input(Some(1.0), None, None, None, None),
                    perfCash = Input(Some(1.0), None, None, None, None)),
                  cashCompensations = AnualCashCompensation(
                    Input(Some(1000.0), None, None, None, None),
                    Input(Some(1.0), None, None, None, None),
                    Input(Some(1.0), None, None, None, None),
                    Input(Some(1.0), None, None, None, None),
                    Input(Some(1.0), None, None, None, None),
                    New8KData(
                      Input(Some(1.0), None, None, None, None),
                      Input(Some(1.0), None, None, None, None))))))
              
         assert(findCompanyBy("IBM", 2010, MongoClient()("test")).toString() === company2010.toString())
         assert(findCompanyBy("IBM", 2001, MongoClient()("test")).toString() === company2001.toString())
    }
    
    it("should persist a company and its executives") {
      import persistence._
      implicit val companiesCollection = MongoClient()("test")("companies")
      
      val company = CompanyFiscalYear(
            ticker = SimpleInput(Some("ticker"), Some("note ticker"), None),
            name = SimpleInput(Some("coname"), Some("note coname"), None),
            disclosureFiscalYear = SimpleInput(Some(2012), None, None),
            executives =
              ArrayBuffer(
                Executive(
                  name = Input(Some("ExecutiveName1"), None, None, None, None),
                  title = Input(Some("ExecutiveTitle1"), None, None, None, None),
                  shortTitle = Input(Some("ExTi1"), None, None, None, None),
                  functionalMatch = Input(Some("CAO"), None, None, None, None),
                  functionalMatch1 = Input(Some("COO"), None, None, None, None),
                  functionalMatch2 = Input(Some("CEO"), None, None, None, None),
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
          functionalMatch = Input(Some("CEO"), None, None, None, None),
          functionalMatch1 = Input(Some("COO"), None, None, None, None),
          functionalMatch2 = Input(Some("CAO"), None, None, None, None),
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
        
        company.save              
        assert(findCompanyBy("ticker", 2012, MongoClient()("test")).toString === company.toString)
    }
    
  } 
}