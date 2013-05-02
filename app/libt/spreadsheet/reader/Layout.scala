package libt.spreadsheet.reader

import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet

import libt.spreadsheet.writer._
import libt.spreadsheet._
import libt.builder._
import libt._

sealed trait Layout {
  def read(area: Area, sheet: Sheet): Seq[Model]
  def write(area: Area, sheet: Sheet, models: Seq[Model]) 
}
object RowOrientedLayout extends Layout {
  import libt.spreadsheet.util._
  override def read(area: Area, sheet: Sheet) = 
    Seq(area.makeModel(sheet.rows, new RowOrientedReader(area.offset, _)))
  override def write(area: Area, sheet: Sheet, models: Seq[Model]) = ???
}

object ColumnOrientedLayout extends Layout {
  import libt.spreadsheet.util._
  
  override def read(area: Area, sheet: Sheet) =
    sheet.rows.drop(area.offset.rowIndex).grouped(6).map { rows =>
      area.makeModel(rows, new ColumnOrientedReader(area.offset.columnIndex, _))
    }.toSeq

  override def write(area: Area, sheet: Sheet, models: Seq[Model]) {
    sheet.defineLimits(area.offset, models.size * 6, area.columns.size)
    (sheet.rows.drop(area.offset.rowIndex).grouped(6).toSeq, models).zipped.foreach { (row, model) =>
      val writer = new ColumnOrientedWriter(area.offset.columnIndex, row)
      area.columns.foreachWithOps(model, area.schema) { ops =>
        writer.write(ops.value :: ops.metadata)
      }
    }
  }
}




