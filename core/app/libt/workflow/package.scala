package libt

import libt.spreadsheet.reader._
import libt.error._
import libt._

import org.apache.poi.ss.usermodel.Workbook

package object workflow {
  type Phase[I, O] = (Workbook, I) => O
  implicit def phaseCombiner[I, O](self: Phase[I, O]) = new {
    def >>[O2](other: Phase[O, O2]): Phase[I, O2] =
      (wb, input) => other(wb, self(wb, input))
  }

  def MappingPhase(mapping: WorkbookMapping): Phase[Workbook, Seq[Seq[Validated[Model]]]] =
    (wb, _) => mapping
    	.read(wb)
    	.filter(
    	    !_.isEmpty)
}



