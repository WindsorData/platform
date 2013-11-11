package libt.spreadsheet.reader

import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Row
import libt.builder.ModelBuilder
import libt.spreadsheet._
import libt.error._
import libt._

case class WorkbookMapping(areas: Seq[SheetDefinition]) {

  protected def sheetsWithAreas[A](wb: Workbook) = {
    val sheets = for (sheetIndex <- 0 to wb.getNumberOfSheets() - 1) yield wb.getSheetAt(sheetIndex)
    (sheets, areas).zipped 
  }
  def read(wb: Workbook) : Validated[Seq[Seq[Model]]] =
    sheetsWithAreas(wb).map((sheet, area) => area.read(sheet)).concat
    
  def write(models: Seq[Model], wb: Workbook) : Unit =
    sheetsWithAreas(wb).foreach((sheet, area) => area.write(models)(sheet)) 
}

trait SheetDefinition {
  def read(sheet: Sheet): Validated[Seq[Model]]
  def write(models: Seq[Model])(sheet: Sheet): Unit
}

/**
 * A declarative description of a mapping of a Model to an
 * Excel file, for both reading from and writing to it
 *
 * @author flbulgarelli
 * @author mcorbanini
 */
trait AreaLike {
  val offset: Offset
  val schema: TModel
  val limit: Option[Int]
  val columns: Seq[Strip]

  private[reader] def makeModel(rows: Seq[Row], orientation: Seq[Row] => CellReader) : Validated[Model]= {
    val modelBuilder = new ModelBuilder()
    val reader = orientation(rows)
    columns
      .impureMap(column => Validated(column.read(reader, schema, modelBuilder)))
      .concat
      .map(_ => modelBuilder.build)
  }

  /**limits the list of rows groups, if necessary*/
  private [reader] def truncate(rowsGroups: Seq[List[Row]]) =
    limit match {
      case None => rowsGroups
      case Some(limit) => rowsGroups.take(limit)
    }
}

/**
 * A standard implementation of an Area.
 *
 * @author mcorbanini
 */
case class Area(
  schema: TModel,
  offset: Offset,
  limit: Option[Int],
  orientation: Layout,
  columns: Seq[Strip]) extends AreaLike with SheetDefinition {

  def read(sheet: Sheet): Validated[Seq[Model]] =
    orientation.read(this, sheet)

  def write(models: Seq[Model])(sheet: Sheet) = 
    orientation.write(this, sheet, models)

  def continually = Stream.continually[SheetDefinition](this)
}

object AreaGap extends SheetDefinition {
  def read(sheet: Sheet) = Valid(Nil)
  def write(models: Seq[Model])(sheet: Sheet): Unit = ()
}
