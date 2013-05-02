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
  def read(schema: TModel, columns: Seq[Column], sheet: Sheet, offset: Offset): Seq[Model]

  def makeModels(schema: TModel, columns: Seq[Column], rows: Seq[Row], orientation: Seq[Row] => CellReader): Model = {
    val modelBuilder = new ModelBuilder()
    val reader = orientation(rows)
    for (column <- columns)
      column.read(reader, schema, modelBuilder)
    modelBuilder.build
  }
}
object RowOrientation extends Orientation {
  import libt.spreadsheet.util._
  override def read(schema: TModel, columns: Seq[Column], sheet: Sheet, offset: Offset): Seq[Model] = {
    Seq(makeModels(schema, columns, sheet.rows, new RowOrientedReader(offset, _)))
  }
}

object ColumnOrientation extends Orientation {
  import libt.spreadsheet.util._
  override def read(schema: TModel, columns: Seq[Column], sheet: Sheet, offset: Offset): Seq[Model] = {
    sheet.rows.drop(offset.rowIndex).grouped(6).map { rows =>
      makeModels(schema, columns, rows, new ColumnOrientedReader(offset.columnIndex, _))
    }.toSeq
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
    orientation.read(schema, columns, sheet, offset)

  def write(models: Seq[Model])(sheet: Sheet): Unit = ???

  def continually = Stream.continually[SheetDefinition](this)
}

object AreaGap extends SheetDefinition {
  def read(sheet: Sheet) = Nil
  def write(models: Seq[Model])(sheet: Sheet): Unit = ???
}