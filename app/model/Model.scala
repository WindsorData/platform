package model
import util.Todo._
import java.util.Date

case class Executive(
  /*Exec data*/
  name: Input[String],
  title: Input[String],
  shortTitle: Input[String], 
  functionalMatch: Input[String],
  founder: Input[String],
  cashCompensations : Seq[AnualCashCompensation],
  equityCompanyValue: EquityCompanyValue,
  carriedInterest: CarriedInterest) {

  def tdcPayRank: BigDecimal = ???
}

case class Input[T](
    value: Option[T],
    calc: Option[String],
    comment: Option[String],
    note: Option[String],
    link: Option[String])

case class EquityCompanyValue(
  optionsValue: Input[BigDecimal],
  options: Input[BigDecimal],
  exPrice: Input[BigDecimal],
  bsPercentage: Input[BigDecimal],
  timeVest: Input[BigDecimal],
  shares: Input[BigDecimal],
  price: Input[BigDecimal],
  perf: Input[BigDecimal])

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
  maxBonus: Input[BigDecimal])

//case class CashCompensation(anualRecords: Seq[AnualRecord]) {
//  def currentTtdc = ???
//}

//TODO
//New 8-K Data	Base Salary
//	Target Bonus


