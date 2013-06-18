package libt.workflow

import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory

import java.io.InputStream

case class InputWorkflow[Output](phase: Phase[Workbook, Output]) {
  def apply(wb: Workbook) : Output = phase(wb, wb)
  def apply(in: InputStream) : Output = this(WorkbookFactory.create(in))
}