package libt.spreadsheet

import libt._
import builder._
import reader._
import org.apache.poi.ss.usermodel.Sheet

import libt.spreadsheet.util._

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
  private def featureReader[A](tValue: TValue[A]): FeatureReader[A] = tValue match {
    case TString => StringReader
    case _:TEnum => StringReader
    case TNumber => NumericReader
    case TWithDefault(baseReader, default) => WithDefaultReader(featureReader(baseReader), default)
    case TInt => ???
    case TBool => ???
    case TXBool => XBoolReader
    case TDate => DateReader
  }
}

object Feature {
  def apply(pathParts: PathPart*) : Feature = Feature(pathParts.toList) 
}

/**A column whose value is not important and should be skipped*/
case object Gap extends Column {
  
  override def read(reader: CellReader, schema: TElement, modelBuilder: ModelBuilder) =
    reader.skip(1)
}