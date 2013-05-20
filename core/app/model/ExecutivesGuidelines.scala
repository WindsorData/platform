package model
import libt._

object ExecutivesGuidelines {
  import model.Commons._
  val TExecGuidelines = TModel(
    'title -> TString,
    'functionalMatches -> functionalMatches,
    'use -> TXBool,
    'yearsToAchieve -> TNumber,
    'retention -> TModel(
      'ratio -> TNumber,
      //TODO: change this to be a TEnum
      'period -> TString),
    'numberOfShares -> TNumber,
    'multipleOfSalary -> TNumber)

  val GuidelinesPeriod = TEnum(
    "Until guidelines are met",
    "Retention Period",
    "Entire employment",
    "No disclosure provided",
    "Other")
}