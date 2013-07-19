package persistence

import com.mongodb.casbah.Imports._
import model._
import libt._

object CompaniesDb extends Persistence {

  val TDBSchema = TCompanyFiscalYear
  protected val colName = "companies"
  protected val pk = Seq(Path('cusip), Path('disclosureFiscalYear))

  val allCompanies = "All Companies"

  //TODO: remove Option and return Seq() if there's no result
  def findCompaniesBy(names: Seq[String])(implicit db: MongoDB): Seq[Model] =
    if(!names.contains(allCompanies)) find(operatorExpression("cusip.value", "$in", names))
    else findAll

  def findAllTickers(implicit db: MongoDB): Seq[String] = findAllMap(_ /!/ 'ticker)
  def findAllFiscalYears(implicit db: MongoDB): Seq[Int] = findAllMap(_ /#/ 'disclosureFiscalYear)

  def findAllCompaniesId(implicit db: MongoDB): Seq[(String,String)] =
    findAllMap(company => company /!/ 'cusip -> company /!/ 'ticker)

}
