package model
import java.util.Date
import play.Logger

case class FunctionalMatch(
  primary: Input[String],
  secondary: Input[String],
  level: Input[String],
  scope: Input[String],
  bod: Input[String]) {

  def validate =
    Set(primary).flatMap { _.value }.subsetOf(FunctionalMatch.primaryValues) &&
      Set(secondary).flatMap { _.value }.subsetOf(FunctionalMatch.secondaryValues) &&
      Set(level).flatMap { _.value }.subsetOf(FunctionalMatch.levelValues) &&
      Set(scope).flatMap { _.value }.subsetOf(FunctionalMatch.scopeValues) &&
      Set(bod).flatMap { _.value }.subsetOf(FunctionalMatch.bodValues)
}

object FunctionalMatch {

  def apply(): FunctionalMatch = FunctionalMatch(None, None, None, None, None)    
  def apply(list: Traversable[Input[String]]): FunctionalMatch =
    FunctionalMatch(
      list.head,
      list.drop(1).head,
      list.drop(2).head,
      list.drop(3).head,
      list.drop(4).head)

  val primaryValues = Set("CEO (Chief Executive Officer)",
    "CFO (Chief Financial Officer)",
    "COO (Chief Operating Officer)",
    "Sales",
    "Bus Dev (Business Development)",
    "CAO (Chief Admin Officer)",
    "GC (General Counsel-Legal)",
    "CAO (Chief Accounting Officer)",
    "CIO (Chief Investment-Asset Officer)",
    "CTO (Chief Technology Officer)",
    "Manufacturing",
    "Engineering",
    "Marketing",
    "CSO (Chief Science Officer)",
    "CSO (Chief Strategic Officer)",
    "CIO (Chief Information Officer)",
    "Product",
    "CRO (Chief Risk Officer)",
    "Treasurer/Secretary",
    "Executive Chairman",
    "Other")

  val secondaryValues = Set(
    "Sales",
    "Bus Dev (Business Development)",
    "CAO (Chief Admin Officer)",
    "GC (General Counsel-Legal)",
    "CAO (Chief Accounting Officer)",
    "CIO (Chief Investment-Asset Officer)",
    "CTO (Chief Technology Officer)",
    "Manufacturing",
    "Engineering",
    "Marketing",
    "CSO (Chief Science Officer)",
    "CSO (Chief Strategic Officer)",
    "CIO (Chief Information Officer)",
    "Product",
    "CRO (Chief Risk Officer)",
    "Treasurer/Secretary",
    "GM (General Manager)",
    "Other")

  val levelValues = Set(
    "President",
    "EVP (Executive Vice President)",
    "SVP (Senior Vice President)",
    "VP (Vice President)",
    "GM (General Manager)",
    "Group President")

  val scopeValues = Set(
    "WW/Global/International",
    "US",
    "North America",
    "Europe",
    "Asia",
    "Americas")

  val bodValues = Set(
    "Chairman",
    "Vice Chairman",
    "Director")

  val invalidFunctionalMatchErrorMessage = "Invalid Functional Match"
}

case class Executive(
  /*Exec data*/
  name: Input[String],
  title: Input[String],
  shortTitle: Input[String],
  //TODO: this could be implemented by creating another model
  functionalMatches: FunctionalMatch,
  founder: Input[String],
  transitionPeriod: Input[String],
  cashCompensations: AnualCashCompensation,
  equityCompanyValue: EquityCompanyValue,
  carriedInterest: CarriedInterest) {

  require(functionalMatches.validate, FunctionalMatch.invalidFunctionalMatchErrorMessage)
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

object Input {

  def apply[A](
    value: Option[A],
    note: Option[String],
    link: Option[String]): Input[A] = Input(value, None, None, note, link)

  def apply[A](value: A): Input[A] = Input(Option(value), None, None, None, None)
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


