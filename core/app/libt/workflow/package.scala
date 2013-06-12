package libt

import libt.error._

import org.apache.poi.ss.usermodel.Workbook

package object workflow {
  
  type Phase[I, O] = (Workbook, I) => O
  
  implicit def phase2RichPhase[I, O](self: Phase[I, O]) = new {
    /***Phases composition */
    def >>[O2](other: Phase[O, O2]): Phase[I, O2] =
      (wb, input) => other(wb, self(wb, input))
  }
}



