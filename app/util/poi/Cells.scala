package util.poi

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.util.CellUtil
import org.apache.poi.ss.usermodel.Row

object Cells {

  def blankToNone[T](mapper: Cell => T)(cell: Cell) =
    if (cell.getCellType() == Cell.CELL_TYPE_BLANK)
      None
    else
      Some(mapper(cell))

  def blankToSome[T](mapper: Cell => T, defaultValue: T)(cell: Cell) =
    Some(blankToNone(mapper)(cell).getOrElse(defaultValue))

  def rows(sheet: Sheet) =
    for (rowIndex <- 0 to sheet.getLastRowNum())
      yield CellUtil.getRow(rowIndex, sheet)

  def cells(row: Row) =
    for (cellIndex <- Stream.from(0))
      yield CellUtil.getCell(row, cellIndex)

}