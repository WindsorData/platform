package libt.spreadsheet

import libt._
import builder._
import reader._

/**
 * A declarative description of a mapping of a Model to an
 * Excel file, for both reading from and writing to it
 *
 * @author flbulgarelli
 * @author metalkorva
 */
case class Mapping(columns: Column*) //TODO output

/**
 * The declaration of the content of a column, that may either important - Feature  - or unimportant - Gap
 */
sealed trait Column {
  /***Reads from a CellReader, using the given TElement as schema, and collects
   * the results into the given ModelBuilder*/
  def read(reader: CellReader, schema: TElement, modelBuilder: ModelBuilder)
}

/**A column whose value is important and should be read or written from and to Model's Value*/
case class Feature(path: Path) extends Column {
  def read(reader: CellReader, schema: TElement, modelBuilder: ModelBuilder) =
    modelBuilder += (path -> readValue(schema, reader))

  private def readValue(schema: TElement, reader: CellReader) = featureReader(schema(path)).read(reader)
  private def featureReader(tValue: TValue): FeatureReader[_] = tValue match {
    case TString => StringReader
    case _:TEnum => StringReader
    case TNumber => NumericReader
    case TInt => NumericReader
    case TBool => ???
    case TDate => ???
  }
}

/**A column whose value is not important and should be skipped*/
case object Gap extends Column {
  override def read(reader: CellReader, schema: TElement, modelBuilder: ModelBuilder) =
    reader.skip(1)
}