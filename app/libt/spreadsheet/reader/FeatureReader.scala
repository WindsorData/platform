package libt.spreadsheet.reader
import libt._

trait FeatureReader[A] {
  def read(reader: CellReader): Value[A]
  //    def readWithDefault(reader: CellReader, defaultValue: A): Value[A]
}

case object NumericReader extends FeatureReader[BigDecimal] {
  def read(reader: CellReader) = reader.numeric
  //    def readWithDefault(reader: CellReader, defaultValue: BigDecimal) = reader.numericWithDefault(defaultValue)
}

case object StringReader extends FeatureReader[String] {
  def read(reader: CellReader) = reader.string
  //    def readWithDefault(reader: CellReader, defaultValue: String) = reader.stringWithDefault(defaultValue)
}

case class WithDefaultReader[A](
  baseFeatureReader: FeatureReader[A],
  defaultValue: A) extends FeatureReader[A] {
  def read(reader: CellReader) = baseFeatureReader.read(reader).orDefault(defaultValue)
}
