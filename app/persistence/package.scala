import com.mongodb.DBObject
import model.Company
import model.CarriedInterest
import model.EquityCompanyValue
import com.mongodb.casbah.commons.MongoDBObject
import model.Input
import model.Executive
import com.mongodb.casbah.Imports._

package object persistence {
  type DBO = DBObject

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

  implicit def executive2DbObject(executive: Executive): DBO =
    MongoDBObject(
      "title" -> (executive.title: DBO),
      "shortTitle" -> (executive.shortTitle: DBO),
      "functionalMatch" -> (executive.functionalMatch: DBO),
      "founder" -> (executive.founder: DBO),
      "carriedInterest" -> (executive.carriedInterest: DBO),
      "equityCompanyValue" -> (executive.equityCompanyValue: DBO))

  implicit def company2DbObject(company: Company) =
    MongoDBObject(
      "ticker" -> (company.ticker: DBO),
      "name" -> (company.name: DBO),
      "disclosureFiscalYear" -> (company.disclosureFiscalYear: DBO),
      "gicsIndustry" -> (company.gicsIndustry: DBO),
      "annualRevenue" -> (company.annualRevenue: DBO),
      "marketCapital" -> (company.marketCapital: DBO),
      "proxyShares" -> (company.proxyShares: DBO),
      "executives" -> company.executives.map { x => x: DBO })

  implicit def company2DbObject(interest: CarriedInterest): DBO =
    MongoDBObject(
      "ownedShares" -> (interest.ownedShares: DBO),
      "perfVest" -> (interest.perfVest: DBO),
      "tineVest" -> (interest.tineVest: DBO),
      "unvestedOptions" -> (interest.unvestedOptions: DBO),
      "vestedOptions" -> (interest.vestedOptions: DBO))

  implicit def equity2DbObject(value: EquityCompanyValue): DBO =
    MongoDBObject(
      "bsPercentage" -> (value.bsPercentage: DBO),
      "exPrice" -> (value.exPrice: DBO),
      "options" -> (value.options: DBO),
      "optionsValue" -> (value.optionsValue: DBO),
      "perf" -> (value.perfCash: DBO),
      "price" -> (value.price: DBO),
      "shares" -> (value.shares: DBO),
      "timeVest" -> (value.timeVest: DBO),
      "perfRSValue" -> (value.perfRSValue: DBO),
      "shares2" -> (value.shares2: DBO),
      "price2" -> (value.price2: DBO),
      "perfCash" -> (value.perfCash: DBO))
}