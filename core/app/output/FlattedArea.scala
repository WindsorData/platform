package output

import org.apache.poi.ss.usermodel.Sheet
import libt.spreadsheet._
import libt.spreadsheet.util._
import libt.spreadsheet.reader._
import libt.spreadsheet.writer._
import libt.error._
import libt._
import libt.TModel
import libt.spreadsheet.Offset

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
  columns: Seq[Strip],
  writeStrategy: WriteStrategy = FullWriteStrategy)
  extends SelectiveSheetDefinition with LibtSizes {

  def featuresSize = columns.size

  def rootPKSize = rootPK.size

  def completePKSize = rootPKSize + flatteningPK.size

  def headerSize = completePKSize + TitlesSize

  def flatteningColSize(models: Seq[Model]) =
    models.map(_.applySeq(flatteningPath).size).sum

  /**
   * Flattens the given model using the root primary
   * key and flattening path
   */
  def flatten(models: Seq[Model]) =
    Model.flattenWith(models, rootPK, flatteningPath)

  protected def flattedSchema = schema(flatteningPath)(Path(*))

  def read(sheet: Sheet): Validated[Seq[Model]] = ???

  def newWriter(writer: CellWriter, flattedModel: Model) = new {

    /**writes each pk of the root*/
    def writeRootPKHeaders = writePKHeaders(schema, rootPK)

    def writeFlattedModelFeaturesValues =
      columns.foreachWithOps(flattedModel, flattedSchema) { ops =>
        writer.write(ops.value :: Nil)
      }

    def writeFlattedModelFeaturesMetadataWithPkAndTitle = {
      val rootPkOps = rootPK.writeOps(schema, flattedModel)
      val flatteningPKOps =
        if (flatteningPK.exists(_.nonEmpty))
          flatteningPK.writeOps(flattedSchema, flattedModel)
        else
          WriteOps.skip(flatteningPK.length)

        columns.foreachWithOps(flattedModel, flattedSchema) { ops =>
          if (ops.hasMetadata)
            writer.write(rootPkOps ++ flatteningPKOps ++ ops.titles ++ ops.metadata)
        }
      }


    private def writePKHeaders(schema: TElement,pk: PK) =
      pk.writeOps(schema, flattedModel).foreach { op =>
        writer.write(op :: Nil)
      }
  }

  def selectiveWrite(models: Seq[Model], sheet: Sheet): Unit =
    layout.write(models, sheet, this)
}

trait FlattedAreaLayout {
  def write(models: Seq[Model], sheet: Sheet, area: FlattedArea): Unit
}

case class ValueAreaLayout(offset: Offset) extends FlattedAreaLayout {
  def write(models: Seq[Model], sheet: Sheet, area: FlattedArea) {
    sheet.defineLimits(offset,
      models.size * area.flatteningColSize(models),
      area.featuresSize)
    (sheet.rows(offset), area.flatten(models)).zipped.foreach { (row, flattedModel) =>
      val writer = area.newWriter(new ColumnOrientedWriter(offset.columnIndex, Seq(row)),
        flattedModel)
      writer.writeRootPKHeaders
      writer.writeFlattedModelFeaturesValues
    }
  }
}

case class MetadataAreaLayout(offset: Offset) extends FlattedAreaLayout with LibtSizes {

  def write(models: Seq[Model], sheet: Sheet, area: FlattedArea) = {
    sheet.defineLimits(offset,
      area.flatteningColSize(models) * area.featuresSize,
      area.headerSize + MetadataSize)
    val rows = sheet.rows(offset).iterator
    area.flatten(models).foreach { flattedModel =>
      val writer = area.newWriter(new RowOrientedWriter(Offset(0, offset.columnIndex), rows), flattedModel)
      writer.writeFlattedModelFeaturesMetadataWithPkAndTitle
    }
  }
}