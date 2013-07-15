package libt.spreadsheet.reader

import org.apache.poi.ss.usermodel.Sheet
import libt.spreadsheet.writer._
import libt.spreadsheet.util._
import libt.spreadsheet._
import libt.error.Validated._
import libt.error._
import libt._

/**
 * The layout of an area, that defines
 * the exact way a sheet is read and written
 * */
sealed trait Layout {
  def read(area: Area, sheet: Sheet): Validated[Seq[Model]]
  def write(area: Area, sheet: Sheet, models: Seq[Model]) 
}

object RowOrientedLayout extends Layout {
  override def read(area: Area, sheet: Sheet) = 
    area.makeModel(sheet.rows, new RowOrientedReader(area.offset, _, WithPartialMetadataValueReader)).map(Seq(_))
  override def write(area: Area, sheet: Sheet, models: Seq[Model]) = ???
}

class ColumnOrientedLayout(valueReader: ValueReader) extends Layout with LibtSizes {
  override def read(area: Area, sheet: Sheet) =
      effectiveRowGroups(area, sheet).
      concatMap { rows => area.makeModel(rows, new ColumnOrientedReader(area.offset.columnIndex, _, valueReader)) }

  override def write(area: Area, sheet: Sheet, models: Seq[Model]) {
    sheet.defineLimits(area.offset, models.size * valueReader.blockSize, area.columns.size)
    (effectiveRowGroups(area, sheet).toSeq, models).zipped.foreach { (row, model) =>
      val writer = new ColumnOrientedWriter(area.offset.columnIndex, row)
      area.columns.foreachWithOps(model, area.schema) { ops =>
        writer.write(ops.value :: ops.metadata)
      }
    }
  }

  def effectiveRowGroups(area: Area, sheet: Sheet) =
    area.truncate(sheet.rows(area.offset).grouped(valueReader.blockSize).map(_.toList).toSeq)
}

object ColumnOrientedLayout extends ColumnOrientedLayout(WithMetadataValueReader)
