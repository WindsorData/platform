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
import java.util.Calendar
import org.joda.time.DateTime
import org.joda.time.LocalDate


@RunWith(classOf[JUnitRunner])
class SpreadsheetPersisterSpec extends FunSpec with TestSpreadsheetLoader with BeforeAndAfter {
  
  
  before {
    MongoClient()("test")("companies").drop
  }
  
  describe("SpreadsheetPersister") {
    
    it("should persist companyFiscalYears and its executives from excel") {
      import persistence._
      implicit val companiesCollection = MongoClient()("test")("companies")
      MongoClient()("test")("companies").drop
      
      loadSpreadsheet("3CompanyFiscalYearsWithExecutives.xls").foreach(_.save)
      
        val company2001 = CompanyFiscalYear(
            ticker = Input("IBM"),
            name = Input("International Business Machine"),
            disclosureFiscalYear = Input(2001), 
            originalCurrency = Input("someCurrency"), 
            currencyConversionDate = {val a = new LocalDate(2001, 1, 1); Input(a.toDate)},
            executives = ArrayBuffer(
                Executive(
                  name = Input("ExecutiveName23"),
                  title = Input("ExecutiveTitle23"),
                  shortTitle = Input("ExTi23"),
                  functionalMatches = ArrayBuffer(Input("Other"), Input("Other"), Input("Other")),
                  founder = Input("lala"),
                  carriedInterest = CarriedInterest(
                    ownedShares = Input(100.0),
                    vestedOptions = Input(200.0),
                    unvestedOptions = Input(300.0),
                    tineVest = Input(400.0),
                    perfVest = Input(500.0)),
                  equityCompanyValue = EquityCompanyValue(
                    optionsValue = Input(1.0),
                    options = Input(1.0),
                    exPrice = Input(1.0),
                    bsPercentage = Input(1.0),
                    timeVestRsValue = Input(1.0),
                    shares = Input(1.0),
                    price = Input(1.0),
                    perfRSValue = Input(1.0),
                    shares2 = Input(1.0),
                    price2 = Input(1.0),
                    perfCash = Input(1.0)),
                  cashCompensations = AnualCashCompensation(
                    Input(1000.0),
                    Input(1.0),
                    Input(1.0),
                    Input(1.0),
                    Input(1.0),
                    New8KData(
                      Input(1.0),
                      Input(1.0))))))
      
      val company2010 = CompanyFiscalYear(
            ticker = Input("IBM"),
            name = Input("International Business Machine"),
            disclosureFiscalYear = Input(2010),
            originalCurrency = Input("someCurrency"), 
            currencyConversionDate = {val a = new LocalDate(2001, 1, 1); Input(a.toDate)},
            executives = ArrayBuffer(
                Executive(
                  name = Input("ExecutiveName12"),
                  title = Input("ExecutiveTitle12"),
                  shortTitle = Input("ExTi12"),
                  functionalMatches = ArrayBuffer(Input("Other"), Input("Other"), Input("Other")),
                  founder = Input("lala"),
                  carriedInterest = CarriedInterest(
                    ownedShares = Input(100.0),
                    vestedOptions = Input(200.0),
                    unvestedOptions = Input(300.0),
                    tineVest = Input(400.0),
                    perfVest = Input(500.0)),
                  equityCompanyValue = EquityCompanyValue(
                    optionsValue = Input(1.0),
                    options = Input(1.0),
                    exPrice = Input(1.0),
                    bsPercentage = Input(1.0),
                    timeVestRsValue = Input(1.0),
                    shares = Input(1.0),
                    price = Input(1.0),
                    perfRSValue = Input(1.0),
                    shares2 = Input(1.0),
                    price2 = Input(1.0),
                    perfCash = Input(1.0)),
                  cashCompensations = AnualCashCompensation(
                    Input(1000.0),
                    Input(1.0),
                    Input(1.0),
                    Input(1.0),
                    Input(1.0),
                    New8KData(
                      Input(1.0),
                      Input(1.0))))))
              
         assert(findCompanyBy("IBM", 2010, MongoClient()("test")).get.toString() === company2010.toString())
         assert(findCompanyBy("IBM", 2001, MongoClient()("test")).get.toString() === company2001.toString())
    }
    
    it("should persist a company and its executives") {
      import persistence._
      implicit val companiesCollection = MongoClient()("test")("companies")
      MongoClient()("test")("companies").drop
      
      val company = CompanyFiscalYear(
            ticker = Input("ticker"),
            name = Input("coname"),
            disclosureFiscalYear = Input(2012),
            originalCurrency = None, 
            currencyConversionDate = None,            
            executives =
              ArrayBuffer(
                Executive(
                  name = Input("ExecutiveName1"),
                  title = Input("ExecutiveTitle1"),
                  shortTitle = Input("ExTi1"),
                  functionalMatches = ArrayBuffer(Input("Other"), Input("Other"), Input("Other")),
                  founder = Input("lala"),
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
          functionalMatches = ArrayBuffer(Input("Other"), Input("Other"), Input("Other")),
          founder = Input("lala"),
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
        
        company.save              
        assert(findCompanyBy("ticker", 2012, MongoClient()("test")).get.toString === company.toString)
    }
    
  } 
}