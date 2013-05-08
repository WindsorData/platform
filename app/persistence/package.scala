import com.mongodb.DBObject
import model._
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBList
import util._
import libt.Model
import libt.persistence._
import libt.Element
import com.mongodb.casbah.commons.MongoDBObjectBuilder

package object persistence {
  type DBO = DBObject

  private def companies(implicit db: MongoDB) = db("companies")

  val allCompanies = "All Companies"
  val allYears = "All Years"

  def saveCompany(company: Model)(implicit db: MongoDB) {
    companies.insert(TCompanyFiscalYear.marshall(company))
  }

  def updateCompany(company: Model)(implicit db: MongoDB) {
    companies.update(
      MongoDBObject(
        "ticker.value" -> company.v[String]('ticker).value.get,
        "disclosureFiscalYear.value" -> company.v('disclosureFiscalYear).value.get),
      TCompanyFiscalYear.marshall(company), true)
  }

  def findAllCompanies(implicit db: MongoDB) = companies.toList.map(TCompanyFiscalYear.unmarshall(_))

  def findCompaniesBy(name: String, year: String)(implicit db: MongoDB) = {
    companies.find(createQuery(year, name)).toSeq.map(TCompanyFiscalYear.unmarshall(_).asModel) match {
    	case Seq() => None
    	case results => Some(results)
    }
  }

  //TODO: check if there's a way to do this better
  def findAllCompaniesNames(implicit db: MongoDB): Seq[String] =
    companies.toSet[DBO].map(_.get("ticker").asInstanceOf[DBO].get("value").toString()).toSeq

  def findAllCompaniesFiscalYears(implicit db: MongoDB): Seq[Int] =
    companies.toSet[DBO].map(_.get("disclosureFiscalYear").asInstanceOf[DBO].get("value").asInstanceOf[Int]).toSeq

  def createQuery(year: String, name: String): MongoDBObject = {
    val builder = new MongoDBObjectBuilder()
    if(year != allYears) builder += ("disclosureFiscalYear.value" -> year.toInt)
    if(name != allCompanies) builder += ("ticker.value" -> name)
    builder.result
  }
}
