package model

import java.util.Date

case class Company(
  ticker: Input[String],
  name: Input[String],
  disclosureFiscalYear: Input[Date],
  gicsIndustry: Input[String],
  /**Annual revenue in $M*/
  annualRevenue: Input[BigDecimal],
  /**In $M*/
  marketCapital: Input[BigDecimal],
  proxyShares: Input[BigDecimal],
  executives: Traversable[Executive]) 


//TODO
//Ticker
//Name
//Disclosure Fiscal Year End
//GICS Industry
//Annual Rev ($M)
//Market Cap ($M)
//Proxy Shares