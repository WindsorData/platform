package model

import libt._

object ExecutivesSTBonusPlan {

  import model.Commons._

  val TBonusType = TEnum("Target",
    "Discretionary",
    "Commission",
    "Shared Profit-Pool",
    "No Plan",
    "Other")

  val TPayoutFrecuency = TEnum("Annual",
    "Semi-Annual",
    "Quarterly",
    "Other")

  val TPerfPeriod = TGenericEnum[BigDecimal](TNumber,Seq(
    0.25,
    0.5,
    0.75,
    1,
    1.25,
    1.5,
    1.75,
    2,
    2.25,
    2.5,
    2.75,
    3))

  val TWeightedUse = TModel(
    'use -> TXBool,
    'weight -> TNumber)

  val TTypeOptions = TEnum("Revenue",
    "EPS",
    "EBITDA",
    "Operating Income",
    "Net Income",
    "ROIC-ROE",
    "Cash Flows")

  val TSelectWeightedType = TCol(
    TModel(
      'use -> TTypeOptions,
      'weight -> TNumber))

  val TTypeInWeightedType = TCol(
    TModel(
      'type -> TString,
      'weight -> TNumber))

  val TExecSTBonusPlan = TModel(
    'title -> TString,
    'functionalMatches -> TFunctionalMatch,
    'useCash -> TXBool,
    'useShares -> TXBool,
    'bonusType -> TBonusType,
    'thresholdTarget -> TNumber,
    'maxTarget -> TNumber,
    'perfPeriod -> TPerfPeriod,
    'payoutFrecuency -> TPayoutFrecuency,
    'scope -> TModel(
      'corporate -> TWeightedUse,
      'busUnit -> TWeightedUse,
      'individual -> TWeightedUse),
    'metrics -> TModel(
      'select -> TSelectWeightedType,
      'typeIn -> TTypeInWeightedType))
}