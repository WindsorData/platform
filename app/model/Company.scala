package model

import java.util.Date

case class CompanyFiscalYear(
  ticker: Input[String],
  name: Input[String],
  disclosureFiscalYear: Input[Int],
  executives: Traversable[Executive])
  
object CompanyFiscalYear{
  import persistence._
  
  def all(): List[CompanyFiscalYear] = findAllCompanies()
  
  def find(name: String, year: Int) = findCompanyBy(name, year)
}




//TODO
//Ticker
//Name
//Disclosure Fiscal Year End
//GICS Industry
//Annual Rev ($M)
//Market Cap ($M)
//Proxy Shares