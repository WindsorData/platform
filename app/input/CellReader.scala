package input

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import model.Input
import util.poi.Cells._
import model.Input
/**
 * Trait for reading cells, that allows to parse cell groups as Inputs
 * The exact orientation of cells groups - columns or rows - depends on implementors
 * @author flbulgarelli
 */

trait CellReader {
  
  def string = createInput(blankToNone(_.getStringCellValue))
  def stringWithDefault(s: String = "BLANK") = createInput(blankToSome(_.getStringCellValue, s))
    
  def numeric = createInput(blankToNone(_.getNumericCellValue : BigDecimal))
  def numericWithDefault(n: BigDecimal = 0.0) = createInput(blankToSome(_.getNumericCellValue : BigDecimal, n))
  
  def boolean = createInput(blankToNone(_.getBooleanCellValue))
  def booleanWithDefault(b: Boolean = false) = createInput(blankToSome(_.getBooleanCellValue, b))
  
  def date = createInput(blankToNone(_.getDateCellValue))
  
  def skip(offset: Int) = for (_ <- 1 to offset) skip1
  def skip1: Unit
  def next: Seq[Cell]

  def createInput[T](valueMapper: Cell => Option[T]): Input[T] = {
    val nextCells = next
    def nextStringValue(index: Int) = blankToNone(_.getStringCellValue)(nextCells(index))
    newInput(valueMapper(nextCells(0)), nextStringValue)
  }

  def newInput[T](value: Option[T], nextStringValue: Int => Option[String]) =
    Input(value,
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