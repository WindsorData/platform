package model
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

//TODO
//New 8-K Data	Base Salary
//	Target Bonus


