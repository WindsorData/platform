package model.mapping

import libt.Path
import model._
import model.ExecutivesTop5._
import model.mapping._
import libt.spreadsheet.reader._
import libt.spreadsheet._
import libt._

object ExecutivesTop5Mapping {

  val executiveMapping =
    Seq[Strip](Path('firstName),
      Path('lastName),
      Path('title),
      Path('functionalMatches, 'primary),
      Path('functionalMatches, 'secondary),
      Path('functionalMatches, 'level),
      Path('functionalMatches, 'scope),
      Path('functionalMatches, 'bod),
      Path('founder),
      Path('transitionPeriod),

      Path('cashCompensations, 'baseSalary),
      Path('cashCompensations, 'actualBonus),
      Path('cashCompensations, 'retentionBonus),
      Path('cashCompensations, 'signOnBonus),
      Path('cashCompensations, 'targetBonus),
      Path('cashCompensations, 'thresholdBonus),
      Path('cashCompensations, 'maxBonus),
      Path('cashCompensations, 'nextFiscalYearData, 'baseSalary),
      Path('cashCompensations, 'nextFiscalYearData, 'targetBonus)) ++
      colOfModelsPath(Path('optionGrants), 5, 'grantDate, 'expireDate, 'number, 'price, 'value, 'perf, 'type) ++
      colOfModelsPath(Path('timeVestRS), 5, 'grantDate, 'number, 'price, 'value, 'type) ++
      colOfModelsPath(Path('performanceVestRS), 2, 'grantDate, 'targetNumber, 'grantDatePrice, 'targetValue, 'type) ++
      colOfModelsPath(Path('performanceCash), 2, 'grantDate, 'targetValue, 'payout) ++
      Seq[Strip](
        Path('carriedInterest, 'ownedShares, 'beneficialOwnership),
        Path('carriedInterest, 'ownedShares, 'options),
        Path('carriedInterest, 'ownedShares, 'unvestedRestrictedStock),
        Path('carriedInterest, 'ownedShares, 'disclaimBeneficialOwnership),
        Path('carriedInterest, 'ownedShares, 'heldByTrust),
        Path('carriedInterest, 'ownedShares, 'other),
        Path('carriedInterest, 'outstandingEquityAwards, 'vestedOptions),
        Path('carriedInterest, 'outstandingEquityAwards, 'unvestedOptions),
        Path('carriedInterest, 'outstandingEquityAwards, 'timeVestRS),
        Path('carriedInterest, 'outstandingEquityAwards, 'perfVestRS))

  val CompanyFiscalYearReader = new WorkbookReader(
    WorkbookMapping(
      Area(TCompanyFiscalYear, Offset(2, 2), None, RowOrientedLayout, Seq(Feature(Path('ticker)), Feature(Path('name))))
        #::
        AreaGap
        #::
        Stream.continually[SheetDefinition](Area(TExecutive, Offset(3, 1), Some(5), ColumnOrientedLayout, executiveMapping))),
    companyFiscalYearCombiner)
  
  def companyFiscalYearCombiner = DocSrcCombiner(25 -> 'executives, 40 -> 'executives, 55 -> 'executives)
}