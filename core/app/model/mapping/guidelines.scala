package model.mapping

import _root_.mapping.GuidelinesMappingComponent
import model.mapping.generic._
import model.ExecutivesSTBonusPlan._
import model.ExecutivesGuidelines._
import model._
import model.validation._

import libt.spreadsheet.reader._
import libt.spreadsheet._
import libt._
import libt.error._

trait FullOutputGuidelinesMappingComponent extends GuidelinesMappingComponent {

  val guidelinesMapping =
    Seq[Strip](
      Path('firstName),
      Path('lastName),
      Path('title),
      Path('functionalMatches, 'primary),
      Path('use),
      Path('yearsToAchieve),
      Path('numberOfShares),
      Path('multipleOfSalary),
      Path('retention, 'ratio),
      Path('retention, 'period))

  val stBonusPlanMapping =
    Seq[Strip](
      Path('firstName),
      Path('lastName),
      Path('title),
      Path('functionalMatches, 'primary),
      Path('useCash),
      Path('useShares),
      Path('bonusType),
      Path('thresholdTarget),
      Path('maxTarget),
      Path('perfPeriod),
      Path('payoutFrecuency),
      Path('scope, 'corporate, 'use),
      Path('scope, 'corporate, 'weight),
      Path('scope, 'busUnit, 'use),
      Path('scope, 'busUnit, 'weight),
      Path('scope, 'individual, 'use),
      Path('scope, 'individual, 'weight)) ++
      Multi(Path('metrics, 'select), 5,
        Path('use),
        Path('weight)) ++
      Multi(Path('metrics, 'typeIn), 5,
        Path('type),
        Path('weight))
}


trait FullInputGuidelinesMappingComponent extends GuidelinesMappingComponent {

  val guidelinesMapping =
    Seq[Strip](
      Path('firstName),
      Path('lastName),
      Path('title),
      Path('functionalMatches, 'primary),
      Path('functionalMatches, 'secondary),
      Path('functionalMatches, 'level),
      Path('functionalMatches, 'scope),
      Path('functionalMatches, 'bod),
      Path('use),
      Path('yearsToAchieve),
      Path('retention, 'ratio),
      Path('retention, 'period),
      Path('numberOfShares),
      Path('multipleOfSalary))

  val stBonusPlanMapping =
    Seq[Strip](
      Path('firstName),
      Path('lastName),
      Path('title),
      Path('functionalMatches, 'primary),
      Path('functionalMatches, 'secondary),
      Path('functionalMatches, 'level),
      Path('functionalMatches, 'scope),
      Path('functionalMatches, 'bod),
      Path('useCash),
      Path('useShares),
      Path('bonusType),
      Path('thresholdTarget),
      Path('maxTarget),
      Path('perfPeriod),
      Path('payoutFrecuency),
      Path('scope, 'corporate, 'use),
      Path('scope, 'corporate, 'weight),
      Path('scope, 'busUnit, 'use),
      Path('scope, 'busUnit, 'weight),
      Path('scope, 'individual, 'use),
      Path('scope, 'individual, 'weight)) ++
      Multi(Path('metrics, 'select), 5,
        Path('use),
        Path('weight)) ++
      Multi(Path('metrics, 'typeIn), 5,
        Path('type),
        Path('weight))

  def guidelinesDigitValidation(model: Model) =
    digitValidation(Path('guidelines, *), Seq(Path('numberOfShares)),model)(_ >= 10) andThen
      digitValidation(Path('guidelines, *), Seq(Path('multipleOfSalary)),model)(_ < 10)

  def guidelinesValidations(model: Model): Validated[Model] = {
    model.validate('guidelines) {
      guidelinesDigitValidation(model)
    }
  }

  //TODO: FIX ME!
  def scopeValidation(model: Model) =
    reduceExecutiveValidations(Path('stBonusPlan, *), model) {
      m =>
        (for {
          corporate <- m(Path('scope, 'corporate, 'use)).rawValue[Boolean]
          if corporate || !nonEmptyExecutive(m)
        } yield Valid(model))
          .getOrElse(
          Doubtful(model,
            warning(
              execMsg((model /#/ 'disclosureFiscalYear), m.asModel)
                + Path('scope, 'corporate, 'use).titles.mkString(" - "),
              "Almost always the scope would include Corporate")))
    }

  def metricsValidation(model: Model) =
    reduceExecutiveValidations(Path('stBonusPlan, *), model) {
      m =>
      {
        val results = m.applySeq(Path('metrics, 'select, *)) ++ m.applySeq(Path('metrics, 'typeIn, *))
        if (results.isEmpty)
          Doubtful(model, "The bonus plan should have at least one matrix")
        else
          Valid(model)
      }
    }

  def stBonusValidations(model: Model): Validated[Model] = {
    model.validate('stBonusPlan) {
      scopeValidation(model) andThen
        metricsValidation(model)
    }
  }

}


package object guidelines extends StandardWorkflowFactory with FullInputGuidelinesMappingComponent {

  def Mapping = WorkbookMapping(
    Seq(
      Area(TCompanyFiscalYear,
        Offset(1, 2),
        None,
        DocSrcLayout,
        DocSrcMapping),
      Area(TExecGuidelines,
        Offset(3, 1),
        Some(5),
        DataLayout,
        guidelinesMapping),
      Area(TExecSTBonusPlan,
        Offset(5, 1),
        Some(5),
        DataLayout,
        stBonusPlanMapping)))

  def CombinerPhase =
    StandardDocSrcCombiner(
      (10, 'guidelines, colWrapping),
      (25, 'stBonusPlan, colWrapping))

  override def SheetValidation = model => guidelinesValidations(model) andThen stBonusValidations(model)
}