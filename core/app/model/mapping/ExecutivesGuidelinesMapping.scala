package model.mapping

import libt.Path
import model._
import model.ExecutivesGuidelines._
import model.mapping._
import libt.spreadsheet.reader._
import libt.spreadsheet._
import libt._

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

  val GuidelineReader = new WorkbookReader(
    WorkbookMapping(
      Seq(Area(TCompanyFiscalYear, Offset(2, 2), None, RowOrientedLayout, Seq(Feature(Path('ticker)), Feature(Path('name)))),
        Area(TExecGuidelines, Offset(3, 1), Some(5), ColumnOrientedLayout, execGuidelinesMapping))),
   execGuidelinesCombiner)
}