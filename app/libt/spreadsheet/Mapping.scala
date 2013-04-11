package libt.spreadsheet

import libt._
import builder._
import reader._

case class Mapping(columns: Column*)

trait Column {
  def read(reader: CellReader, schema: TElement, modelBuilder: ModelBuilder)
}

case class Feature(path: Path) extends Column {
  def read(reader: CellReader, schema: TElement, modelBuilder: ModelBuilder) =
    modelBuilder += (path -> readValue(schema, reader))

  private def readValue(schema: TElement, reader: CellReader) = featureReader(schema(path)).read(reader)
  private def featureReader(tValue: TValue): FeatureReader[_] = tValue match {
    case TString => StringReader
    case TNumber => NumericReader
    case e: TEnum => EnumReader(e)
  }
}

case object Gap extends Column {
  override def read(reader: CellReader, schema: TElement, modelBuilder: ModelBuilder) =
    reader.skip(1)
}