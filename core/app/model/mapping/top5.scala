package model.mapping

import model.mapping.generic._
import model.ExecutivesTop5._
import model.validation._
import model._

import libt.spreadsheet.reader._
import libt.spreadsheet._
import libt.calc._
import libt.error._
import libt.util.math._
import libt.builder.ModelBuilder
import libt._

import java.util.Date
import org.joda.time.DateTime
import libt.spreadsheet.reader.workflow._
import scala.Some
import libt.reduction.{SubstractAll, Reduction, Average, Sum}
import libt.spreadsheet.reader.Area
import libt.spreadsheet.reader.WorkbookMapping
import libt.spreadsheet.Offset
import scala.Some
import libt.spreadsheet.reader.Area
import libt.spreadsheet.reader.WorkbookMapping
import libt.spreadsheet.Offset
import scala.Some
import libt.Col
import libt.spreadsheet.reader.Area
import libt.spreadsheet.reader.WorkbookMapping
import libt.spreadsheet.Offset

object top5 extends StandardWorkflowFactory {

  def performanceVestingMapping(rootPath: Symbol) =
    Seq[Strip](Path(rootPath, 'useShares),
      Path(rootPath, 'neos),
      Path(rootPath, 'performance, 'period),
      Path(rootPath, 'performance, 'interval),
      Path(rootPath, 'timeVest, 'period),
      Path(rootPath, 'timeVest, 'vesting),
      Path(rootPath, 'minPayout),
      Path(rootPath, 'maxPayout)) ++
      Multi(Path(rootPath, 'metrics), 3, Path('select)) ++
      Multi(Path(rootPath, 'metrics), 3, Path('typeIn))

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
      Area(TCompanyFiscalYear, Offset(1, 2), None, DocSrcLayout, DocSrcMapping)
        #::
        Area(TGrantTypes, Offset(3, 1), Some(1), DataLayout, grantTypesMapping)
        #::
        Stream.continually[SheetDefinition](Area(TExecutive, Offset(3, 1), Some(5), DataLayout, executiveMapping)))

  override def CombinerPhase =
    DocSrcCombiner(
      (10, 'grantTypes, singleModelWrapping),
      (25, 'executives, colWrapping),
      (40, 'executives, colWrapping),
      (55, 'executives, colWrapping))

  override def AgreggationPhase : Phase[Seq[Model], Seq[Model]] =
    (_, models) =>
      Valid(models.map { model =>
        if(model.contains('executives)) {
          val execsWithCalcs = model.applySeq(Path('executives, *)).map { m => {
              def equityCalc(path: Path, calc: Reduction, elem: Element) =
                (Path('calculated, 'equityCompValue) ++ path, Value(calc.reduce(elem)))

              def calculateTTDC(mTtdc: Model): BigDecimal =
                ((if(mTtdc / 'cashCompensations /% 'targetBonus isEmpty) 1: BigDecimal
                else ((mTtdc / 'cashCompensations /% 'targetBonus get) + 1)) *
                  (mTtdc / 'cashCompensations /% 'baseSalary getOrElse(0: BigDecimal))) +
                  (mTtdc / 'calculated / 'equityCompValue / 'options /% 'value getOrElse(0: BigDecimal)) +
                  (mTtdc / 'calculated / 'equityCompValue / 'timeVestRs /% 'value getOrElse(0: BigDecimal)) +
                  (mTtdc / 'calculated / 'equityCompValue / 'perfRs /% 'value getOrElse(0: BigDecimal)) +
                  (mTtdc / 'calculated / 'equityCompValue /% 'perfCash getOrElse(0: BigDecimal))


              val builder = new ModelBuilder()
              builder += equityCalc(Path('options, 'value), Sum(Path('optionGrants, *, 'value)), m)
              builder += equityCalc(Path('options, 'options), Sum(Path('optionGrants, *, 'number)), m)
              builder += equityCalc(Path('options, 'exPrice), Average(Path('optionGrants, *, 'price)), m)

              builder += equityCalc(Path('timeVestRs, 'value), Sum(Path('timeVestRS, *, 'value)), m)
              builder += equityCalc(Path('timeVestRs, 'shares), Sum(Path('timeVestRS, *, 'number)), m)
              builder += equityCalc(Path('timeVestRs, 'price), Average(Path('timeVestRS, *, 'price)), m)

              builder += equityCalc(Path('perfRs, 'value), Sum(Path('performanceVestRS, *, 'targetValue)), m)
              builder += equityCalc(Path('perfRs, 'shares), Sum(Path('performanceVestRS, *, 'targetNumber)), m)
              builder += equityCalc(Path('perfRs, 'price), Average(Path('performanceVestRS, *, 'grantDatePrice)), m)

              builder += equityCalc(Path('perfCash), Sum(Path('performanceCash, *, 'targetValue)), m)

              builder += (Path('calculated, 'carriedInterest, 'ownedShares),
                Value(SubstractAll(
                  Path('carriedInterest, 'ownedShares),
                  Path('beneficialOwnership),
                  Path('options),
                  Path('unvestedRestrictedStock),
                  Path('disclaimBeneficialOwnership)).reduce(m)))

              val modelWithPartialCalcs = m.asModel ++ builder.build

              modelWithPartialCalcs.merge(Model('calculated -> Model('ttdc -> Value(calculateTTDC(modelWithPartialCalcs)))))
            }
          }

          val ttdcRankings = execsWithCalcs.sortBy(_ / 'calculated /%/ 'ttdc)
                              .reverse.map(it => it /!/ 'firstName -> it /!/ 'lastName).zip(1 to 5)

          model.merge(Model('executives -> Col(execsWithCalcs.map { exec =>
            val rank = ttdcRankings.find(_._1 == exec /!/ 'firstName -> exec /!/ 'lastName).get._2
            exec.merge(Model('calculated -> Model('ttdcPayRank -> Value(rank))))
          }: _*)))
        }
        else
          model
      }.map(_.asModel))

  override def SheetValidation = model =>
    grantTypeValidation(model) andThen
    executivesValidation(model)

  def gzip(sheets: Seq[Seq[Validated[Model]]]): Seq[Seq[Validated[Model]]] = sheets match {
        case m1 :: m2 :: Nil => m1.zip(m2).map { case (x, y) => Seq(x,y)}
        case head :: tail => head.zip(gzip(tail)).map { case (x, xs) => x +: xs }
      }
  
  override def WorkbookValidationPhase =
    (_, models) =>
    	  if(models.nonEmpty)
          gzip(Seq(
    	      transitionPeriodValidation(models),
    	      optionGrantsVsGrantType(models),
            execDbTimeVestValidation(models)))
    	  .concatMap(_.reduce((a, b) => a andThen b))
        else
          Valid(models)

  def execDbTimeVestValidation(models: Seq[Model]) = {
    val lastYear = models.maxBy(_ /#/ 'disclosureFiscalYear) /#/ 'disclosureFiscalYear
    models.map { model =>
      model.validateWhen(model.contains('executives) && ( model /#/ 'disclosureFiscalYear) == lastYear) {
        val results: Seq[Validated[Model]] =
          model.applySeq(Path('executives, *)).map { exec =>
            val hasSomeGrantDate = exec.applySeq(Path('timeVestRS, *, 'grantDate)).flatMap(_.rawValue[Date]).nonEmpty
            val hasNoTimeVestRs = ( exec / 'carriedInterest / 'outstandingEquityAwards /# 'timeVestRS).isEmpty
            model.validateWhen(hasSomeGrantDate && hasNoTimeVestRs) {
              Doubtful(model,
                warning("ExecDb - " + execMsg((model /#/ 'disclosureFiscalYear), exec.asModel) +
                  "Carried Interest - Outstanding Equity Awards - Time Vest RS",
                  "should not be blank if there's some time vest rs - grant date"))
            }
          }
        results.reduce((a, b) => a andThen b)
      }
    }
  }


  def optionGrantsVsGrantType(models: Seq[Model]): Seq[Validated[Model]] = {
    def gap(eDate: Date, gDate: Date) =
      Math.abs(new DateTime(eDate).getYear() - new DateTime(gDate).getYear())

	  models.map { m =>
	  	val maxTerm = (models.find(_.contains('grantTypes)).get) / 'grantTypes / 'stockOptions /% 'maxTerm
		m.validate('executives) {
		  val results : Seq[Validated[Model]] =
		  for {
			  execs <- m.applySeq(Path('executives, *))
			  grants = execs.applySeq(Path('optionGrants, *))
			  grant <- grants.zip(Stream.from(1))
		  }
		  yield
		  ((grant._1 / 'expireDate).rawValue[Date], (grant._1 / 'grantDate).rawValue[Date], maxTerm) match {
		    case (Some(eDate), Some(gDate), Some(term))
		    	if gap(eDate,gDate) != term.toInt =>
		    	  Doubtful(m,
		    	      warning("ExecDb - " + execMsg((m /#/ 'disclosureFiscalYear), execs.asModel)
		    	    		  + "grant " + grant._2,
		    	    		  "option grants should have gap between dates equal to max term on grant types"))
		    case _ => Valid(m)
		  }
		  results.reduce( (a, b) => a andThen b)
		}
	  }
  }

  def transitionPeriodValidation(models: Seq[Model]): Seq[Validated[Model]] = {

    def validateTransition(model: Model)(execs: (Element, Element)): Validated[Model] =
      ((execs._1/ 'functionalMatches /! 'primary),
       (execs._2 / 'functionalMatches /! 'primary),
       (execs._2 /! 'transitionPeriod)) match {
          case (Some(rol0), Some(rol1), Some(isTransition))
          if ((rol0 == rol1 && isTransition == "Yes") || (rol0 != rol1 && isTransition == "No")) =>
            Doubtful(model,
              s"Warning on ExecDb ${(model /#/ 'disclosureFiscalYear)} " +
                s"- ${(execs._2 /!/ 'firstName)} " +
                s"${(execs._2 /!/ 'lastName)} : Transition Period is wrong")
          case _ => Valid(model)
        }

    def validateExecutives(executives: (Model, Seq[(Element, Element)])): Validated[Model] =
      executives match {
        case (execDbModel, combinedExecs) =>
          combinedExecs.map(validateTransition(execDbModel))
            .foldLeft(Valid(execDbModel): Validated[Model])((acc, e) => acc andThen e)
      }

    def execId(exec: Element) = (exec /!/ 'firstName) + (exec /! 'lastName).get

    def combineSameExecutives(execs: (Model, Seq[Element]), nextExecs: (Model, Seq[Element])) =
      (execs, nextExecs) match {
        case ((_, execs0), (model, execs1)) =>
          (model,
            (execs0 ++ execs1)
            .filter { e => e.nonEmpty('firstName) && e.nonEmpty('lastName) }
            .groupBy(execId)
            .toSeq
            .flatMap {
              case (_, exec0 :: exec1 :: Nil) =>
                Some((exec0.asModel, exec1.asModel))
              case _ =>
                None
            })
      }

    val inputPartitioned: (Seq[Model], Seq[Model]) =
      models.partition(_.contains('executives))

    val executives: Seq[(Model, Seq[(Element)])] =
      inputPartitioned._1
        .sortBy(_ /#/ 'disclosureFiscalYear).reverse
        .map { execs =>
          (execs, execs.applySeq(Path('executives, *)))
        }

    if (executives.isEmpty)
      models.map(Valid(_))
    else {
      val firstPairsOfExecutives = combineSameExecutives(executives(0), executives(1))
      val secondPairOfExecutives = combineSameExecutives(executives(1), executives(2))

      inputPartitioned._2.map(Valid(_)) ++
        Seq(Valid(executives(0)._1),
          validateExecutives(firstPairsOfExecutives),
          validateExecutives(secondPairOfExecutives))
    }
  }

  def nextFiscalYearDataValidation(model: Model): Validated[Model] =
    reduceExecutiveValidations(Path('executives, *), model) {
      m => {
        val result : Seq[Validated[Model]] = Seq(
          m / 'cashCompensations /% 'baseSalary -> m / 'cashCompensations / 'nextFiscalYearData /% 'baseSalary,
          m / 'cashCompensations /% 'targetBonus -> m / 'cashCompensations / 'nextFiscalYearData /% 'targetBonus).map { it =>
            it match {
              case (Some(currentValue), Some(nextValue)) if currentValue <= nextValue => Valid(model)
              case (_, None) => Valid(model)
              case (None, _) => Valid(model)
              case _ =>
                Doubtful(model,
                  warning("ExecDb - " + execMsg((model /#/ 'disclosureFiscalYear), m.asModel),
                    "current base salary and target bonus are equal or greater that next fiscal year data"))
            }}
         result.reduce( (a, b) => a andThen b)
      }
    }

  def bodValidation(model: Model): Validated[Model] =
    reduceExecutiveValidations(Path('executives, *), model) {
      m =>
        {
          val primary = (m / 'functionalMatches /! 'primary)
          val bod = (m / 'functionalMatches /! 'bod)
          (for (primaryv <- primary; if (primaryv == "CEO (Chief Executive Officer)" && bod.isEmpty))
            yield Doubtful(model,
            warning("ExecDb - " + execMsg((model /#/ 'disclosureFiscalYear), m.asModel) + " - Bod: ",
            		"CEO with no BOD"))).getOrElse(Valid(model))
        }
    }

  def founderValidation(model: Model): Validated[Model] =
    reduceExecutiveValidations(Path('executives, *), model) {
      m =>
        {
          val baseSalary = (m / 'cashCompensations /% 'baseSalary)
          val founder = (m / 'founder).rawValue[Boolean]
          (for (salaryv <- baseSalary; if salaryv == 1 && founder.isEmpty)
            yield Doubtful(model,
            warning("ExecDb - " + execMsg((model /#/ 'disclosureFiscalYear), m.asModel) + " - Founder: ",
            		"base salary is 1 and executive is not a founder"))).getOrElse(Valid(model))
        }
    }

  def perfCashValidation(model: Model): Validated[Model] =
    reduceExecutiveValidations(Path('executives, *), model) {
      m =>
        {
          val results: Seq[Validated[Model]] =
            m.applySeq(Path('performanceCash, *))
              .map { perfCash =>
                ((perfCash/'targetValue).rawValue[BigDecimal],
                  (perfCash/'payout).rawValue[BigDecimal]) match {
                    case (Some(_), Some(_)) =>
                      Invalid(
                        err("ExecDb - " +execMsg((model /#/ 'disclosureFiscalYear), m.asModel),
                          ": in PerfCash Both target value and Payout can’t be filled for the same grant"))
                    case _ => Valid(model)
                  }
              }
          results.reduceValidations(m.asModel)
        }
    }

  def ownedSharesValidation(model: Model): Validated[Model] =
    reduceExecutiveValidations(Path('executives, *), model) {
      m =>
        {
          val ownedShares = (m / 'carriedInterest / 'ownedShares).asModel
          val beneficial = (ownedShares /% 'beneficialOwnership)
          val results: Seq[Validated[Model]] =
            Seq((ownedShares/% 'options),
              (ownedShares /% 'unvestedRestrictedStock),
              (ownedShares /% 'disclaimBeneficialOwnership),
              (ownedShares/% 'heldByTrust)).zip(Stream.continually(beneficial))
              .map {
                case (Some(value), Some(benef)) if value > benef =>
                  Doubtful(model,
                    warning("ExecDb + " + execMsg((model /#/ 'disclosureFiscalYear), m.asModel),
                    		"Some Owned Shares values cannot be greater than Beneficial Ownership"))
                case _ => Valid(model)
              }
          results.reduceValidations(m.asModel)
        }
    }

  def optionsExercisableValidation(model: Model): Validated[Model] =
    reduceExecutiveValidations(Path('executives, *), model) {
      m =>
        {
          (m / 'carriedInterest / 'ownedShares /% 'options,
           m / 'carriedInterest / 'outstandingEquityAwards /% 'vestedOptions,
           m / 'carriedInterest / 'outstandingEquityAwards /% 'unvestedOptions) match {
              case (Some(options), vested, unvested) if options == 0 && Seq(vested, unvested).flatten.sum > 0 =>
                Invalid(err("ExecDb - " 
                    + execMsg((model /#/ 'disclosureFiscalYear), m.asModel)
                    + Path('carriedInterest, 'ownedShares, 'options).titles.mkString(" - "),
                	" is 0, so vested options and unvested options should be 0 or empty"))
              case _ => Valid(model)
            }
        }
    }

  def timeVestRsValueValidation(model: Model): Validated[Model] =
    reduceExecutiveValidations(Path('executives, *), model) {
      m =>
        {
          val results: Seq[Validated[Model]] =
            m.applySeq(Path('timeVestRS, *))
              .map { timeVest =>
                (for {
                  n <- (timeVest /% 'number)
                  p <- (timeVest /% 'price)
                  v <- (timeVest /% 'value)
                  product = ((n * p) / 1000).roundUp(0)
                  if product != v.roundUp(0)
                } yield Invalid(
                  err("ExecDb - " + execMsg((model /#/ 'disclosureFiscalYear), m.asModel) + "TimeVestRs",
                    "Number multiplied by price should be equal to value"))).getOrElse(Valid(model))
              }
          results.reduceValidations(m.asModel)
        }
    }

  def timeVestRsGrantValidation(model: Model): Validated[Model] =
    reduceExecutiveValidations(Path('executives, *), model) {
      m =>
        {
          ((m / 'carriedInterest / 'outstandingEquityAwards / 'timeVestRS).rawValue[Date],
            m.applySeq(Path('timeVestRS, *, 'grantDate)).flatMap(_.rawValue[Date])) match {
              case (None, dates) if dates.nonEmpty => Doubtful(model, "asda")
              case _ => Valid(model)
            }
        }
    }
  
  //TODO: FIX ME! 
  def nonEmptyTransitionPeriods(model: Model) =
    reduceExecutiveValidations(Path('executives, *), model) { m =>
        model.validateWhen(m.isEmpty('transitionPeriod) && nonEmptyExecutive(m)) {
          Invalid(
            err("ExecDb - " + execMsg((model /#/ 'disclosureFiscalYear), m.asModel),
              "Transition Period should not be BLANK"))
        }
    }

  def salaryValidation(model: Model): Validated[Model] =
    threeDigitValidation(Path('executives, *), Seq(Path('cashCompensations, 'baseSalary)), model)

  def optionGrantsValidation(model: Model): Validated[Model] =
    reduceExecutiveValidations(Path('executives, *), model) {
      m =>
        {
          val results: Seq[Validated[Model]] =
            m.applySeq(Path('optionGrants, *))
              .map { grant =>
                val elements = (grant.asModel - 'perf).elements
                model.validateUnless(elements.forall(!_._2.asValue.isComplete) || elements.forall(_._2.asValue.isComplete)) {
                  Doubtful(model,
                    warning("ExecDb - " + execMsg((model /#/ 'disclosureFiscalYear), m.asModel),
                    		"Option grants must have all columns filled with data or all empty"))
                }
              }

          results.reduceValidations(m.asModel)
        }
    }

  def formulaValidation(model: Model): Validated[Model] = {
    val results: Seq[Validated[Model]] =
      model.applySeq(Path('executives, *)).map { m =>
        if (Seq((m / 'cashCompensations / 'maxBonus).asValue[BigDecimal],
                (m / 'cashCompensations / 'thresholdBonus).asValue[BigDecimal]).forall(_.isConsistent))
          Valid(model)
        else
          Invalid(
            err(execMsg(model /#/ 'disclosureFiscalYear, m.asModel),
              "max bonus and threshold bonus calculations are wrong"))
    }
    results.reduceValidations(model)
  }


  def executivesValidation(model: Model): Validated[Model] = {
    model.validate('executives) {
        formulaValidation(model) andThen
        founderValidation(model) andThen
        bodValidation(model) andThen
        nextFiscalYearDataValidation(model) andThen
        perfCashValidation(model) andThen
        ownedSharesValidation(model) andThen
        salaryValidation(model) andThen
        timeVestRsValueValidation(model) andThen
        optionsExercisableValidation(model) andThen
        nonEmptyTransitionPeriods(model)
    }
  }

  def grantTypeValidation(model: Model): Validated[Model] = {
    def validateGrantTypeUse: Validated[Model] = {
      val path = Path('grantTypes, 'stockOptions, 'use)
      val use = model(path).getRawValue[Boolean]
      model.validateUnless(!use || (model(path.init).asModel - path.last.routeValue).isComplete) {
        Invalid(err(path.titles.mkString(" - "), "Incomplete data"))
      }
    }

    def validateGrantTypeMinPayout = {
      val path = Path('grantTypes, 'performanceEquityVesting, 'minPayout)
      (for (minPayout <- model(path).rawValue[Number]; if minPayout != 0)
        yield Doubtful(model,
        warning(path.titles.mkString(" - "), "value is not 0%")))
        .getOrElse(Valid(model))
    }

    model.validate('grantTypes) {
      validateGrantTypeUse andThen validateGrantTypeMinPayout
    }
  }

}
