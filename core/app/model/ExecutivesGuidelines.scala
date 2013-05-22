package model
import libt._

object ExecutivesGuidelines {
  import model.Commons._

  val TGuidelinesPeriod = TEnum(
    "Until guidelines are met",
    "Retention Period",
    "Entire employment",
    "No disclosure provided",
    "Other")

  val TExecGuidelines = TModel(
    'title -> TString,
    'functionalMatches -> functionalMatches,
    'use -> TXBool,
    'yearsToAchieve -> TNumber,
    'retention -> TModel(
      'ratio -> TNumber,
      'period -> TGuidelinesPeriod),
    'numberOfShares -> TNumber,
    'multipleOfSalary -> TNumber)

}