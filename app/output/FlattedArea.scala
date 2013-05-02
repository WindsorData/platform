package output

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
import libt.spreadsheet.reader.Offset

//TODO refactor packages
/**
 * *
 * An Area that maps flattened models.
 * It is designed for writing only
 */
case class FlattedArea(
  /**
   * The PK of the root element, that is
   * the key that will be repeated in each
   * row or column
   */
  rootPK: PK,
  flatteningPK: PK,
  /**
   * The path of the elements to flatten.
   * It must point to a collection.
   */
  flatteningPath: Path,
  schema: TModel,
  layout: FlattedAreaLayout,
  columns: Seq[Column])
  extends SheetDefinition {

  //TODO remove method 
  def read(sheet: Sheet): Seq[Model] = ???

  def write(models: Seq[Model])(sheet: Sheet) =
    layout.write(models, sheet, this)

  def featuresSize = columns.size + 1

  def rootPKSize = rootPK.size

  def completePKSize = rootPKSize + flatteningPK.size

  def flatteningColSize(models: Seq[Model]) =
    models.map(_.apply(flatteningPath).asCol.size).sum

  /**
   * Flattens the given model using the root primary
   * key and flattening path
   */
  def flatten(models: Seq[Model]) =
    Model.flattenWith(models, rootPK, flatteningPath)

  //TODO remove
  protected def * = 0
  protected def flattedSchema = schema(flatteningPath)

  def newWriter(writer: CellWriter, flattedModel: Model) = new {

    def writeTitles = ??? //TODO

    /**writes each pk of the root*/
    def writeRootPKHeaders = writePKHeaders(rootPK)

    /**writes each pk of the flattened model*/
    def writeFlattedPKHeaders = writePKHeaders(flatteningPK)

    /**writes each feature of the (flattened) model*/
    def writeFlattedModelFeatures =
      columns.foreach(_.write(writer, flattedSchema(Path(*)), flattedModel))

    private def writePKHeaders(pk: PK) =
      pk.map(Feature(_)).foreach(_.write(writer, schema, flattedModel))

  }
}

trait FlattedAreaLayout {
  def write(models: Seq[Model], sheet: Sheet, area: FlattedArea): Unit
}

case class ValueAreaLayout(offset: Offset) extends FlattedAreaLayout {
  override def write(models: Seq[Model], sheet: Sheet, area: FlattedArea) {
    sheet.defineLimits(offset,
      models.size * area.flatteningColSize(models),
      area.featuresSize)
    sheet.rows.drop(offset.rowIndex).zip(area.flatten(models)).foreach {
      case (row, flattedModel) => {
        val writer = area.newWriter(
          new ColumnOrientedValueWriter(offset.columnIndex, row),
          flattedModel)
        writer.writeRootPKHeaders
        writer.writeFlattedModelFeatures
      }
    }
  }
}

case class MetadataAreaLayout(offset: Offset) extends FlattedAreaLayout {
  override def write(models: Seq[Model], sheet: Sheet, area: FlattedArea) {
    sheet.defineLimits(offset,
      area.flatteningColSize(models) * area.featuresSize,
      area.completePKSize
        + 2 //number of titles 
        + 4 //number of metadata features
        )
    sheet
      .rows
      .drop(offset.rowIndex)
      .grouped(area.featuresSize)
      .zip(area.flatten(models).iterator)
      .foreach {
        case (rows, flattedModel) => {
          val writer = area.newWriter(
            new RowOrientedMetadataWriter(rows),
            flattedModel)
          //          writer.writeRootPKHeaders
          //          writer.writeFlattedPKHeaders
          //          writer.writeTitles
          writer.writeFlattedModelFeatures
        }
      }
  }
}



