package model.mapping

import libt.Path
import model._
import model.ExecutivesTop5._
import model.mapping._
import model.validation._
import libt.spreadsheet.reader._
import libt.spreadsheet._
import libt.workflow._
import libt.util._
import libt._
import libt.error._
import java.math.MathContext
import org.joda._
import java.util.Date
import org.joda.time.Interval
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.Years
import play.Logger

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

  override def SheetValidation = model => grantTypeValidation(model) andThen executivesValidation(model)

  def gzip(sheets: Seq[Seq[Validated[Model]]]): Seq[Seq[Validated[Model]]] = sheets match {
        case m1 :: m2 :: Nil => m1.zip(m2).map { case (x, y) => Seq(x,y)}
        case head :: tail => head.zip(gzip(tail)).map { case (x, xs) => x +: xs }
      }
  
  override def WorkbookValidationPhase =
    (_, models) =>
    	  gzip(Seq(
    	      transitionPeriodValidation(models),
    	      optionGrantsVsGrantType(models),
            execDbTimeVestValidation(models)))
    	  .concatMap(_.reduce((a, b) => a andThen b))

  def execDbTimeVestValidation(models: Seq[Model]) = {
    val lastYear = models.maxBy(_(Path('disclosureFiscalYear)).getRawValue[Int])
    				.apply(Path('disclosureFiscalYear)).getRawValue[Int]
    models.map { model =>
      if (model.hasElement('executives) && model(Path('disclosureFiscalYear)).getRawValue[Int] == lastYear) {
        val results: Seq[Validated[Model]] =
          model.applySeq(Path('executives, *)).map { exec =>
            val hasSomeGrantDate = exec.applySeq(Path('timeVestRS, *, 'grantDate)).flatMap(_.rawValue[Date]).nonEmpty
            val hasNoTimeVestRs = exec(Path('carriedInterest, 'outstandingEquityAwards, 'timeVestRS)).rawValue[Int].isEmpty
            if ( hasSomeGrantDate && hasNoTimeVestRs )
              Doubtful(model,
                warning("ExecDb - " + execMsg(model(Path('disclosureFiscalYear)).getRawValue[Int], exec.asModel) +
                  "Carried Interest - Outstanding Equity Awards - Time Vest RS",
                  "should not be blank if there's some time vest rs - grant date"))
            else
              Valid(model)
          }
        results.reduce((a, b) => a andThen b)
      } else
        Valid(model)
    }
  }


  def optionGrantsVsGrantType(models: Seq[Model]): Seq[Validated[Model]] = {
    def gap(eDate: Date, gDate: Date) =
      Math.abs(new DateTime(eDate).getYear() - new DateTime(gDate).getYear())
        
	  models.map { m =>
	  	val maxTerm = models.find(_.hasElement('grantTypes))
	  				.get(Path('grantTypes, 'stockOptions, 'maxTerm)).rawValue[BigDecimal]
		if(m.hasElement('executives)) {
		  val results : Seq[Validated[Model]] = 
		  for {
			  execs <- m.applySeq(Path('executives, *))
			  grants = execs.applySeq(Path('optionGrants, *))
			  grant <- grants.zip(Stream.from(1))
		  }
		  yield
		  (grant._1(Path('expireDate)).rawValue[Date], grant._1(Path('grantDate)).rawValue[Date], maxTerm) match {
		    case (Some(eDate), Some(gDate), Some(term)) 
		    	if gap(eDate,gDate) != term.toInt => 
		    	  Doubtful(m,
		    	      warning("ExecDb - " + execMsg(m(Path('disclosureFiscalYear)).getRawValue[Int], execs.asModel)
		    	    		  + "grant " + grant._2,
		    	    		  "option grants should have gap between dates equal to max term on grant types"))
		    case _ => Valid(m)
		  }
		  results.reduce( (a, b) => a andThen b)
		}
		else
		  Valid(m)
	  }
  }
  
  def transitionPeriodValidation(models: Seq[Model]): Seq[Validated[Model]] = {

    def validateTransition(model: Model)(execs: (Element, Element)): Validated[Model] =
      (execs._1(Path('functionalMatches, 'primary)).rawValue[String],
        execs._2(Path('functionalMatches, 'primary)).rawValue[String],
        execs._2(Path('transitionPeriod)).rawValue[String]) match {
          case (Some(rol0), Some(rol1), Some(isTransition))
          if ((rol0 == rol1 && isTransition == "Yes") || (rol0 != rol1 && isTransition == "No")) =>
            Invalid(
              s"Error on ExecDb ${model(Path('disclosureFiscalYear)).getRawValue[Int]} " +
                s"- ${execs._2(Path('firstName)).getRawValue[String]} " +
                s"${execs._2(Path('lastName)).getRawValue[String]} : Transition Period is wrong")
          case _ => Valid(model)
        }

    def validateExecutives(executives: (Model, Seq[(Element, Element)])): Validated[Model] =
      executives match {
        case (execDbModel, combinedExecs) =>
          combinedExecs.map(validateTransition(execDbModel))
            .foldLeft(Valid(execDbModel): Validated[Model])((acc, e) => acc andThen e)
      }

    def execId(exec: Element) =
      exec('firstName).getRawValue[String] +
        exec('lastName).getRawValue[String]

    def combineSameExecutives(execs: (Model, Seq[Element]), nextExecs: (Model, Seq[Element])) =
      (execs, nextExecs) match {
        case ((_, execs0), (model, execs1)) =>
          (model,
            (execs0 ++ execs1)
            .filter { e =>
              e('firstName).rawValue[String].nonEmpty &&
                e('lastName).rawValue[String].nonEmpty
            }
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
      models.partition(_.hasElement('executives))

    val executives: Seq[(Model, Seq[(Element)])] =
      inputPartitioned._1
        .sortBy(_.apply('disclosureFiscalYear).getRawValue[Int]).reverse
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
      m =>
        (m(Path('cashCompensations, 'baseSalary)).rawValue[BigDecimal],
          m(Path('cashCompensations, 'nextFiscalYearData, 'baseSalary)).rawValue[BigDecimal],
          m(Path('cashCompensations, 'targetBonus)).rawValue[BigDecimal],
          m(Path('cashCompensations, 'nextFiscalYearData, 'targetBonus)).rawValue[BigDecimal]) match {
            case (Some(baseSalary), Some(nextBaseSalary), Some(targetBonus), Some(nextTargetBonus)) if (baseSalary <= nextBaseSalary && targetBonus <= nextTargetBonus) =>
              Valid(model)
            case (_, None, _, None) => Valid(model)
            case _ =>
              Doubtful(model,
                warning("ExecDb - " + execMsg(model(Path('disclosureFiscalYear)).getRawValue[Int], m.asModel),
                		"current base salary and target bonus are equal or greater that next fiscal year data"))
          }
    }

  def bodValidation(model: Model): Validated[Model] =
    reduceExecutiveValidations(Path('executives, *), model) {
      m =>
        {
          val primary = m(Path('functionalMatches, 'primary)).rawValue[String]
          val bod = m(Path('functionalMatches, 'bod)).rawValue[String]
          (for (primaryv <- primary; if (primaryv == "CEO (Chief Executive Officer)" && bod.isEmpty))
            yield Doubtful(model,
            warning("ExecDb - " + execMsg(model(Path('disclosureFiscalYear)).getRawValue[Int], m.asModel) + " - Bod: ",
            		"CEO with no BOD"))).getOrElse(Valid(model))
        }
    }

  def founderValidation(model: Model): Validated[Model] =
    reduceExecutiveValidations(Path('executives, *), model) {
      m =>
        {
          val baseSalary = m(Path('cashCompensations, 'baseSalary)).rawValue[BigDecimal]
          val founder = m(Path('founder)).rawValue[Boolean]
          (for (salaryv <- baseSalary; if salaryv == 1 && founder.isEmpty)
            yield Doubtful(model,
            warning("ExecDb - " + execMsg(model(Path('disclosureFiscalYear)).getRawValue[Int], m.asModel) + " - Founder: ",
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
                (perfCash('targetValue).rawValue[BigDecimal],
                  perfCash('payout).rawValue[BigDecimal]) match {
                    case (Some(_), Some(_)) =>
                      Invalid(
                        err("ExecDb - " +execMsg(model(Path('disclosureFiscalYear)).getRawValue[Int], m.asModel),
                          ": in PerfCash Both target value and Payout can’t be filled for the same grant"))
                    case _ => Valid(model)
                  }
              }
          results.reduce((a, b) => a andThen b)
        }
    }

  def ownedSharesValidation(model: Model): Validated[Model] =
    reduceExecutiveValidations(Path('executives, *), model) {
      m =>
        {
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
                    warning("ExecDb + " + execMsg(model(Path('disclosureFiscalYear)).getRawValue[Int], m.asModel),
                    		"Some Owned Shares values cannot be greater than Beneficial Ownership"))
                case _ => Valid(model)
              }
          results.reduce((a, b) => a andThen b)
        }
    }

  def optionsExercisableValidation(model: Model): Validated[Model] =
    reduceExecutiveValidations(Path('executives, *), model) {
      m =>
        {
          (m(Path('carriedInterest, 'ownedShares, 'options)).rawValue[BigDecimal],
            m(Path('carriedInterest, 'outstandingEquityAwards, 'vestedOptions)).rawValue[BigDecimal],
            m(Path('carriedInterest, 'outstandingEquityAwards, 'unvestedOptions)).rawValue[BigDecimal]) match {
              case (Some(options), vested, unvested) if options == 0 && Seq(vested, unvested).flatten.sum > 0 =>
                Invalid(err("ExecDb - " 
                    + execMsg(model(Path('disclosureFiscalYear)).getRawValue[Int], m.asModel)
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
                  n <- timeVest(Path('number)).rawValue[BigDecimal]
                  p <- timeVest(Path('price)).rawValue[BigDecimal]
                  v <- timeVest(Path('value)).rawValue[BigDecimal]
                  product = (n * p).setScale(0, BigDecimal.RoundingMode.HALF_UP) / 1000
                  if product != v
                } yield Invalid(
                  err("ExecDb - " + execMsg(model(Path('disclosureFiscalYear)).getRawValue[Int], m.asModel) + "TimeVestRs",
                    "Number multiplied by price should be equal to value"))).getOrElse(Valid(model))
              }
          results.reduce((a, b) => a andThen b)
        }
    }

  def timeVestRsGrantValidation(model: Model): Validated[Model] =
    reduceExecutiveValidations(Path('executives, *), model) {
      m =>
        {
          (m(Path('carriedInterest, 'outstandingEquityAwards, 'timeVestRS)).rawValue[Date],
            m.applySeq(Path('timeVestRS, *, 'grantDate)).flatMap(_.rawValue[Date])) match {
              case (None, dates) if dates.nonEmpty => Doubtful(model, "asda")
              case _ => Valid(model)
            }
        }
    }
  
  //TODO: FIX ME! 
  def nonEmptyTransitionPeriods(model: Model) =
    reduceExecutiveValidations(Path('executives, *), model) {
      m =>
        if (m(Path('transitionPeriod)).rawValue[String].isEmpty
            && nonEmptyExecutive(m))
          Invalid(
            err("ExecDb - " + execMsg(model(Path('disclosureFiscalYear)).getRawValue[Int], m.asModel),
              "Transition Period should not be BLANK"))
        else
          Valid(model)
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
                val elements = grant.asModel.without('perf).elements
                if (elements.forall(!_._2.asValue.isComplete)
                  || elements.forall(_._2.asValue.isComplete))
                  Valid(model)
                else
                  Doubtful(model,
                    warning("ExecDb - " + execMsg(model(Path('disclosureFiscalYear)).getRawValue[Int], m.asModel),
                    		"Option grants must have all columns filled with data or all empty"))
              }

          results.reduce((a, b) => a andThen b)
        }
    }

  def executivesValidation(model: Model): Validated[Model] = {
    if (model.hasElement('executives))
      founderValidation(model) andThen
        bodValidation(model) andThen
        nextFiscalYearDataValidation(model) andThen
        perfCashValidation(model) andThen
        ownedSharesValidation(model) andThen
        salaryValidation(model) andThen
        timeVestRsValueValidation(model) andThen
        optionsExercisableValidation(model) andThen
        nonEmptyTransitionPeriods(model)
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
        Invalid(err(path.titles.mkString(" - "), "Incomplete data"))
    }

    def validateGrantTypeMinPayout = {
      val path = Path('grantTypes, 'performanceEquityVesting, 'minPayout)
      (for (minPayout <- model(path).rawValue[Number]; if minPayout != 0)
        yield Doubtful(model,
        warning(path.titles.mkString(" - "), "value is not 0%")))
        .getOrElse(Valid(model))
    }

    if (model.hasElement('grantTypes)) {
      validateGrantTypeUse andThen validateGrantTypeMinPayout
    } else
      Valid(model)
  }
}
