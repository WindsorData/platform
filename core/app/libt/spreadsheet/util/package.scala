package libt.spreadsheet

import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.util.CellUtil
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row

package object util {

  implicit def sheet2RichSheet(sheet: Sheet) = new {
    def cellAt(rowIndex: Int, columnIndex: Int): Cell =
      CellUtil.getCell(CellUtil.getRow(rowIndex, sheet), columnIndex)

    def rows(offset:Offset) : Seq[Row] = rows.drop(offset.rowIndex)

    def rows: Seq[Row] =
      (0 to sheet.getLastRowNum()).view.map(CellUtil.getRow(_, sheet))

    def defineLimits(offset: Offset, rowLimit: Int, columnLimit: Int) =
      for {
        cIndex <- 1 to columnLimit + offset.columnIndex
        rIndex <- 1 to rowLimit + offset.rowIndex
      } cellAt(rIndex, cIndex).setAsActiveCell()
  }

  implicit def row2RichRow(row: Row) = new {
    def cells: Seq[Cell] = for (cellIndex <- Stream.from(0))
      yield CellUtil.getCell(row, cellIndex)
    def cellAt(columnIndex: Int) =
      CellUtil.getCell(row, columnIndex)
  }

  def blankToNone[T](mapper: Cell => T)(cell: Cell) =
    if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK)
      None
    else
      Some(mapper(cell))
}