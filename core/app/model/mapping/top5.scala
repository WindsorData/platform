package model.mapping

import libt.Path
import model._
import model.ExecutivesTop5._
import model.mapping._
import libt.spreadsheet.reader._
import libt.spreadsheet._
import libt.workflow._
import libt._

object top5 extends WorkflowFactory {

  def performanceVestingMapping(rootPath: Symbol) =
    Seq[Strip](Path(rootPath, 'useShares),
      Path(rootPath, 'neos),
      Path(rootPath, 'performance, 'period),
      Path(rootPath, 'performance, 'interval),
      Path(rootPath, 'timeVest, 'period),
      Path(rootPath, 'timeVest, 'vesting),
      Path(rootPath, 'minPayout),
      Path(rootPath, 'maxPayout)) ++
      Multi(Path(rootPath, 'metrics), 4, Path('select), Path('typeIn))

  val grantTypesMapping =
    Seq[Strip](
      Path('stockOptions, 'use),
      Path('stockOptions, 'neos),
      Path('stockOptions, 'maxTerm),
      Path('stockOptions, 'frecuency),
      Path('stockOptions, 'years),
      Path('stockOptions, 'vesting),
      Path('timeVestingRestrictedShares, 'use),
      Path('timeVestingRestrictedShares, 'neos),
      Path('timeVestingRestrictedShares, 'years),
      Path('timeVestingRestrictedShares, 'vesting)) ++
      performanceVestingMapping('performanceEquityVesting) ++
      performanceVestingMapping('performanceCashVesting)

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
      Multi(Path('optionGrants), 5, 
          Path('grantDate), 
          Path('expireDate), 
          Path('number), 
          Path('price), 
          Path('value), 
          Path('perf), 
          Path('type)) ++
      Multi(Path('timeVestRS), 5, 
          Path('grantDate), 
          Path('number), 
          Path('price), 
          Path('value), 
          Path('type)) ++
      Multi(Path('performanceVestRS), 2,
          Path('grantDate), 
          Path('targetNumber), 
          Path('grantDatePrice), 
          Path('targetValue), 
          Path('type)) ++
      Multi(Path('performanceCash), 2, 
          Path('grantDate), 
          Path('targetValue), 
          Path('payout)) ++
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

  def  Mapping = 
    WorkbookMapping(
      Area(TCompanyFiscalYear, Offset(2, 2), None, RowOrientedLayout, Seq(Feature(Path('ticker)), Feature(Path('name))))
        #::
        Area(TGrantTypes, Offset(3, 1), Some(1), ColumnOrientedLayout, grantTypesMapping)
        #::
        Stream.continually[SheetDefinition](Area(TExecutive, Offset(3, 1), Some(5), ColumnOrientedLayout, executiveMapping)))
  
  def CombinerPhase =
    DocSrcCombiner(
      (10, 'grantTypes, singleModelWrapping),
      (25, 'executives, colWrapping),
      (40, 'executives, colWrapping),
      (55, 'executives, colWrapping))
}