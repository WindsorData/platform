package model
import java.util.Date

case class Executive(
  /*Exec data*/
  name: Input[String],
  title: Input[String],
  shortTitle: Input[String],
  functionalMatches: Traversable[Input[String]],
  founder: Input[String],
  cashCompensations: AnualCashCompensation,
  equityCompanyValue: EquityCompanyValue,
  carriedInterest: CarriedInterest) {

  require(validFunctionalMatch, "Invalid Functional Match")
  def tdcPayRank: BigDecimal = ???

  def functionalMatch(n: Int): Input[String] =
    try {
      functionalMatches.toList(n - 1)
    } catch {
      case e: IndexOutOfBoundsException => None
    }

  def validFunctionalMatch =
    functionalMatches.toSet[Input[String]].flatMap { _.value }.subsetOf(Executive.functionalMatchValues)

}

object Executive {
  val functionalMatchValues = Set("Bus Dev (Business Development)",
    "CAO (Chief Admin Officer)",
    "CEO (Chief Executive Officer)",
    "CFO (Chief Financial Officer)",
    "Chmn (Chairman)",
    "CIO (Chief Investment Officer)",
    "COO (Chief Operating Officer)",
    "CSO (Chief Science Officer)",
    "EVP (Executive Vice President)",
    "GC (General Counsel)",
    "GM (General Manager)",
    "Pres (President)",
    "Sales",
    "SVP (Senior Vice President)",
    "Treasr (Treasurer)",
    "VP (Vice President)",
    "Other")
}

case class Input[A](
  value: Option[A],
  calc: Option[String],
  comment: Option[String],
  note: Option[String],
  link: Option[String]) {

  def map[B](f: A => B) =
    Input(value.map(f), calc, comment, note, link)

  def toSimpleInput = SimpleInput(value = value, note = note, link = link)
}

object SimpleInput {

  def apply[A](
    value: Option[A],
    note: Option[String],
    link: Option[String]) = Input(value, None, None, note, link)

}

case class EquityCompanyValue(
  optionsValue: Input[BigDecimal],
  options: Input[BigDecimal],
  exPrice: Input[BigDecimal],
  bsPercentage: Input[BigDecimal],
  timeVestRsValue: Input[BigDecimal],
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


