package output

import libt.spreadsheet._
import libt.reduction._
import libt._
import model.mapping._
import model.ExecutivesSTBonusPlan._
import model.ExecutivesGuidelines._
import libt.spreadsheet.reader.SheetDefinition

package object mapping {

  val usageAndSVTDataMapping =
    Seq[Strip](Gap) ++
      addTYears(
        Path('avgSharesOutstanding),
        Path('optionsSARs, 'granted),
        Path('optionsSARs, 'exPrice),
        Path('optionsSARs, 'cancelled),
        Path('fullValue, 'sharesGranted),
        Path('fullValue, 'grantPrice),
        Path('fullValue, 'sharesCancelled),
        Path('cashLTIP, 'grants),
        Path('cashLTIP, 'payouts))

  val executiveOwnershipMapping = Seq(
    Gap, //GICS Industry
    Gap,
    Gap,
    //Exec Data
    Feature('title),
    Feature('functionalMatches, 'primary),
    Feature('use),
    Feature('yearsToAchieve),
    Feature('numberOfShares),
    Feature('multipleOfSalary),
    Feature('retention, 'ratio)) ++
    TGuidelinesPeriod.values.map(value =>
      EnumCheck(Path('retention, 'period), value))
  val stBonusPlanOutputMapping = Seq(
    Gap, //GICS Industry
    Gap,
    Gap,
    //Exec Data
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

  val execDbOutputMapping = Seq(
    Gap,
    //Exec Data
    Feature('lastName),
    Feature('title),
    Gap, //Leave Blank
    Feature('functionalMatches, 'primary),
    Feature('founder),
    Gap, //TTDC Pay Rank Calculation

    //Cash Compensation
    //Current Year
    Gap,
    Feature('cashCompensations, 'baseSalary),
    Feature('cashCompensations, 'actualBonus),

    Gap, // TTDC Pay calculation
    Feature('cashCompensations, 'targetBonus),
    Feature('cashCompensations, 'thresholdBonus),
    Feature('cashCompensations, 'maxBonus),
    //New 8-K Data
    Feature('cashCompensations, 'nextFiscalYearData, 'baseSalary),
    Feature('cashCompensations, 'nextFiscalYearData, 'targetBonus),

    //Equity Comp Value
    Gap,
    Tag("Options Value", Calc(Sum(Path('optionGrants, *, 'value)))),
    Calc(Sum(Path('optionGrants, *, 'number))),
    Calc(Average(Path('optionGrants, *, 'price))),
    Gap, //Leave Blank
    //Time Vest RS
    Calc(Sum(Path('timeVestRS, *, 'value))),
    Calc(Sum(Path('timeVestRS, *, 'number))),
    Calc(Average(Path('timeVestRS, *, 'price))),
    //Perf RS
    Calc(Sum(Path('performanceVestRS, *, 'targetValue))),
    Calc(Sum(Path('performanceVestRS, *, 'targetNumber))),
    Calc(Average(Path('performanceVestRS, *, 'grantDatePrice))),
    //Perf Cash
    Calc(Sum(Path('performanceCash, *, 'targetValue))),

    //Carried Interest
    Gap,
    Calc(SubstractAll(
      Path('carriedInterest, 'ownedShares),
      Path('beneficialOwnership),
      Path('options),
      Path('unvestedRestrictedStock),
      Path('disclaimBeneficialOwnership))),
    Feature('carriedInterest, 'outstandingEquityAwards, 'vestedOptions),
    Feature('carriedInterest, 'outstandingEquityAwards, 'unvestedOptions),
    Feature('carriedInterest, 'outstandingEquityAwards, 'timeVestRS),
    Feature('carriedInterest, 'outstandingEquityAwards, 'perfVestRS))
}