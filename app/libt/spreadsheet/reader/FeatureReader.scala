package libt.spreadsheet.reader
import libt._
import java.util.Date
import libt.spreadsheet.writer.CellWriter

trait FeatureReader[A] {
  def read(reader: CellReader): Value[A]
  def write(writer : CellWriter, value: Value[A]) : Unit
}

case object NumericReader extends FeatureReader[BigDecimal] {
  def read(reader: CellReader) = reader.numeric
  def write(writer : CellWriter, value: Value[BigDecimal]) = writer.numeric(value)
}

case object StringReader extends FeatureReader[String] {
  def read(reader: CellReader) = reader.string
  def write(writer : CellWriter, value: Value[String]) = writer.string(value)
}

case object DateReader extends FeatureReader[Date] {
  def read(reader: CellReader) = reader.date
  def write(writer : CellWriter, value: Value[Date]) = writer.date(value)
}

case object XBoolReader extends FeatureReader[Boolean] {
  def read(reader: CellReader) = reader.xBoolean
  def write(writer : CellWriter, value: Value[Boolean]) = writer.xBoolean(value)
}

case class WithDefaultReader[A](
  baseFeatureReader: FeatureReader[A],
  defaultValue: A) extends FeatureReader[A] {
  def read(reader: CellReader) = baseFeatureReader.read(reader).orDefault(defaultValue)
  def write(writer: CellWriter, value: Value[A]) =
    if (value.contains(defaultValue))
      writer.skip(1)
    else
      baseFeatureReader.write(writer, value);
}
