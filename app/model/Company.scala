package model

import util.persistence.LongKeyedEntity

case class Company(name: String, peerGroup: Double, foundingYear: Int) extends LongKeyedEntity

//TODO
//Ticker
//Name
//Disclosure Fiscal Year End
//GICS Industry
//Annual Rev ($M)
//Market Cap ($M)
//Proxy Shares