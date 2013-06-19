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
              grantTypeValidation(m) andThen 
              executivesValidation(m)
            }
          }
        }
        
      }
      else
        models
    }
    
    def execMsg(year: Int, m: Model) = 
      year + " - " + m(Path('firstName)).getRawValue[String] + m(Path('lastName)).getRawValue[String] + " - "
      
    def reduceExecutiveValidations(model: Model)(action: (Element) => Validated[Model]) = {
      val results: Seq[Validated[Model]] = model.applySeq(Path('executives, *)) .map { m => action(m)  }
      results.reduce( (a,b) => a andThen b)
    }
    
    def nextFiscalYearDataValidation(model: Model): Validated[Model] = 
      reduceExecutiveValidations(model)(
        (m) =>
          (m(Path('cashCompensations, 'baseSalary)).rawValue[BigDecimal], 
           m(Path('cashCompensations, 'nextFiscalYearData, 'baseSalary)).rawValue[BigDecimal],
           m(Path('cashCompensations, 'targetBonus)).rawValue[BigDecimal],
           m(Path('cashCompensations, 'nextFiscalYearData, 'targetBonus)).rawValue[BigDecimal]) match {
            case (Some(baseSalary), Some(nextBaseSalary), Some(targetBonus), Some(nextTargetBonus))
            if (baseSalary <= nextBaseSalary && targetBonus <= nextTargetBonus) =>
              Valid(model)
            case (_,None, _, None) => Valid(model) 
            case (_,_,_,_) =>
              Doubtful(model,
                  "Warning on ExecDb " + execMsg(model(Path('disclosureFiscalYear)).getRawValue[Int], m.asModel) + 
                  ": current base salary and target bonus are equal or greater that next fiscal year data")
          })
    

    def bodValidation(model: Model): Validated[Model] =
      reduceExecutiveValidations(model)(
        (m) =>
          (m(Path('functionalMatches, 'primary)).rawValue[String],
            m(Path('functionalMatches, 'bod)).rawValue[String].isEmpty) match {
              case (Some("CEO (Chief Executive Officer)"), true) =>
                Doubtful(model,
                  "Warning on ExecDb " + model(Path('disclosureFiscalYear)).getRawValue[Int] + " - "
                    + Path('bod).titles.mkString(" - ")
                    + ": CEO with no BOD")
              case (_, _) => Valid(model)
            })

    def founderValidation(model: Model): Validated[Model] =
      reduceExecutiveValidations(model)(
        (m) =>
          (m(Path('cashCompensations, 'baseSalary)).rawValue[BigDecimal],
            m(Path('founder)).rawValue[Boolean]) match {
              case (Some(baseSalary), None) if baseSalary == 1 =>
                Doubtful(model,
                  "Warning on ExecDb " + model(Path('disclosureFiscalYear)).getRawValue[Int] + " - "
                    + Path('founder).titles.mkString(" - ")
                    + ": base salary is 1 and executive is not a founder")
              case (_, _) =>
                Valid(model)
            })
    
    def perfCashValidation(model: Model): Validated[Model] =
      reduceExecutiveValidations(model)(
        (m) => {
          val results: Seq[Validated[Model]] =
            m.applySeq(Path('performanceCash, *))
              .map { perfCash =>
                (perfCash('targetValue).rawValue[BigDecimal],
                  perfCash('payout).rawValue[BigDecimal]) match {
                    case (Some(_), Some(_)) =>
                      Invalid("Error on ExecDb " + execMsg(model(Path('disclosureFiscalYear)).getRawValue[Int], m.asModel) +
                        ": in PerfCash Both target value and Payout can’t be filled for the same grant")
                    case (_, _) => Valid(model)
                  }
              }
          results.reduce((a, b) => a andThen b)
        })
   
    def ownedSharesValidation(model: Model): Validated[Model] =
      reduceExecutiveValidations(model)(
        (m) => {
          val ownedShares = m(Path('carriedInterest, 'ownedShares)).asModel
          val beneficial = ownedShares(Path('beneficialOwnership)).rawValue[BigDecimal]
          val results: Seq[Validated[Model]] =
            Seq(ownedShares(Path('options)).rawValue[BigDecimal],
              ownedShares(Path('unvestedRestrictedStock)).rawValue[BigDecimal],
              ownedShares(Path('disclaimBeneficialOwnership)).rawValue[BigDecimal],
              ownedShares(Path('heldByTrust)).rawValue[BigDecimal]).zip(Stream.continually(beneficial))
              .map {
                case (Some(value), Some(benef)) if value > benef =>
                  Doubtful(model,
                    "Warning on ExecDb " + execMsg(model(Path('disclosureFiscalYear)).getRawValue[Int], m.asModel) +
                      ": Some Owned Shares values cannot be greater than Beneficial Ownership")
                case (_, _) => Valid(model)
              }
          results.reduce((a, b) => a andThen b)
        })
     
    def salaryValidation(model: Model): Validated[Model] = 
      reduceExecutiveValidations(model)(
        (m) => 
          m(Path('cashCompensations, 'baseSalary)).rawValue[BigDecimal] match {
            case Some(salary) if salary.compare(BigDecimal(100)) < 0 =>
              Doubtful(model,
                    "Warning on ExecDb " + execMsg(model(Path('disclosureFiscalYear)).getRawValue[Int], m.asModel) +
                      ": Base Salary should be 3 digits or more")
            case _ => Valid(model)
          })

    def optionGrantsValidation(model: Model): Validated[Model] =
      reduceExecutiveValidations(model)(
        (m) => {
          val results: Seq[Validated[Model]] =
            m.applySeq(Path('optionGrants, *))
              .map { grant =>
                val elements = grant.asModel.without('perf).elements
                if (elements.forall(!_._2.asValue.value.isEmpty)
                  || elements.forall(_._2.asValue.value.isEmpty))
                  Valid(model)
                else
                  Doubtful(model,
                    "Warning on ExecDb " + execMsg(model(Path('disclosureFiscalYear)).getRawValue[Int], m.asModel) +
                      ": Option grants ")
              }

          results.reduce((a, b) => a andThen b)
        })


  def executivesValidation(model: Model): Validated[Model] = {
    if (model.hasElement('executives))
      founderValidation(model) andThen 
      bodValidation(model) andThen 
      nextFiscalYearDataValidation(model) andThen
      perfCashValidation(model) andThen
      ownedSharesValidation(model) andThen
      salaryValidation(model)
    else
      Valid(model)

  }
  
  def grantTypeValidation(model: Model): Validated[Model] = {
    def validateGrantTypeUse: Validated[Model] = {
      val path = Path('grantTypes, 'stockOptions, 'use)
      val use = model(path).getRawValue[Boolean]
      if (!use || model(path.init).asModel.without(path.last.routeValue).isComplete)
        Valid(model)
      else
        Invalid("Error on " + path.titles.mkString(" - ") + ": Incomplete data")
    }
    
    def validateGrantTypeMinPayout = {
      val path = Path('grantTypes, 'performanceEquityVesting, 'minPayout)
      model(path).rawValue[Number] match {
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
      validateGrantTypeUse andThen validateGrantTypeMinPayout 
    }
    else
      Valid(model)
  }
}
