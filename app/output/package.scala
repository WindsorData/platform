import libt._
import libt.spreadsheet.Column
import libt.spreadsheet.reader.SheetDefinition
import org.apache.poi.ss.usermodel.Sheet
import libt.spreadsheet.util._
import libt.spreadsheet.writer.ColumnOrientedWriter
import libt.spreadsheet.Feature
import libt.spreadsheet.writer.CellWriter
import libt.spreadsheet.writer.RowOrientedWriter

package output {
  //TODO refactor packages
  case class FlattedArea(
    rootPK: PK,
    flatteningPK: PK,
    flatteningPath: Path,
    schema: TModel,
    columns: Seq[Column]) extends SheetDefinition {

    val * = 0

    private def flattedSchema = schema(flatteningPath)

    private def defineSheetLimits(sheet: Sheet, x: Int, y: Int) =
      for (n <- 1 to y; m <- 1 to x)
        sheet.createRow(n).createCell(m).setAsActiveCell()

    //TODO remove method 
    def read(sheet: Sheet): Seq[Model] = ???

    def write(models: Seq[Model])(sheet: Sheet) {
      defineSheetLimits(sheet, models.size * 5, columns.size) //TODO why by 5??

      sheet.rows.zip(Model.flattenWith(models, rootPK, flatteningPath)).foreach {
        case (row, flattedModel) => {
          val writer = new FlattedModelWriter(
            new ColumnOrientedWriter(row),
            flattedModel)
          writer.writeRootPKHeaders
          writer.writeFlattedModelFeatures
        }
      }
    }

    def modelHeight = columns.size + 1

    def write2(models: Seq[Model])(sheet: Sheet) {
      defineSheetLimits(sheet, models.size * modelHeight, rootPK.size) //TODO  + flatteningPk.size

      sheet
        .rows
        .grouped(modelHeight)
        .zip(Model.flattenWith(models, rootPK, flatteningPath).iterator)
        .foreach {
          case (rows, flattedModel) => {
            val writer = new FlattedModelWriter(
              new RowOrientedWriter(rows),
              flattedModel)
            writer.writeRootPKHeaders
            writer.writeFlattedPKHeaders
            writer.writeTitles
            writer.writeFlattedModelFeatures
          }
        }
    }

    class FlattedModelWriter(writer: CellWriter, flattedModel: Model) {
      private def writePKHeaders(pk: PK) =
        pk.map(Feature(_)).foreach(_.write(writer, schema, flattedModel))

      def writeTitles = () //TODO

      def writeRootPKHeaders = writePKHeaders(rootPK)

      def writeFlattedPKHeaders = writePKHeaders(???)

      def writeFlattedModelFeatures =
        columns.foreach(_.write(writer, flattedSchema(Path(*)), flattedModel))
    }
  }
}

