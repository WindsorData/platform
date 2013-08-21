package output

import org.apache.poi.ss.usermodel.{Cell, Row, Sheet}
import libt.util._
import libt._
import libt.spreadsheet.Strip
import libt.spreadsheet.reader.SheetDefinition
import libt.spreadsheet.util._
import libt.spreadsheet.reader._
import libt.spreadsheet.writer._
import libt.spreadsheet._
import libt.error._
import libt._

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
  extends SheetDefinition with LibtSizes {

  def write(models: Seq[Model])(sheet: Sheet) = {
    writeStrategy.write(models, this, sheet)
  }

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

    /**writes each pk of the flattened model*/
    def writeFlattedPKHeaders =
      if(flatteningPK.exists(_.nonEmpty))
        writePKHeaders(flattedSchema, flatteningPK)

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
    (sheet.rows(offset), area.flatten(models)).zipped.foreach { (row, flattedModel) =>
      val writer = area.newWriter(new ColumnOrientedWriter(offset.columnIndex, Seq(row)),
        flattedModel)
      writer.writeRootPKHeaders
      writer.writeFlattedModelFeaturesValues
    }
  }
}

case class MetadataAreaLayout(offset: Offset) extends FlattedAreaLayout with LibtSizes {

  override def write(models: Seq[Model], sheet: Sheet, area: FlattedArea) = {
    sheet.defineLimits(offset,
      area.flatteningColSize(models) * area.featuresSize,
      area.headerSize + MetadataSize)

    (sheet.rows(offset).grouped(area.featuresSize).toSeq, area.flatten(models)).zipped.foreach {
      (rows, flattedModel) =>

        rows.foreach { row =>
          val headersWriter = area.newWriter(new ColumnOrientedWriter(offset.columnIndex, Seq(row)), flattedModel)
          headersWriter.writeRootPKHeaders
          headersWriter.writeFlattedPKHeaders
        }

        val writer = area.newWriter(new RowOrientedWriter(Offset(0, area.completePKSize + offset.columnIndex), rows), flattedModel)
        writer.writeFlattedModelFeaturesMetadataWithTitle
    }

    /**
     * TODO:
     * This is almost a hack in order to avoid writing empty rows. The problem is that apache poi
     * does not have a method to delete an entirely row, it just let you clear all the cell values
     * for an specific row using removeRow method, not remove it. So it is forced to use shiftRows
     * method so it can shift empty rows to the bottom.
     *
     * The best way to avoid these empty rows will be not writing it at the first time, by changing
     * the area writer in order to achieve this.
     */

    var index = 0
    while (index < sheet.getLastRowNum) {
      if(isEmpty(sheet.getRow(index))){
        sheet.shiftRows(index + 1, sheet.getLastRowNum(), -1)
        index = index - 1 //Adjusts the sweep in accordance to a row removal
      }
      index = index + 1
    }

    def isEmpty(row: Row) =
      (6 to 9).flatMap { i =>
        blankToNone(_.getStringCellValue)(row.getCell(i))
      }.isEmpty

  }
}

trait WriteStrategy {
  def write(models: Seq[Model], area: FlattedArea, sheet: Sheet): Unit
}

/**
 * [[output.WriteStrategy]] that completelty delegates on the layout and
 * simply writes everything
 */
object FullWriteStrategy extends WriteStrategy {
  override def write(models: Seq[Model], area: FlattedArea, sheet: Sheet) =
    area.layout.write(models, sheet, area)
}

