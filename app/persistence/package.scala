import com.mongodb.DBObject
import model.CarriedInterest
import model.EquityCompanyValue
import com.mongodb.casbah.commons.MongoDBObject
import model.Input
import model.Executive
import com.mongodb.casbah.Imports._
import model.CompanyFiscalYear

package object persistence {
  type DBO = DBObject
  val windsorDB = MongoClient()("windsor")

  implicit def company2RichCompany[A <% DBObject](company: A)(implicit collection: MongoCollection) =
    new {
      def save() {
        collection.insert(company)
      }
    }

  def findAllCompanies = windsorDB("companies")
  
  def findCompanyBy(name: String) = windsorDB("companies").find(MongoDBObject("ticker.value" -> name))
//    windsorDB("companies").find({"ticker.value":  "ticker", "disclosureFiscalYear.value": 2011})
  def findCompanyBy(name: String, year: Int) = 
    windsorDB("companies").findOne(MongoDBObject("ticker.value" -> name, "disclosureFiscalYear.value" -> year))
  
  
  
  /**Conversions for creating mappings to mongo*/
  implicit def string2MongoArrow(key: Symbol) = new {
    
    def ~>[A <% DBO](value:A) : (String, DBO) = (toString(key), value : DBO)
    
    def ~>[A <% DBO](value:Traversable[A]) : (String, Traversable[DBO]) = 
      	(toString(key), value.map { x => (x : DBO) } )
      	
    def toString(s: Symbol) = s.toString.drop(1)   	
  }
  
  implicit def input2DbObject[A](input: Input[A]): DBO =
    MongoDBObject(
      "value" -> input.value,
      "calc" -> input.calc,
      "comment" -> input.comment,
      "link" -> input.link,
      "note" -> input.note)
      .filter {
        case (k, v) => v != null
      }

  implicit def executive2DbObject(executive: Executive): DBO = {
    MongoDBObject(
      'title ~> executive.title,
      'shortTitle ~> executive.shortTitle,
      'functionalMatch ~> executive.functionalMatch,
      'founder ~> executive.founder,
      'carriedInterest ~> executive.carriedInterest,
      'equityCompanyValue ~> executive.equityCompanyValue)
  }
  implicit def company2DbObject(company: CompanyFiscalYear) =
    MongoDBObject(
      'ticker ~> company.ticker,
      'name ~> company.name,
      'disclosureFiscalYear ~> company.disclosureFiscalYear,
      'gicsIndustry ~> company.gicsIndustry,
      'annualRevenue ~> company.annualRevenue,
      'marketCapital ~> company.marketCapital,
      'proxyShares ~> company.proxyShares,
      'executives ~> company.executives)

  implicit def company2DbObject(interest: CarriedInterest): DBO =
    MongoDBObject(
//      'ownedShares ~> interest.ownedShares,
      'perfVest ~> interest.perfVest,
      'tineVest ~> interest.tineVest,
      'unvestedOptions ~> interest.unvestedOptions,
      'vestedOptions ~> interest.vestedOptions)

  implicit def equity2DbObject(value: EquityCompanyValue): DBO =
    MongoDBObject(
      'bsPercentage ~> value.bsPercentage,
      'exPrice ~> value.exPrice,
      'options ~> value.options,
      'optionsValue ~> value.optionsValue,
      'perf ~> value.perfCash,
      'price ~> value.price,
      'shares ~> value.shares,
      'timeVest ~> value.timeVest,
      'perfRSValue ~> value.perfRSValue,
      'shares2 ~> value.shares2,
      'price2 ~> value.price2,
      'perfCash ~> value.perfCash)
}