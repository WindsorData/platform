package libt.spreadsheet.reader

import scala.math.BigDecimal.double2bigDecimal
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import util.WorkbookLogger._
import libt.spreadsheet.util._
import libt.spreadsheet.generic._
import libt.spreadsheet._
import libt._

/**
 * Trait for reading cells, that allows to parse cell groups as Inputs
 * The exact orientation of cells groups - columns or rows - depends on implementors.
 * 
 * CellReaders are aimed to read single data items
 * 
 * @author flbulgarelli
 */
trait CellReader extends SkipeableLike {

  def string = createValue(blankToNone(_.getStringCellValue))
  def int = createValue(blankToNone(_.getNumericCellValue.toInt))
  def numeric = createValue(blankToNone(_.getNumericCellValue: BigDecimal))
  def boolean = createValue(blankToNone(_.getBooleanCellValue))
  def xBoolean = string.orDefault("").map(_=="X")
  def date = createValue(blankToNone(_.getDateCellValue))
  def any: Value[String] = createValue(blankToNone(readAnyValue))
  
  def readAnyValue(cell: Cell) = 
    cell.getCellType() match {
    	case Cell.CELL_TYPE_BOOLEAN => cell.getBooleanCellValue().toString
    	case Cell.CELL_TYPE_NUMERIC => cell.getNumericCellValue().toString
    	case _ => cell.getStringCellValue()
    }
  
  protected def next: Seq[Cell]

  private def createValue[T](valueMapper: Cell => Option[T]): Value[T] = {
    val nextCells = next
    def nextValue(index: Int) = blankToNone(readAnyValue)(nextCells(index))
    
    try {
      newValue(valueMapper(nextCells(0)), nextValue)
    } catch {
      case e: RuntimeException =>
        throw new RuntimeException(ReaderError(e.getMessage()).description(nextCells(0)))
    }

  }

  protected def newValue[T](value: Option[T], nextValue: Int => Option[String]) =
    Value(value,
      nextValue(1),
      nextValue(2),
      nextValue(3),
      nextValue(4))
}

/**
 * {{CellReader}} that expects vertical cell groups, that is, data items are found in columns
 * @author flbulgarelli
 */
class ColumnOrientedReader(
    override val columnOffset: Int, 
    override val rows: Seq[Row]) extends CellReader with ColumnOrientedLike {

  override protected def next = cellIterators.map(_.next)
}

/**
 * {{CellReader}} that expects horizontal cell groups, that is, different
 * data items are found in rows
 * @author flbulgarelli
 */
class RowOrientedReader(
    override val offset: Offset, 
    override val rows: Seq[Row]) extends CellReader with RowOrientedLike {
  
  override protected def next = rowIterator.next.cells.drop(offset.columnIndex) 
  override protected def newValue[T](value: Option[T], nextStringValue: Int => Option[String]) =
    Value(value,
      None,
      None,
      nextStringValue(1),
      nextStringValue(2))
}