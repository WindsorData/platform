package libt.export.spreadsheet

import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.util.CellUtil
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row

package object util {

  implicit def sheet2RichSheet(sheet: Sheet) = new {
    def cellAt(rowIndex: Int, columnIndex: Int) =
      CellUtil.getCell(CellUtil.getRow(rowIndex, sheet), columnIndex)

    def rows : Seq[Row] = for (rowIndex <- 0 to sheet.getLastRowNum())
      yield CellUtil.getRow(rowIndex, sheet)
  }

  implicit def row2RichRow(row: Row) = new {
    def cells : Seq[Cell] = for (cellIndex <- Stream.from(0))
      yield CellUtil.getCell(row, cellIndex)
  }

  def blankToNone[T](mapper: Cell => T)(cell: Cell) =
    if (cell.getCellType() == Cell.CELL_TYPE_BLANK)
      None
    else
      Some(mapper(cell))

  def blankToSome[T](mapper: Cell => T, defaultValue: T)(cell: Cell) =
    Some(blankToNone(mapper)(cell).getOrElse(defaultValue))

}