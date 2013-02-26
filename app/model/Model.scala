package model
import util.Todo._

case class CarriedInterest(
  ownedShares: Number,
  vestedOptions: Number,
  carriedInterest: Number,
  unvestedOptions: Number,
  tineVest: Number,
  perfVest: Number)

case class Execution(name: String,
  title: String,
  shortTitle: String,
  functionalMatch: ???,
  founder: Boolean) {

  def tdcPayRank: Number = ???
}

case class AnualRecord(baseSalary: Number,
  actualBonus: Number,
  targetBonus: Number,
  thresholdBonus: Option[Number],
  maxBonus: Option[Number])

case class CashCompensation(anualRecords: Seq[AnualRecord]) {
  def currentTtdc = ???
}

//TODO
//New 8-K Data	Base Salary
//	Target Bonus
	