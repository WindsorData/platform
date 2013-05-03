package libt.spreadsheet.reader

import util.FileManager._
import libt._
import libt.util._
import libt.spreadsheet._
import java.io.InputStream
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Row
import libt.builder.ModelBuilder
import libt.spreadsheet.util.sheet2RichSheet
import libt.spreadsheet.writer.ColumnOrientedWriter

class WorkbookReader[A](wbMapping: WorkbookMapping, combiner: Combiner[A]) {
  //TODO move to WBMapping
  def read(in: InputStream): A = read(WorkbookFactory.create(in))
  def read(wb: Workbook): A =
    combiner.combineReadResult(wb, wbMapping.read(wb).filter(!_.isEmpty))
}
//TODO varageize
case class WorkbookMapping(areas: Seq[SheetDefinition]) {

  //TODO wtf??
  def ioAction[A](wb: Workbook, action: (Sheet, SheetDefinition) => A) = {
    val sheets = for (sheetIndex <- 0 to wb.getNumberOfSheets() - 1) yield wb.getSheetAt(sheetIndex)
    sheets.zip(areas).map{ case (sheet, area) => action(sheet, area) }
  }

  def read(wb: Workbook) = ioAction(wb, (sheet, area) => area.read(sheet))
  def write(models: Seq[Model], wb: Workbook) = ioAction(wb, (sheet, area) => area.write(models)(sheet)) 
}

trait Combiner[A] {
  def combineReadResult(wb: Workbook, results: Seq[Seq[Model]]): A
}

case class Offset(rowIndex: Int, columnIndex: Int) {
  def +(that: Offset) =
    Offset(rowIndex + that.rowIndex, columnIndex + that.columnIndex)
}

sealed trait Orientation {
  def read(area: Area, sheet: Sheet): Seq[Model]
  def write(area: Area, sheet: Sheet, models: Seq[Model]) 
}
object RowOrientation extends Orientation {
  import libt.spreadsheet.util._
  override def read(area: Area, sheet: Sheet) = 
    Seq(area.makeModel(sheet.rows, new RowOrientedReader(area.offset, _)))
  override def write(area: Area, sheet: Sheet, models: Seq[Model]) = ???
}

object ColumnOrientation extends Orientation {
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

trait SheetDefinition {
  def read(sheet: Sheet): Seq[Model]
  def write(models: Seq[Model])(sheet: Sheet): Unit
}

/**
 * A declarative description of a mapping of a Model to an
 * Excel file, for both reading from and writing to it
 *
 * @author flbulgarelli
 * @author metalkorva
 */
case class Area(
    schema: TModel, 
    offset: Offset, 
    orientation: Orientation, 
    columns: Seq[Column]) extends SheetDefinition {
  
  import libt.spreadsheet.util._

  def read(sheet: Sheet): Seq[Model] =
    orientation.read(this, sheet)

  def write(models: Seq[Model])(sheet: Sheet) = 
    orientation.write(this, sheet, models)
    
  private[reader] def makeModel(rows: Seq[Row], orientation: Seq[Row] => CellReader) = {
    val modelBuilder = new ModelBuilder()
    val reader = orientation(rows)
    for (column <- columns)
      column.read(reader, schema, modelBuilder)
    modelBuilder.build
  }  

  def continually = Stream.continually[SheetDefinition](this)
}

object AreaGap extends SheetDefinition {
  def read(sheet: Sheet) = Nil
  def write(models: Seq[Model])(sheet: Sheet): Unit = ???
}