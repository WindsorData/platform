package libt.spreadsheet.writer
import util.IndexedTraversables._
import libt.Value
import java.util.Date
import org.apache.poi.ss.usermodel.Row
import libt.spreadsheet.util._
import org.apache.poi.ss.usermodel.Cell
import libt.spreadsheet.reader.Offset

trait CellWriter {

  def numeric(value: Value[BigDecimal]) = writeNext(value.map(_.toDouble))(_.setCellValue(_))
  def string(value: Value[String]) = writeNext(value)(_.setCellValue(_))
  def xBoolean(value: Value[Boolean]) = writeNext(value)(_.setCellValue(_))
  def date(value: Value[Date]) = writeNext(value)(_.setCellValue(_))

  def skip(offset: Int) = for (_ <- 1 to offset) skip1

  protected def writeNext[A](value: Value[A])(writeFunction: (Cell, A) => Unit)

  protected def skip1: Unit
}

class ColumnOrientedValueWriter(offset: Int, row: Row) extends CellWriter {
  private val cellIterator = row.cells.iterator
  skip(offset)
  
  override protected def skip1 = cellIterator.next
  override protected def writeNext[A](value: Value[A])(writeFunction: (Cell, A) => Unit) {
    val nextCell = cellIterator.next
    value.value.foreach(writeFunction(nextCell, _))
  }
}

//currently only supports metadata
class RowOrientedMetadataWriter(offset: Offset, rows: Seq[Row]) extends CellWriter {
  private val rowsIterator = rows.drop(offset.rowIndex).iterator

  override protected def skip1 = rowsIterator.next

  override protected def writeNext[A](value: Value[A])(writeFunction: (Cell, A) => Unit) {
    val nextRow = rowsIterator.next
    for ((Some(metadata), index) <- value.metadataSeq.zipWithIndex) {
      nextRow
      .cellAt(index + offset.columnIndex)
      .setCellValue(metadata)
    }
  }
  
}