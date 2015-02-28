package model.mapping

import model.CompanyIndex._
import libt.spreadsheet.Strip
import libt.Path
import libt.spreadsheet.reader.WorkbookMapping
import libt.spreadsheet.reader.workflow._
import libt.Model
import libt.error.generic.Validated
import libt.spreadsheet.reader.Area
import libt.spreadsheet.Offset
import libt.spreadsheet.reader.ColumnOrientedLayout
import libt.spreadsheet.reader.RawValueReader

package object companyIndex {

  val indexMapping = Seq[Strip](
    Path('ticker),
    Path('name))

  def Workflow: FrontPhase[Seq[Model]] =
    MappingPhase(Mapping) >> CombinerPhase

  def Mapping = WorkbookMapping(
      Seq(Area(TCompanyIndex, Offset(2, 0), None, ColumnOrientedLayout(RawValueReader), indexMapping)))

  def CombinerPhase: Phase[Seq[Seq[Model]], Seq[Model]] =
    (_, xs) => Validated(xs.head)

}
