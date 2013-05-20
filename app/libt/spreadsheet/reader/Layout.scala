package libt.spreadsheet.reader

import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet

import libt.spreadsheet.writer._
import libt.spreadsheet.util._
import libt.spreadsheet._
import libt.builder._
import libt._

/**
 * The layout of an area, that defines
 * the exact way a sheet is read and written
 * */
sealed trait Layout {
  def read(area: Area, sheet: Sheet): Seq[ModelOrErrors]
  def write(area: Area, sheet: Sheet, models: Seq[Model]) 
}

object RowOrientedLayout extends Layout {
  override def read(area: Area, sheet: Sheet) = 
    Seq(area.makeModel(sheet.rows, new RowOrientedReader(area.offset, _)))
  override def write(area: Area, sheet: Sheet, models: Seq[Model]) = ???
}

object ColumnOrientedLayout extends Layout {
  
  override def read(area: Area, sheet: Sheet) =
    effectiveRowGroups(area, sheet).map { rows =>
      area.makeModel(rows, new ColumnOrientedReader(area.offset.columnIndex, _))
    }.toSeq

  override def write(area: Area, sheet: Sheet, models: Seq[Model]) {
    sheet.defineLimits(area.offset, models.size * 6, area.columns.size)
    (effectiveRowGroups(area, sheet).toSeq, models).zipped.foreach { (row, model) =>
      val writer = new ColumnOrientedWriter(area.offset.columnIndex, row)
      area.columns.foreachWithOps(model, area.schema) { ops =>
        writer.write(ops.value :: ops.metadata)
      }
    }
  }
    
  def effectiveRowGroups(area: Area, sheet: Sheet) = 
     sheet.rows(area.offset).grouped(6)   
}



