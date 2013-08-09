package model

import libt._

object ExecutivesTop5 {
  import model.Commons._

  /**
   * Sheet Grant Types
   */

  val TNeos = TEnum(TNumber, Range.BigDecimal(1, 5, 1))
  val TMaxTerm = TEnum(TNumber, Range.BigDecimal(0, 10, 1))
  val TFrecuency = TEnum(TNumber, Range.BigDecimal(1, 4, 1))
  val TYears = TEnum(TNumber, Range.BigDecimal(1, 8, 1))
  val TPeriod = TEnum(TNumber, Range.BigDecimal(1, 8, 1))
  val TInterval = TEnum(TNumber, Range.BigDecimal(1, 8, 1).+:(0.5: BigDecimal))

  val TVesting = TStringEnum("Annual",
    "Semi-annual",
    "Quarterly",
    "Monthly",
    "Combo",
    "Cliff",
    "Other (not combo)")

  val TMetricsSelect = TStringEnum(
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
          "Working Capital")
          
  val TMetrics = TCol(
    TModel(
      'select -> TMetricsSelect,
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
    
  val TTypes = TStringEnum("Annual", "Retention", "Hire", "Promotion", "Special", "Acquisition", "Other")

  val TExecutive = TModel(
    'firstName -> TString,
    'lastName -> TString,
    'title -> TString,
    'functionalMatches -> TFunctionalMatch,
    'founder -> TString,
    'transitionPeriod -> TString,

    'cashCompensations -> TModel(
      'baseSalary -> TNumber,
      'actualBonus -> TNumber,
      'ttdc -> TNumber,
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
        'other -> TAny),
      'outstandingEquityAwards -> TModel(
        'vestedOptions -> TNumber,
        'unvestedOptions -> TNumber,
        'timeVestRS -> TNumber,
        'perfVestRS -> TNumber)),

     'calculated -> TModel(
       'ttdc -> TNumber,
       'ttdcPayRank -> TInt,
       'salaryAndBonus -> TNumber,
       'carriedInterest -> TModel('ownedShares -> TNumber),
       'equityCompValue -> TModel(
         'options -> TModel(
           'value -> TNumber,
           'options -> TNumber,
           'exPrice -> TNumber),
         'timeVestRs -> TModel(
           'value -> TNumber,
           'shares -> TNumber,
           'price -> TNumber),
         'perfRs -> TModel(
           'value -> TNumber,
           'shares -> TNumber,
           'price -> TNumber),
         'perfCash -> TNumber)))

}
