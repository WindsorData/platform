package model
import java.util.Date

case class Executive(
  /*Exec data*/
  name: Input[String],
  title: Input[String],
  shortTitle: Input[String],
  functionalMatch: Input[String],
  functionalMatch1: Input[String],
  functionalMatch2: Input[String],
  founder: Input[String],
  cashCompensations: AnualCashCompensation,
  equityCompanyValue: EquityCompanyValue,
  carriedInterest: CarriedInterest) {

  require(validFunctionalMatch, "Invalid Functional Match")
  def tdcPayRank: BigDecimal = ???

  def validFunctionalMatch =
    functionalMatches.flatMap { _.value }.subsetOf(Executive.functionalMatchValues)

  def functionalMatches = Set(functionalMatch, functionalMatch1, functionalMatch2)
}

object Executive {
  val functionalMatchValues = Set("Bus Dev", "CAO", "CEO", "CFO", "Chmn", "CIO", "COO", "CSO",
    "EVP", "GC", "GM", "Pres", "Sales", "SVP", "Treasr", "VP", "Other")
}

case class Input[A](
  value: Option[A],
  calc: Option[String],
  comment: Option[String],
  note: Option[String],
  link: Option[String]) {

  def map[B](f: A => B) =
    Input(value.map(f), calc, comment, note, link)
}

case class EquityCompanyValue(
  optionsValue: Input[BigDecimal],
  options: Input[BigDecimal],
  exPrice: Input[BigDecimal],
  bsPercentage: Input[BigDecimal],
  timeVest: Input[BigDecimal],
  rsValue: Input[BigDecimal],
  shares: Input[BigDecimal],
  price: Input[BigDecimal],
  perfRSValue: Input[BigDecimal],
  shares2: Input[BigDecimal],
  price2: Input[BigDecimal],
  perfCash: Input[BigDecimal])

case class CarriedInterest(
  ownedShares: Input[BigDecimal],
  vestedOptions: Input[BigDecimal],
  unvestedOptions: Input[BigDecimal],
  tineVest: Input[BigDecimal],
  perfVest: Input[BigDecimal])

case class AnualCashCompensation(
  baseSalary: Input[BigDecimal],
  actualBonus: Input[BigDecimal],
  targetBonus: Input[BigDecimal],
  thresholdBonus: Input[BigDecimal],
  maxBonus: Input[BigDecimal],
  new8KData: New8KData)

case class New8KData(
  baseSalary: Input[BigDecimal],
  targetBonus: Input[BigDecimal])  

//case class CashCompensation(anualRecords: Seq[AnualRecord]) {
//  def currentTtdc = ???
//}


