package model

import libt._

object ExecutivesSVTBSDilution {

  val TYears = TModel(
    'year1 -> TNumber,
    'year2 -> TNumber,
    'year3 -> TNumber)

  /**
   * Sheet Usage and SVT Data
   */
  val TUsageAndSVTData = TModel(
    'avgSharesOutstanding -> TYears,
    'optionsSARs -> TModel(
      'granted -> TYears,
      'exPrice -> TYears,
      'cancelled -> TYears),
    'fullValue -> TModel(
      'sharesGranted -> TYears,
      'grantPrice -> TYears,
      'sharesCancelled -> TYears),
    'cashLTIP -> TModel(
      'grants -> TYears,
      'payouts -> TYears))

  /**
   * Sheet Usage and SVT Data
   */

  val TValuationModel = TStringEnum(
    "Valuation Model",
    "Black-Scholes",
    "Monte Carlo",
    "Binomial (Lattice)",
    "Other")
    
  val TBlackScholesInputs = TModel(
    'valuationModel -> TModel(
      'year1 -> TValuationModel,
      'year2 -> TValuationModel,
      'year3 -> TValuationModel),
    'volatility -> TYears,
    'expectedTerm -> TYears,
    'riskFreeRate -> TYears,
    'dividendYield -> TYears,
    'bs -> TYears)

  /**
   * Sheet Dilution and ISS SVT Data
   */
  val TDilution = TModel(
    'awardsOutstandings -> TModel(
      'option -> TNumber,
      'fullValue -> TNumber,
      'total -> TNumber),
    'sharesAvailable -> TModel(
      'current -> TNumber,
      'new -> TNumber,
      'everGreen -> TModel(
        'anual -> TNumber,
        'yearsLeft -> TNumber),
      'fungible -> TModel(
        'ratio -> TNumber,
        'fullValue -> TNumber)))
}