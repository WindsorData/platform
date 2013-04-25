package libt.spreadsheet.writer

import libt.Value
import java.util.Date
import org.apache.poi.ss.usermodel.Row
import libt.spreadsheet.util._
import org.apache.poi.ss.usermodel.Cell

trait CellWriter {

  def numeric(value: Value[BigDecimal]) = writeNext(value.map(_.toDouble))(_.setCellValue(_))
  def string(value: Value[String]) = writeNext(value)(_.setCellValue(_))
  def xBoolean(value: Value[Boolean]) = writeNext(value)(_.setCellValue(_))
  def date(value: Value[Date]) = writeNext(value)(_.setCellValue(_))
  
  def skip(offset : Int) = for (_ <- 1 to offset) skip1
  
  protected def writeNext[A](value:Value[A])(writeFunction : (Cell,  A) => Unit)
  
  protected def skip1: Unit
}

class ColumnOrientedWriter(row: Row) extends CellWriter {
  private val cellIterator = row.cells.iterator

  override protected def skip1 = cellIterator.next
  override def writeNext[A](value: Value[A])(writeFunction: (Cell, A) => Unit) {
    val nextCell = cellIterator.next
    value.value.foreach(writeFunction(nextCell, _)) 
  }

}