import com.mongodb.DBObject
import model._
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBList
import util._
import libt.persistence._
import libt._
import com.mongodb.casbah.commons.MongoDBObjectBuilder

package object persistence {
  type DBO = DBObject

  private def companies(implicit db: MongoDB) = db("companies")

  val allCompanies = "All Companies"

  def saveCompany(company: Model)(implicit db: MongoDB) {
    companies.insert(TCompanyFiscalYear.marshall(company))
  }

  def updateCompany(company: Model)(implicit db: MongoDB) {
    companies.update(
      MongoDBObject(
        "ticker.value" -> company(Path('ticker)).asValue[String].value.get,
        "disclosureFiscalYear.value" -> company(Path('disclosureFiscalYear)).asValue.value.get),
      MongoDBObject("$set" -> TCompanyFiscalYear.marshall(company)), true)
  }

  def findAllCompanies(implicit db: MongoDB) = companies.toList.map(TCompanyFiscalYear.unmarshall(_))

  def findCompaniesBy(names: Seq[String], yearRange: Int)(implicit db: MongoDB) = {
    companies.find(createQuery(names)).toSeq.map(TCompanyFiscalYear.unmarshall(_).asModel) match {
      case Seq() => None
      case results => Some(results)
    }
  }

  //TODO: check if there's a way to do this better
  def findAllCompaniesNames(implicit db: MongoDB): Seq[String] =
    companies.toSet[DBO].map(_.get("ticker").asInstanceOf[DBO].get("value").toString()).toSeq

  def findAllCompaniesFiscalYears(implicit db: MongoDB): Seq[Int] =
    companies.toSet[DBO].map(_.get("disclosureFiscalYear").asInstanceOf[DBO].get("value").asInstanceOf[Int]).toSeq

  def createQuery(names: Seq[String]): MongoDBObject =
    if (!names.contains(allCompanies))
      MongoDBObject("ticker.value" -> MongoDBObject("$in" -> names))
    else
      MongoDBObject()

}
