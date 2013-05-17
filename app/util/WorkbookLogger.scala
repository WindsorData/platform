package util

import play.Logger
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.util.CellReference

object WorkbookLogger {

  trait Loggeable {
    def generateErrorMessage(description: String): String
    def where(cell: Cell) =
      if (cell != null) {
        " on Sheet " + cell.getSheet().getSheetName +
        " -> Column: " + CellReference.convertNumToColString(cell.getColumnIndex()) +
        ", Row: " + { cell.getRowIndex + 1 }
      }
  }

  case class ReaderError(baseMessage: String) extends Loggeable {

    def generateErrorMessage(description: String) =
      "PARSING INPUT" + " - " + description

    def description(cell: Cell, specificMessage: String = "") =
      generateErrorMessage(baseMessage + specificMessage + where(cell))

    def noFiscalYearProvidedAt(cell: Cell) = description(cell, "No Fiscal Year provided ")
  }

  case class WriterError(baseMessage: String) extends Loggeable {
    def generateErrorMessage(description: String) = ???
  }

  object ReaderError {
    def apply(): ReaderError = ReaderError("")
  }

  def log(cause: String) = {
    val completeMsg = "ERROR " + cause
    Logger.error(completeMsg);
    completeMsg
  }
}
