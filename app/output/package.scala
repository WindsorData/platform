import libt._
import libt.spreadsheet.Column
import libt.spreadsheet.reader.SheetDefinition
import org.apache.poi.ss.usermodel.Sheet
import libt.spreadsheet.util._
import libt.spreadsheet.writer.ColumnOrientedWriter

package output {
  case class PK(elements: Path*)

  case class FlattedArea(
    rootPrimaryKey: PK,
    flatteningPath: Path,
    schema: TModel,
    columns: Seq[Column]) extends SheetDefinition {

    val * = 0
    
    def defineSheetLimits(sheet: Sheet, x: Int, y: Int) =
      for (n <- 1 to y; m <- 1 to x) 
        sheet.createRow(n).createCell(m).setAsActiveCell()

    def read(sheet: Sheet): Seq[Model] = ???

    def write(models: Seq[Model])(sheet: Sheet): Unit = {
      defineSheetLimits(sheet, models.size * 5, columns.size)

      sheet.rows.zip(flattenWith(models, rootPrimaryKey, flatteningPath)).foreach {
        case (row, model) => {
          val writer = new ColumnOrientedWriter(row)
          columns.foreach(_.write(writer, schema(flatteningPath)(Path(*)), model))
        }
      }
    }
  }
}

package object output {

  def flattenWith(models: Seq[Model], rootPk: PK, flatteningPath: Path): Seq[Model] =
    models.flatMap(_.flattenWith(rootPk, flatteningPath))
}