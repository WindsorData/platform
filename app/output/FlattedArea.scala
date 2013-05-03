package output

import libt._
import libt.spreadsheet.Strip
import libt.spreadsheet.reader.SheetDefinition
import org.apache.poi.ss.usermodel.Sheet
import libt.spreadsheet.util._
import libt.spreadsheet.Feature
import libt.spreadsheet.writer.CellWriter
import org.apache.poi.ss.usermodel.Row
import libt.spreadsheet.writer.ColumnOrientedWriter
import libt.spreadsheet.writer.RowOrientedWriter
import libt.spreadsheet.Offset
import libt.spreadsheet.LibtSizes

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
  columns: Seq[Strip])
  extends SheetDefinition with LibtSizes {

  //TODO remove method 
  def read(sheet: Sheet): Seq[Model] = ???

  def write(models: Seq[Model])(sheet: Sheet) =
    layout.write(models, sheet, this)

  def featuresSize = columns.size + 1

  def rootPKSize = rootPK.size

  def completePKSize = rootPKSize + flatteningPK.size
  
  def headerSize = completePKSize + TitlesSize 

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
  protected def flattedSchema = schema(flatteningPath)(Path(*))

  def newWriter(writer: CellWriter, flattedModel: Model) = new {

    /**writes each pk of the root*/
    def writeRootPKHeaders = writePKHeaders(schema, rootPK)

    /**writes each pk of the flattened model*/
    def writeFlattedPKHeaders = writePKHeaders(flattedSchema, flatteningPK)

    def writeFlattedModelFeaturesValues =
      columns.foreachWithOps(flattedModel, flattedSchema) { ops =>
        writer.write(ops.value :: Nil)
      }

    def writeFlattedModelFeaturesMetadataWithTitle =
      columns.foreachWithOps(flattedModel, flattedSchema) { ops =>
        writer.write(ops.titles ++ ops.metadata)
      }

    private def writePKHeaders(schema: TElement, pk: PK) =
      pk.map(Feature(_)).foreachWithOps(flattedModel, schema) { ops =>
        writer.write(ops.value :: Nil)
      }
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
    (sheet.rows.drop(offset.rowIndex), area.flatten(models)).zipped.foreach { (row, flattedModel) =>
      val writer = area.newWriter(new ColumnOrientedWriter(offset.columnIndex, Seq(row)),
        flattedModel)
      writer.writeRootPKHeaders
      writer.writeFlattedModelFeaturesValues
    }
  }
}

case class MetadataAreaLayout(offset: Offset) extends FlattedAreaLayout with LibtSizes {
  override def write(models: Seq[Model], sheet: Sheet, area: FlattedArea) {
    sheet.defineLimits(offset,
      area.flatteningColSize(models) * area.featuresSize,
      area.headerSize + MetadataSize)
    (sheet.rows.drop(offset.rowIndex).grouped(area.featuresSize).toSeq, area.flatten(models)).zipped.foreach { 
      (rows, flattedModel) =>
      rows.foreach { row =>
        val headersWriter = area.newWriter(new ColumnOrientedWriter(offset.rowIndex, Seq(row)), flattedModel)
        headersWriter.writeRootPKHeaders
        headersWriter.writeFlattedPKHeaders
      }

      val writer = area.newWriter(new RowOrientedWriter(offset + Offset(0, area.completePKSize), rows), flattedModel)
      writer.writeFlattedModelFeaturesMetadataWithTitle
    }
  }
}



