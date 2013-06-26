package model.mapping

import libt.Path
import model.ExecutivesSTBonusPlan._
import model.ExecutivesGuidelines._
import model._
import model.mapping._
import model.validation._
import libt.spreadsheet.reader._
import libt.spreadsheet._
import libt.util._
import libt._
import libt.error._

package object guidelines extends WorkflowFactory {

  val GuidelinesSheetMapping =
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

  val STBonusPlanSheetMapping =
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

  def Mapping = WorkbookMapping(
    Seq(
      Area(TCompanyFiscalYear,
        Offset(2, 2),
        None,
        RowOrientedLayout,
        Seq(Feature(Path('ticker)), Feature(Path('name)))),
      Area(TExecGuidelines,
        Offset(3, 1),
        Some(5),
        ColumnOrientedLayout,
        GuidelinesSheetMapping),
      Area(TExecSTBonusPlan,
        Offset(5, 1),
        Some(5),
        ColumnOrientedLayout,
        STBonusPlanSheetMapping)))

  def CombinerPhase =
    DocSrcCombiner(
      (10, 'guidelines, colWrapping),
      (25, 'stBonusPlan, colWrapping))

  def guidelinesDigitValidation(model: Model) =
    digitValidation(Path('guidelines, *), Seq(Path('numberOfShares)),model)(_ >= 10) andThen
    digitValidation(Path('guidelines, *), Seq(Path('multipleOfSalary)),model)(_ < 10)
  
  def guidelinesValidations(model: Model): Validated[Model] = {
    if(model.hasElement('guidelines)) {
      guidelinesDigitValidation(model)
    }
    else
      Valid(model)
  } 

  override def ValidationPhase =
    (_, models) => {
      if (!models.concat.isInvalid) {
        models.map { model =>
          umatch(model) {
            case validModel @ Valid(m) => {
              guidelinesValidations(m)
            }
          }
        }

      } else
        models
    }
}