package libt.spreadsheet.reader

import util.FileManager._
import libt.error._
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
import libt.spreadsheet._
import libt._

class WorkbookReader[A](wbMapping: WorkbookMapping, combiner: Combiner[A]) {
  //TODO move to WBMapping
  def read(in: InputStream): A = read(WorkbookFactory.create(in))
  def read(wb: Workbook): A =
    combiner.combineReadResult(wb, wbMapping.read(wb).filter(!_.isEmpty))
}

case class WorkbookMapping(areas: Seq[SheetDefinition]) {

  def sheetsWithAreas[A](wb: Workbook) = {
    val sheets = for (sheetIndex <- 0 to wb.getNumberOfSheets() - 1) yield wb.getSheetAt(sheetIndex)
    (sheets, areas).zipped 
  }
  def read(wb: Workbook) = sheetsWithAreas(wb).map((sheet, area) => area.read(sheet))
  def write(models: Seq[Model], wb: Workbook) = sheetsWithAreas(wb).foreach((sheet, area) => area.write(models)(sheet)) 
}

trait Combiner[A] {
  def combineReadResult(wb: Workbook, results: Seq[Seq[Validated[Model]]]): A
}

trait SheetDefinition {
  def read(sheet: Sheet): Seq[Validated[Model]]
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
  limit: Option[Int],
  orientation: Layout,
  columns: Seq[Strip]) extends SheetDefinition {

  import libt.spreadsheet.util._

  def read(sheet: Sheet): Seq[Validated[Model]] =
    orientation.read(this, sheet)

  def write(models: Seq[Model])(sheet: Sheet) = 
    orientation.write(this, sheet, models)
  
  import libt.error._
  private[reader] def makeModel(rows: Seq[Row], orientation: Seq[Row] => CellReader) = {
    val modelBuilder = new ModelBuilder()
    val reader = orientation(rows)
    columns
    	.impureMap(column => Validated(column.read(reader, schema, modelBuilder)))
    	.join
    	.map(_ => modelBuilder.build)
  }

  def continually = Stream.continually[SheetDefinition](this)
}

object AreaGap extends SheetDefinition {
  def read(sheet: Sheet) = Nil
  def write(models: Seq[Model])(sheet: Sheet): Unit = ()
}
