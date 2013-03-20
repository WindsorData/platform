package util.poi

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.util.CellUtil
import org.apache.poi.ss.usermodel.Row

object Cells {

  def blankToNone(cell: Cell) =
    if (cell.getCellType() == Cell.CELL_TYPE_BLANK)
      None
    else
      Some(cell)

  def validValueMapper[T](valueMapper: Cell => T)(cell: Cell): T = {
    try {
      valueMapper(cell)
    } catch {
      case e: RuntimeException =>
        throw new IllegalStateException(invalidCellTypeErrorMessage(e.getMessage(), cell))
    }
  }

  def invalidCellTypeErrorMessage(baseMessage: String, cell: Cell) =
    baseMessage +
      " on Sheet: " + cell.getSheet().getSheetName() +
      " -> Column: " + { cell.getColumnIndex() + 1 } +
      ", Row: " + { cell.getRowIndex() + 1 }

  def noFiscalYearErrorMessage(cell: Cell) =
    "No Fiscal Year provided at Sheet " +
      cell.getSheet().getSheetName() +
      " Column: " + cell.getColumnIndex() +
      " Row: " + cell.getRowIndex()

  def rows(sheet: Sheet) =
    for (rowIndex <- 0 to sheet.getLastRowNum())
      yield CellUtil.getRow(rowIndex, sheet)

  def cells(row: Row) =
    for (cellIndex <- Stream.from(0))
      yield CellUtil.getCell(row, cellIndex)

}