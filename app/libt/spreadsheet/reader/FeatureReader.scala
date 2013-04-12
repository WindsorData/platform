package libt.spreadsheet.reader
import libt._

trait FeatureReader[A] {
  def read(reader: CellReader): Value[A]
}

case object NumericReader extends FeatureReader[BigDecimal] {
  def read(reader: CellReader) = reader.numeric
}

case object StringReader extends FeatureReader[String] {
  def read(reader: CellReader) = reader.string
}

case class WithDefaultReader[A](
  baseFeatureReader: FeatureReader[A],
  defaultValue: A) extends FeatureReader[A] {
  def read(reader: CellReader) = baseFeatureReader.read(reader).orDefault(defaultValue)
}
