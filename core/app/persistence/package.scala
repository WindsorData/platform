
import model._
import com.mongodb.casbah.Imports._
import libt.persistence._
import libt._

package object persistence {
  type DBO = DBObject

  def unmarshallCompany = TCompanyFiscalYear.unmarshall(_)
  def marshallCompany = TCompanyFiscalYear.marshall(_)

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
      MongoDBObject("$set" -> marshallCompany(company)), true)
  }

  def findAllCompanies(implicit db: MongoDB) = companies.toList.map(unmarshallCompany)

  def findByExample(implicit db: MongoDB, example : DBO) = companies.find(example).toList.map(unmarshallCompany)

  def findCompaniesBy(names: Seq[String], yearRange: Int)(implicit db: MongoDB) = {
    companies.find(createQuery(names)).toSeq.map(unmarshallCompany(_).asModel) match {
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
      operatorExpression("ticker.value", "$in", names)
    else
      MongoDBObject()


  def operatorExpression(property: String, operator: String, values: Any) = MongoDBObject(property -> MongoDBObject(operator -> values))
}
