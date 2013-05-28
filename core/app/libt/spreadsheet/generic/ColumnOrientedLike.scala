package libt.spreadsheet.generic
import libt.spreadsheet.util._

import org.apache.poi.ss.usermodel.Row

/**
 * statefull mixin for writers or readers that fetch cells
 * by iterating over columns, that is, each {{{Value}}} is placed in 
 * a single column
 * */
trait ColumnOrientedLike extends SkipeableLike {
  val columnOffset: Int
  val rows: Seq[Row]
  
  protected val cellIterators = rows.map(_.cells).map(_.iterator)
  
  skip(columnOffset)
  
  override protected def skip1 = cellIterators.foreach(_.next)
}