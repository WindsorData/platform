package input

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import util.poi.Cells._
import libt.Value
/**
 * Trait for reading cells, that allows to parse cell groups as Inputs
 * The exact orientation of cells groups - columns or rows - depends on implementors
 * @author flbulgarelli
 */
trait CellReader {
  
  def string = createValue(blankToNone(_.getStringCellValue))
  def stringWithDefault(s: String = "BLANK") = createValue(blankToSome(_.getStringCellValue, s))
    
  def numeric = createValue(blankToNone(_.getNumericCellValue : BigDecimal))
  def numericWithDefault(n: BigDecimal = 0.0) = createValue(blankToSome(_.getNumericCellValue : BigDecimal, n))
  
  def boolean = createValue(blankToNone(_.getBooleanCellValue))
  def booleanWithDefault(b: Boolean = false) = createValue(blankToSome(_.getBooleanCellValue, b))
  
  def date = createValue(blankToNone(_.getDateCellValue))
  
  def skip(offset: Int) = for (_ <- 1 to offset) skip1
  def skip1: Unit
  def next: Seq[Cell]

  def createValue[T](valueMapper: Cell => Option[T]): Value[T] = {
    val nextCells = next
    def nextStringValue(index: Int) = blankToNone(_.getStringCellValue)(nextCells(index))
    newValue(valueMapper(nextCells(0)), nextStringValue)
  }

  def newValue[T](value: Option[T], nextStringValue: Int => Option[String]) =
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

  override def newValue[T](value: Option[T], nextStringValue: Int => Option[String]) =
    Value(value,
      None,
      None,
      nextStringValue(1),
      nextStringValue(2))
}