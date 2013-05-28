package model

import libt._

object ExecutivesTop5 {
  import model.Commons._

  /**
   * Sheet Grant Types
   */

  val TNeos = TGenericEnum(TNumber, Range.BigDecimal(1, 5, 1))
  val TMaxTerm = TGenericEnum(TNumber, Range.BigDecimal(0, 10, 1))
  val TFrecuency = TGenericEnum(TNumber, Range.BigDecimal(1, 4, 1))
  val TYears = TGenericEnum(TNumber, Range.BigDecimal(1, 8, 1))
  val TPeriod = TGenericEnum(TNumber, Range.BigDecimal(1, 8, 1))
  val TInterval = TGenericEnum(TNumber, Range.BigDecimal(1, 8, 1).+:(0.5: BigDecimal))

  val TVesting = TEnum("Annual",
    "Semi-annual",
    "Quarterly",
    "Monthly",
    "Combo",
    "Cliff",
    "Other (not combo)")

  val TMetrics = TCol(TModel(
    'select -> TEnum(
          "Revenue",
          "TSR",
          "EPS",
          "ROE",
          "Operating Income",
          "Net Income",
          "EBITDA",
          "EBIT",
          "ROA",
          "Cash Flow",
          "ROI",
          "Working Capital"),
    'typeIn -> TString))

  val TPerformanceVesting = TModel(
    'useShares -> TXBool,
    'neos -> TNeos,
    'performance -> TModel(
      'period -> TPeriod,
      'interval -> TInterval),
    'timeVest -> TModel(
      'period -> TPeriod,
      'vesting -> TVesting),
    'minPayout -> TNumber,
    'maxPayout -> TNumber,
    'metrics -> TMetrics)

  val TGrantTypes = TModel(
    'stockOptions -> TModel(
      'use -> TXBool,
      'neos -> TNeos,
      'maxTerm -> TMaxTerm,
      'frecuency -> TFrecuency,
      'years -> TYears,
      'vesting -> TVesting),
    'timeVestingRestrictedShares -> TModel(
      'use -> TXBool,
      'neos -> TNeos,
      'years -> TYears,
      'vesting -> TVesting),
    'performanceEquityVesting -> TPerformanceVesting,
    'performanceCashVesting -> TPerformanceVesting)

    
  /**
   * Sheet ExecDB
   */
    
  val TTypes = TEnum("Annual", "Retention", "Hire", "Promotion", "Special", "Acquisition", "Other")

  val TExecutive = TModel(
    'firstName -> TString,
    'lastName -> TString,
    'title -> TString,
    'functionalMatches -> functionalMatches,
    'founder -> TString,
    'transitionPeriod -> TString,

    'cashCompensations -> TModel(
      'baseSalary -> TNumber,
      'actualBonus -> TNumber,
      'retentionBonus -> TNumber,
      'signOnBonus -> TNumber,
      'targetBonus -> TNumber,
      'thresholdBonus -> TNumber,
      'maxBonus -> TNumber,
      'nextFiscalYearData -> TModel(
        'baseSalary -> TNumber,
        'targetBonus -> TNumber)),

    'optionGrants -> TCol(
      TModel(
        'grantDate -> TDate,
        'expireDate -> TDate,
        'number -> TNumber,
        'price -> TNumber,
        'value -> TNumber,
        'perf -> TXBool,
        'type -> TTypes)),

    'timeVestRS -> TCol(
      TModel(
        'grantDate -> TDate,
        'number -> TNumber,
        'price -> TNumber,
        'value -> TNumber,
        'type -> TTypes)),

    'performanceVestRS -> TCol(
      TModel(
        'grantDate -> TDate,
        'targetNumber -> TNumber,
        'grantDatePrice -> TNumber,
        'targetValue -> TNumber,
        'type -> TTypes)),

    'performanceCash -> TCol(
      TModel(
        'grantDate -> TDate,
        'targetValue -> TNumber,
        'payout -> TNumber)),

    'carriedInterest -> TModel(
      'ownedShares -> TModel(
        'beneficialOwnership -> TNumber,
        'options -> TNumber,
        'unvestedRestrictedStock -> TNumber,
        'disclaimBeneficialOwnership -> TNumber,
        'heldByTrust -> TNumber,
        'other -> TString),
      'outstandingEquityAwards -> TModel(
        'vestedOptions -> TNumber,
        'unvestedOptions -> TNumber,
        'timeVestRS -> TNumber,
        'perfVestRS -> TNumber)))

}
