import com.mongodb.DBObject
import model._
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBList
import com.mongodb.casbah.commons.MongoDBListBuilder
import util._
import libt.Model
import libt.persistence._
import libt.Element

package object persistence {
  type DBO = DBObject

  private def companies(implicit db: MongoDB) = db("companies")

  def saveCompany(company: Model)(implicit db: MongoDB) {
    companies.insert(TCompanyFiscalYear.marshall(company))
  }

  def updateCompany(company: Model)(implicit db: MongoDB) {
    companies.update(
      MongoDBObject(
        "'ticker.value" -> company.v[String]('ticker).value.get,
        "'disclosureFiscalYear.value" -> company.v('disclosureFiscalYear).value.get),
      TCompanyFiscalYear.marshall(company), true)
  }

  def findAllCompanies(implicit db: MongoDB) =
    companies.toList.map(TCompanyFiscalYear.unmarshall(_))

  def findCompanyBy(name: String, year: Int)(implicit db: MongoDB) = {
    companies.
      findOne(MongoDBObject("'ticker.value" -> name, "'disclosureFiscalYear.value" -> year)).
      map { TCompanyFiscalYear.unmarshall(_) }
  }

  //TODO: check if there's a way to do this better
  def findAllCompaniesNames(implicit db: MongoDB): Seq[String] =
    companies.toSet[DBO].map(_.get("'ticker").asInstanceOf[DBO].get("value").toString()).toSeq

  def findAllCompaniesFiscalYears(implicit db: MongoDB): Seq[Int] =
    companies.toSet[DBO].map(x =>
      x.get("'disclosureFiscalYear").asInstanceOf[DBO].get("value").asInstanceOf[Int]).toSeq

}
