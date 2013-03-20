package input

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import model.Input
import util.poi.Cells._
import model.SimpleInput
/**
 * Trait for reading cells, that allows to parse cell groups as Inputs
 * The exact orientation of cells groups - columns or rows - depends on implementors
 * @author flbulgarelli
 */

trait CellReader {
  def string = createInput(_.getStringCellValue)
  def boolean = createInput(_.getBooleanCellValue)
  def numeric = createInput(_.getNumericCellValue: BigDecimal)
  def date = createInput(_.getDateCellValue)
  def skip(offset: Int) = for (_ <- 1 to offset) skip1
  def skip1: Unit
  def next: Seq[Cell]

  def createInput[T](valueMapper: Cell => T): Input[T] = {
    val nextCells = next.map(blankToNone)
    def nextStringValue(index: Int) = nextCells(index).map(_.getStringCellValue)
    newInput(nextCells(0).map(validValueMapper(valueMapper)_), nextStringValue);
  }

  def newInput[T](value: Option[T], nextStringValue: Int => Option[String]) =
    Input(value,
      nextStringValue(1),
      nextStringValue(2),
      nextStringValue(3),
      nextStringValue(4))
}

/**
 * {{CellReader}} that expects vertical cell groups, that is, data iems are found in columns
 * @author flbulgarelli
 */
class ColumnOrientedReader(rows: Seq[Row]) extends CellReader {
  private val cellIterators = rows.map(cells).map(_.iterator)

  override def skip1 = cellIterators.foreach(_.next)
  override def next = cellIterators.map(_.next)
}

/**
 * {{CellReader}} that expects horizontal cell groups, that is, different
 * data items are found in rows
 * @author flbulgarelli
 */
class RowOrientedReader(rows: Seq[Row]) extends CellReader {
  private val rowIterator = rows.iterator

  override def skip1 = rowIterator.next
  override def next = cells(rowIterator.next).drop(2) //harcoded offset

  override def newInput[T](value: Option[T], nextStringValue: Int => Option[String]) =
    Input(value,
      None,
      None,
      nextStringValue(1),
      nextStringValue(2))

}