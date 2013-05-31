package model.mapping

import libt.Path
import model.ExecutivesSTBonusPlan._
import model.ExecutivesGuidelines._
import model._
import model.mapping._
import libt.spreadsheet.reader._
import libt.spreadsheet._
import libt._
import libt.spreadsheet.Offset

object ExecutivesGuidelinesMapping {

  val execGuidelinesMapping =
    Seq[Strip](
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

  val execSTBonusPlanMapping =
    Seq[Strip](
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
      colOfModelsPath(Path('metrics, 'select), 5, 'use, 'weight) ++
      colOfModelsPath(Path('metrics, 'typeIn), 5, 'type, 'weight)

  val GuidelineReader = new WorkbookReader(
    WorkbookMapping(
      Seq(Area(TCompanyFiscalYear, Offset(2, 2), None, RowOrientedLayout, Seq(Feature(Path('ticker)), Feature(Path('name)))),
        Area(TExecGuidelines, Offset(3, 1), Some(5), ColumnOrientedLayout, execGuidelinesMapping),
        Area(TExecSTBonusPlan, Offset(5, 1), Some(5), ColumnOrientedLayout, execSTBonusPlanMapping))),
    execGuidelinesCombiner)

  def execGuidelinesCombiner =
    DocSrcCombiner(
      (10, 'guidelines, colWrapping),
      (25, 'stBonusPlan, colWrapping))
}