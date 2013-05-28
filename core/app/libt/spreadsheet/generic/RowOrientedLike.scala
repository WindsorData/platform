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
  val rows: Seq[Row]
  
  protected val rowIterator = rows.drop(offset.rowIndex).iterator
  override protected def skip1 = rowIterator.next
}