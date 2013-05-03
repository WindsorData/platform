package libt.spreadsheet.writer
import util.IndexedTraversables._
import libt.Value
import java.util.Date
import org.apache.poi.ss.usermodel.Row
import libt.spreadsheet.util._
import org.apache.poi.ss.usermodel.Cell
import libt.spreadsheet.reader.Offset

package object op {
  type WriteOp = (Cell) => Unit

  private def WriteOp[A](rawOp: (Cell, A) => Unit)(optionalValue: Option[A]): WriteOp =
    cell => optionalValue.foreach(rawOp(cell, _)) 
  
  val Skip : WriteOp = _ => ()
  
  val Numeric = WriteOp((cell, value : BigDecimal) => cell.setCellValue(value.toDouble)) _
  val String = WriteOp((cell, value : String ) => cell.setCellValue(value)) _ 
  val Boolean = WriteOp((cell, value : Boolean) => cell.setCellValue(value)) _
  val Date = WriteOp((cell, value : Date) => cell.setCellValue(value)) _
}


trait CellWriter {
  def write(writeOps: Seq[op.WriteOp])

  def skip(offset: Int) = for (_ <- 1 to offset) skip1

  protected def skip1: Unit
}

class ColumnOrientedWriter(offset: Int, rows: Seq[Row]) extends CellWriter {
  private val cellIterator = rows.map(_.cells.iterator)
  skip(offset)

  private def nextCells = cellIterator.map(_.next)
  override protected def skip1 = nextCells

  override def write(ops: Seq[op.WriteOp]) {
    for ((op, nextCell) <- ops.zip(nextCells))
      op(nextCell)
  }
}

//currently only supports metadata
class RowOrientedWriter(offset: Offset, rows: Seq[Row]) extends CellWriter {
  private val rowsIterator = rows.drop(offset.rowIndex).iterator

  override protected def skip1 = rowsIterator.next

  override def write(ops: Seq[op.WriteOp]) {
    val nextRow = rowsIterator.next
    for ((op, index) <- ops.zipWithIndex)
      op(nextRow.cellAt(index + offset.columnIndex))
  }

}