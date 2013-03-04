package model

import util.persistence.LongKeyedEntity
import java.sql.Date

case class Company(
  ticker: String,
  name: String,
  disclosureFiscalYear: Date,
  gicsIndustry: String,
  /**Annual revenue in $M*/
  annualRevenue: BigDecimal,
  /**In $M*/
  marketCapital: BigDecimal,
  proxyShares: BigDecimal,
  executives: Traversable[Executive]) extends LongKeyedEntity

//TODO
//Ticker
//Name
//Disclosure Fiscal Year End
//GICS Industry
//Annual Rev ($M)
//Market Cap ($M)
//Proxy Shares