package libt.spreadsheet.writer
import libt.util.Lists._
import libt.Value
import java.util.Date
import org.apache.poi.ss.usermodel.Row
import libt.spreadsheet.util._
import org.apache.poi.ss.usermodel.Cell
import libt.spreadsheet.Offset
import libt.spreadsheet.generic.SkipeableLike
import libt.spreadsheet.generic.RowOrientedLike
import libt.spreadsheet.generic.ColumnOrientedLike

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
  val XBoolean = WriteOp((cell, value: Boolean) => if(value) cell.setCellValue("X")) _
}


trait CellWriter extends SkipeableLike {
  def write(writeOps: Seq[op.WriteOp])
}

class ColumnOrientedWriter(
    override val columnOffset: Int, 
    override val rows: Seq[Row]) extends CellWriter with ColumnOrientedLike {
  private def nextCells = cellIterators.map(_.next)
  override def write(ops: Seq[op.WriteOp]) {
    for ((op, nextCell) <- ops.zip(nextCells))
      op(nextCell)
  }
}

class RowOrientedWriter(
    override val offset: Offset, 
    override val rows: TraversableOnce[Row]) extends CellWriter with RowOrientedLike {
  
  override def write(ops: Seq[op.WriteOp]) {
    val nextRow = rowIterator.next
    for ((op, index) <- ops.zipWithIndex)
      op(nextRow.cellAt(index + offset.columnIndex))
  }

}