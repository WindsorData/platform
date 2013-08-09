package output

import _root_.mapping._
import libt.spreadsheet._
import libt._
import model.mapping._
import model.ExecutivesSTBonusPlan._
import model.ExecutivesGuidelines._
import model.ExecutivesTop5._
import libt.spreadsheet.EnumCheck
import libt.spreadsheet.ComplexEnumCheck

package object mapping {

  trait StandardMapping
    extends DilutionMappingComponent
    with Top5MappingComponent
    with GuidelinesMappingComponent {

  def performanceVestingMapping(base: Symbol) =
    Seq[Strip](
      Path(base, 'useShares),
      Path(base, 'neos),
      Path(base, 'performance, 'period),
      Path(base, 'performance, 'interval),
      Path(base, 'timeVest, 'period)) ++
      TVesting.values.map(value =>
        EnumCheck(Path(base, 'timeVest, 'vesting), value)) ++
      Seq[Strip](
        Path(base, 'minPayout),
        Path(base, 'maxPayout)) ++
        TMetricsSelect.values.map(value =>
          EnumCheck(Path(base, 'metrics, *, 'select), value)) ++
        Seq[Strip](
          Path(base, 'metrics, 0, 'typeIn),
          Path(base, 'metrics, 1, 'typeIn),
          Path(base, 'metrics, 2, 'typeIn),
          Path(base, 'metrics, 3, 'typeIn))

  val grantTypesMapping =
    Seq[Strip](
      Gap,
      Path('stockOptions, 'use),
      Path('stockOptions, 'neos),
      Path('stockOptions, 'maxTerm),
      Path('stockOptions, 'frecuency),
      Path('stockOptions, 'years)) ++
      TVesting.values.map(value =>
        EnumCheck(Path('stockOptions, 'vesting), value)) ++
      Seq[Strip](
        Path('timeVestingRestrictedShares, 'use),
        Path('timeVestingRestrictedShares, 'neos),
        Path('timeVestingRestrictedShares, 'years)) ++
        TVesting.values.map(value =>
          EnumCheck(Path('timeVestingRestrictedShares, 'vesting), value)) ++
        performanceVestingMapping('performanceEquityVesting) ++
        performanceVestingMapping('performanceCashVesting)

  val dilutionMapping = {
    def Path(ps:PathPart*) = RelativeTo('dilution)(ps)
    Seq[Strip](
      Path('awardsOutstandings, 'option),
      Path('awardsOutstandings, 'fullValue),
      Path('awardsOutstandings, 'total),
      Path('sharesAvailable, 'current),
      Path('sharesAvailable, 'new),
      Path('sharesAvailable, 'everGreen, 'anual),
      Path('sharesAvailable, 'everGreen, 'yearsLeft),
      Path('sharesAvailable, 'fungible, 'ratio),
      Path('sharesAvailable, 'fungible, 'fullValue))
  }

  val bsInputsMapping = {
    def Path(ps:PathPart*) = RelativeTo('bsInputs)(ps)
    Seq[Strip](
      Path('valuationModel, 'year1),
      Path('valuationModel, 'year2),
      Path('valuationModel, 'year3)) ++
      Years(
        Path('volatility),
        Path('expectedTerm),
        Path('riskFreeRate),
        Path('dividendYield),
        Path('bs))
  }

  val usageAndSVTDataMapping = {
    def Path(ps:PathPart*) = RelativeTo('usageAndSVTData)(ps)
    Years(
      Path('avgSharesOutstanding),
      Path('optionsSARs, 'granted),
      Path('optionsSARs, 'exPrice),
      Path('optionsSARs, 'cancelled),
      Path('fullValue, 'sharesGranted),
      Path('fullValue, 'grantPrice),
      Path('fullValue, 'sharesCancelled),
      Path('cashLTIP, 'grants),
      Path('cashLTIP, 'payouts))
  }

  val guidelinesMapping = Seq(
    Gap, //GICS Industry
    Gap,
    //Exec Data
    Feature('lastName),
    Feature('title),
    Feature('functionalMatches, 'primary),
    Feature('use),
    Feature('yearsToAchieve),
    Feature('numberOfShares),
    Feature('multipleOfSalary),
    Feature('retention, 'ratio)) ++
    TGuidelinesPeriod.values.map(value =>
      EnumCheck(Path('retention, 'period), value))

  val stBonusPlanMapping = Seq(
    Gap, //GICS Industry
    Gap,
    //Exec Data
    Feature('lastName),
    Feature('title),
    Feature('functionalMatches, 'primary),
    Feature('useCash),
    Feature('useShares),
    Feature('bonusType),
    Gap,
    //Short-Term Bonus Plan
    Feature('thresholdTarget),
    Feature('maxTarget),
    Feature('perfPeriod),
    Feature('payoutFrecuency),
    Feature('scope, 'corporate, 'use),
    Feature('scope, 'corporate, 'weight),
    Feature('scope, 'busUnit, 'use),
    Feature('scope, 'busUnit, 'weight),
    Feature('scope, 'individual, 'use),
    Feature('scope, 'individual, 'weight)) ++
    TTypeOptions.values.flatMap(value =>
      Seq(EnumCheck(Path('metrics, 'select, *, 'use), value),
        ComplexEnumCheck(Path('metrics, 'select, *), Path('use), Path('weight), value))) ++
    Seq(
      Feature('metrics, 'typeIn, 0, 'type),
      Feature('metrics, 'typeIn, 0, 'weight),
      Feature('metrics, 'typeIn, 1, 'type),
      Feature('metrics, 'typeIn, 1, 'weight))

  val executiveMapping = Seq(
    Gap,
    //Exec Data
    Feature('lastName),
    Feature('title),
    Gap, //Leave Blank
    Feature('functionalMatches, 'primary),
    Feature('founder),
    Feature('calculated, 'ttdcPayRank),

    //Cash Compensation
    //Current Year
    Gap,
    Feature('cashCompensations, 'baseSalary),
    Feature('cashCompensations, 'actualBonus),

    Feature('calculated, 'ttdc),
    Feature('cashCompensations, 'targetBonus),
    Feature('cashCompensations, 'thresholdBonus),
    Feature('cashCompensations, 'maxBonus),
    //New 8-K Data
    Feature('cashCompensations, 'nextFiscalYearData, 'baseSalary),
    Feature('cashCompensations, 'nextFiscalYearData, 'targetBonus),

    //Equity Comp Value
    Gap,
    Feature('calculated, 'equityCompValue, 'options, 'value),
    Feature('calculated, 'equityCompValue, 'options, 'options),
    Feature('calculated, 'equityCompValue, 'options, 'exPrice),
    Gap, //Leave Blank
    //Time Vest RS
    Feature('calculated, 'equityCompValue, 'timeVestRs, 'value),
    Feature('calculated, 'equityCompValue, 'timeVestRs, 'shares),
    Feature('calculated, 'equityCompValue, 'timeVestRs, 'price),
    //Perf RS
    Feature('calculated, 'equityCompValue, 'perfRs, 'value),
    Feature('calculated, 'equityCompValue, 'perfRs, 'shares),
    Feature('calculated, 'equityCompValue, 'perfRs, 'price),
    //Perf Cash
    Feature('calculated, 'equityCompValue, 'perfCash),

    //Carried Interest
    Gap,
    Feature('calculated, 'carriedInterest, 'ownedShares),
    Feature('carriedInterest, 'outstandingEquityAwards, 'vestedOptions),
    Feature('carriedInterest, 'outstandingEquityAwards, 'unvestedOptions),
    Feature('carriedInterest, 'outstandingEquityAwards, 'timeVestRS),
    Feature('carriedInterest, 'outstandingEquityAwards, 'perfVestRS))
  }
}