package libt.spreadsheet.generic

import org.apache.poi.ss.usermodel.Row
import libt.spreadsheet.Offset

/**
 * statefull mixin for writers or readers that fetch cells
 * by iterating over rows. That is, a single Value is placed 
 * in a single row 
 * */
trait RowOrientedLike extends SkipeableLike {
  val offset: Offset
  val rows: TraversableOnce[Row]
  
  protected val rowIterator = rows.toIterator.drop(offset.rowIndex)
  override protected def skip1 = rowIterator.next
}