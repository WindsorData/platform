import libt._
import libt.spreadsheet.Column
import libt.spreadsheet.reader.SheetDefinition
import org.apache.poi.ss.usermodel.Sheet
import libt.spreadsheet.util._
import libt.spreadsheet.writer.ColumnOrientedWriter
import libt.spreadsheet.Feature
import libt.spreadsheet.writer.CellWriter

package output {
  //TODO refactor packages
  case class FlattedArea(
    rootPrimaryKey: PK,
    flatteningPath: Path,
    schema: TModel,
    columns: Seq[Column]) extends SheetDefinition {

    val * = 0

    private def flattedSchema = schema(flatteningPath)

    def defineSheetLimits(sheet: Sheet, x: Int, y: Int) =
      for (n <- 1 to y; m <- 1 to x)
        sheet.createRow(n).createCell(m).setAsActiveCell()

    //TODO remove method 
    def read(sheet: Sheet): Seq[Model] = ???

    def write(models: Seq[Model])(sheet: Sheet): Unit = {
      defineSheetLimits(sheet, models.size * 5, columns.size)

      sheet.rows.zip(flattenWith(models, rootPrimaryKey, flatteningPath)).foreach {
        case (row, flattedModel) => {
          val writer = new FlatterModelWriter(
            new ColumnOrientedWriter(row),
            flattedModel)
          writer.writeHeaders
          writer.writeFlattedModel
        }
      }
    }

    class FlatterModelWriter(writer: CellWriter, flattedModel: Model) {
      def writeHeaders =
        rootPrimaryKey.map(Feature(_)).foreach(_.write(writer, schema, flattedModel))

      def writeFlattedModel =
        columns.foreach(_.write(writer, flattedSchema(Path(*)), flattedModel))
    }
  }
}

package object output {
  type PK = Seq[Path]
  object PK {
    def apply(elements: Path*) = elements
  }

  def flattenWith(models: Seq[Model], rootPk: PK, flatteningPath: Path): Seq[Model] =
    models.flatMap(_.flattenWith(rootPk, flatteningPath))
}