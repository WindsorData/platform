package model

import java.util.Date

case class CompanyFiscalYear(
  ticker: Input[String],
  name: Input[String],
  disclosureFiscalYear: Input[Int],
  executives: Traversable[Executive])




//TODO
//Ticker
//Name
//Disclosure Fiscal Year End
//GICS Industry
//Annual Rev ($M)
//Market Cap ($M)
//Proxy Shares