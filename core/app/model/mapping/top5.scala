package model.mapping

import libt.Path
import model._
import model.ExecutivesTop5._
import model.mapping._
import libt.spreadsheet.reader._
import libt.spreadsheet._
import libt.workflow._
import libt.util._
import libt._
import libt.error._

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

  override def Mapping =
    WorkbookMapping(
      Area(TCompanyFiscalYear, Offset(2, 2), None, RowOrientedLayout, Seq(Feature(Path('ticker)), Feature(Path('name))))
        #::
        Area(TGrantTypes, Offset(3, 1), Some(1), ColumnOrientedLayout, grantTypesMapping)
        #::
        Stream.continually[SheetDefinition](Area(TExecutive, Offset(3, 1), Some(5), ColumnOrientedLayout, executiveMapping)))

  override def CombinerPhase =
    DocSrcCombiner(
      (10, 'grantTypes, singleModelWrapping),
      (25, 'executives, colWrapping),
      (40, 'executives, colWrapping),
      (55, 'executives, colWrapping))

  override def ValidationPhase =
    (_, models) => {
      if (!models.concat.isInvalid) {
        models.map { model =>
          umatch(model) {
            case validModel @ Valid(m) => {
              grantTypeValidation(m) andThen top5Validation(m)
            }
          }
        }
      }
      else
        models
    }
    
  def top5Validation(model: Model): Validated[Model] = 
    if (model.hasElement('executives)) {
      val path = Path('executives, *, 'functionalMatches)
      val results : Seq[Validated[Model]] = model.applySeq(path)
      .zip(Seq("ExecDb", "ExecDb-1", "ExecDb-2")).map { case (m, tab) =>
        m.asModel(Path('primary)).asValue[String].value match {
          case Some(rol) =>
            if (rol == "CEO (Chief Executive Officer)"
              && m(Path('bod)).asValue[String].value.isEmpty)
              Doubtful(model, 
                  "Warning on " + tab + " - " 
                  + Path('bod).titles.mkString(" - ") 
                  + ": CEO with no BOD")
            else
              Valid(model)
          case None => 
            Valid(model)
        }
      }
      results.reduce( (a,b) => a andThen b)
    }
    else
      Valid(model)
      
  
  def grantTypeValidation(model: Model): Validated[Model] = {
    def validateGrantTypeUse(path: Path): Validated[Model] =
      umatch(model(path).asValue[Boolean].value) {
        case Some(use) =>
          if (!use || (use && model(path.init).asModel.without(path.last.routeValue).isComplete))
            validateGrantTypeMinPayout(model)
          else
            Invalid("Error on " + path.titles.mkString(" - ") + ": Incomplete data")
      }
    
    def validateGrantTypeMinPayout(model: Model) = {
      val path = Path('grantTypes, 'performanceEquityVesting, 'minPayout)
      model(path).asValue[Number].value match {
        case Some(value) =>
          if (value != 0)
            Doubtful(model,
              "Warning on " + path.titles.mkString(" - ") + ": value is not 0%")
          else
            Valid(model)
        case None =>
          Valid(model)
      }
    }

    if (model.hasElement('grantTypes)) {
      validateGrantTypeUse(Path('grantTypes, 'stockOptions, 'use))
    }
    else
      Valid(model)
  }
}