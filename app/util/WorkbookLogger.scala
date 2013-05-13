package util

import play.Logger
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet

object WorkbookLogger {

  trait Loggeable {
    def generateErrorMessage(description: String): String
    def where(cell: Cell) = " on Sheet " + cell.getSheet().getSheetName +
      " -> Column: " + { cell.getColumnIndex + 1 } +
      ", Row: " + { cell.getRowIndex + 1 }
  }

  case class ReaderError(baseMessage: String) extends Loggeable {

    def generateErrorMessage(description: String) =
      "PARSING INPUT" + " - " + description

    def description(cell: Cell, specificMessage: String = "") =
      generateErrorMessage(baseMessage + specificMessage + where(cell))

    def noFiscalYearProvidedAt(cell: Cell) = description(cell, "No Fiscal Year provided ")
    def invalidCellTypeAt(cell: Cell) = description(cell)
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

  def logAndThrowException(msg: String) =
    throw new RuntimeException(log(msg))
}
