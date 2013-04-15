package libt.spreadsheet.reader

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import libt.Value
import libt.spreadsheet.util._
import scala.math.BigDecimal.double2bigDecimal
/**
 * Trait for reading cells, that allows to parse cell groups as Inputs
 * The exact orientation of cells groups - columns or rows - depends on implementors
 * @author flbulgarelli
 */
trait CellReader {

  def string = createValue(blankToNone(_.getStringCellValue))
  def numeric = createValue(blankToNone(_.getNumericCellValue: BigDecimal))
  def boolean = createValue(blankToNone(_.getBooleanCellValue))
  def xBoolean = string.orDefault("").map(_=="X")
  def date = createValue(blankToNone(_.getDateCellValue))
  

  def skip(offset: Int) = for (_ <- 1 to offset) skip1
  protected def skip1: Unit
  protected def next: Seq[Cell]

  private def createValue[T](valueMapper: Cell => Option[T]): Value[T] = {
    val nextCells = next
    def nextStringValue(index: Int) = blankToNone(_.getStringCellValue)(nextCells(index))
    newValue(valueMapper(nextCells(0)), nextStringValue)
  }

  protected def newValue[T](value: Option[T], nextStringValue: Int => Option[String]) =
    Value(value,
      nextStringValue(1),
      nextStringValue(2),
      nextStringValue(3),
      nextStringValue(4))
}

/**
 * {{CellReader}} that expects vertical cell groups, that is, data items are found in columns
 * @author flbulgarelli
 */
class ColumnOrientedReader(rows: Seq[Row]) extends CellReader {
  private val cellIterators = rows.map(_.cells).map(_.iterator)

  override protected def skip1 = cellIterators.foreach(_.next)
  override protected def next = cellIterators.map(_.next)
}

/**
 * {{CellReader}} that expects horizontal cell groups, that is, different
 * data items are found in rows
 * @author flbulgarelli
 */
class RowOrientedReader(rows: Seq[Row]) extends CellReader {
  private val rowIterator = rows.iterator

  override protected def skip1 = rowIterator.next
  override protected def next = rowIterator.next.cells.drop(2) //harcoded offset

  override protected def newValue[T](value: Option[T], nextStringValue: Int => Option[String]) =
    Value(value,
      None,
      None,
      nextStringValue(1),
      nextStringValue(2))
}