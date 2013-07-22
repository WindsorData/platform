package persistence

import com.mongodb.casbah.Imports._
import model._
import libt._
import com.mongodb.casbah.MongoClient

case class CompaniesDb(db: MongoDB) extends Persistence {

  val TDBSchema = TCompanyFiscalYear
  protected val colName = "companies"
  protected val pk = Seq(Path('cusip), Path('disclosureFiscalYear))

  val allCompanies = "All Companies"

  //TODO: remove Option and return Seq() if there's no result
  def findCompaniesBy(names: Seq[String]): Seq[Model] =
    if(!names.contains(allCompanies)) find(operatorExpression("cusip.value", "$in", names))
    else findAll

  def findAllTickers: Seq[String] = findAllMap(_ /!/ 'ticker)
  def findAllFiscalYears: Seq[Int] = findAllMap(_ /#/ 'disclosureFiscalYear)

  def findAllCompaniesId: Seq[(String,String)] =
    findAllMap(company => company /!/ 'cusip -> company /!/ 'ticker)

  def findAllCompaniesIdWithNames: Seq[(String,String,String)] =
    findAllMap(company => (company /!/ 'cusip, company /!/ 'ticker, company /!/ 'name))

}