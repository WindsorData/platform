import libt._
import libt.spreadsheet.Column
import libt.spreadsheet.reader.SheetDefinition
import org.apache.poi.ss.usermodel.Sheet
import libt.spreadsheet.util._
import libt.spreadsheet.writer.ColumnOrientedValueWriter
import libt.spreadsheet.Feature
import libt.spreadsheet.writer.CellWriter
import libt.spreadsheet.writer.RowOrientedMetadataWriter
import org.apache.poi.ss.usermodel.Row

package output {

  trait FlattedAreaLayout {
    def write(models: Seq[Model], sheet: Sheet, area: FlattedArea): Unit
  }

  case object ValueAreaLayout extends FlattedAreaLayout {
    override def write(models: Seq[Model], sheet: Sheet, area: FlattedArea) {
      sheet.defineLimits(models.size * 5, area.featuresSize) //TODO why by 5??
      sheet.rows.zip(area.flatten(models)).foreach {
        case (row, flattedModel) => {
          val writer = area.newWriter(
            new ColumnOrientedValueWriter(row),
            flattedModel)
          writer.writeRootPKHeaders
          writer.writeFlattedModelFeatures
        }
      }
    }
  }

  case object MetadataAreaLayout extends FlattedAreaLayout {
    override def write(models: Seq[Model], sheet: Sheet, area: FlattedArea) {
      sheet.defineLimits(models.size * area.featuresSize, area.completePKSize)
      sheet
        .rows
        .grouped(area.featuresSize)
        .zip(area.flatten(models).iterator)
        .foreach {
          case (rows, flattedModel) => {
            val writer = area.newWriter(
              new RowOrientedMetadataWriter(rows),
              flattedModel)
            writer.writeRootPKHeaders
            writer.writeFlattedPKHeaders
            writer.writeTitles
            writer.writeFlattedModelFeatures
          }
        }
    }
  }

  //TODO refactor packages
  case class FlattedArea(
    rootPK: PK,
    flatteningPK: PK,
    flatteningPath: Path,
    schema: TModel,
    layout: FlattedAreaLayout,
    columns: Seq[Column]) extends SheetDefinition {

    //TODO remove method 
    def read(sheet: Sheet): Seq[Model] = ???

    def write(models: Seq[Model])(sheet: Sheet) =
      layout.write(models, sheet, this)

    def featuresSize = columns.size + 1

    def rootPKSize = rootPK.size

    def completePKSize = rootPKSize + flatteningPK.size

    def flatten(models: Seq[Model]) =
      Model.flattenWith(models, rootPK, flatteningPath)

    protected def * = 0
    protected def flattedSchema = schema(flatteningPath)

    def newWriter(writer: CellWriter, flattedModel: Model) = new {

      def writeTitles = () //TODO

      def writeRootPKHeaders = writePKHeaders(rootPK)

      def writeFlattedPKHeaders = writePKHeaders(???)

      def writeFlattedModelFeatures =
        columns.foreach(_.write(writer, flattedSchema(Path(*)), flattedModel))

      private def writePKHeaders(pk: PK) =
        pk.map(Feature(_)).foreach(_.write(writer, schema, flattedModel))

    }
  }
}

