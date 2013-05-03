package libt.spreadsheet.writer
import util.IndexedTraversables._
import libt.Value
import java.util.Date
import org.apache.poi.ss.usermodel.Row
import libt.spreadsheet.util._
import org.apache.poi.ss.usermodel.Cell
import libt.spreadsheet.Offset
import libt.spreadsheet.generic.SkipeableLike

package object op {
  type WriteOp = (Cell) => Unit

  private def WriteOp[A](rawOp: (Cell, A) => Unit)(optionalValue: Option[A]): WriteOp =
    cell => optionalValue.foreach(rawOp(cell, _)) 
  
  val Skip : WriteOp = _ => ()
  
  val Numeric = WriteOp((cell, value : BigDecimal) => cell.setCellValue(value.toDouble)) _
  val Int = WriteOp((cell, value : Int) => cell.setCellValue(value.toInt)) _
  val String = WriteOp((cell, value : String ) => cell.setCellValue(value)) _ 
  val Boolean = WriteOp((cell, value : Boolean) => cell.setCellValue(value)) _
  val Date = WriteOp((cell, value : Date) => cell.setCellValue(value)) _
}


trait CellWriter extends SkipeableLike {
  def write(writeOps: Seq[op.WriteOp])
}

class ColumnOrientedWriter(columnOffset: Int, rows: Seq[Row]) extends CellWriter {
  private val cellIterator = rows.map(_.cells.iterator)
  skip(columnOffset)

  private def nextCells = cellIterator.map(_.next)
  override protected def skip1 = nextCells

  override def write(ops: Seq[op.WriteOp]) {
    for ((op, nextCell) <- ops.zip(nextCells))
      op(nextCell)
  }
}

class RowOrientedWriter(offset: Offset, rows: Seq[Row]) extends CellWriter {
  private val rowsIterator = rows.drop(offset.rowIndex).iterator

  override protected def skip1 = rowsIterator.next

  override def write(ops: Seq[op.WriteOp]) {
    val nextRow = rowsIterator.next
    for ((op, index) <- ops.zipWithIndex)
      op(nextRow.cellAt(index + offset.columnIndex))
  }

}