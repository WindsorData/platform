package libt

import libt.error._

import org.apache.poi.ss.usermodel.{WorkbookFactory, Workbook}
import java.io.InputStream

/** *
  * Dead simple architecture for
  * pipelining workbook processing
  */
package object workflow {

  /**A processing unit for a workbook*/
  type Phase[I, O] = (Workbook, I) => Validated[O]

  def IdPhase[I]: Phase[I, I] = (_, input) => Valid(input)
  
  implicit def phase2RichPhase[I, O](self: Phase[I, O]) = new {
    /***Phases composition */
    def >>[O2](other: Phase[O, O2]): Phase[I, O2] =
      (wb, input) => self(wb, input) flatMap (other(wb, _))
  }

  /***Worksheet processing pipeline frontend*/
  implicit class FrontPhase[Output](val phase: Phase[Workbook, Output]) extends AnyVal {
    def apply(wb: Workbook) : Validated[Output] = phase(wb, wb)
    def apply(in: InputStream) : Validated[Output] =  Validated(WorkbookFactory.create(in)) flatMap apply
  }
}



