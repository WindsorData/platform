package libt.spreadsheet.reader
import libt._
import java.util.Date
import libt.spreadsheet.writer.CellWriter
import libt.spreadsheet.writer.op.WriteOp
import libt.spreadsheet.writer.op

trait FeatureReader[A] {
  def read(reader: CellReader): Value[A]
  def writeOp(value: Option[A]) : op.WriteOp
}

case object NumericReader extends FeatureReader[BigDecimal] {
  def read(reader: CellReader) = reader.numeric
  override def writeOp(value: Option[BigDecimal]) = op.Numeric(value)
}

case object StringReader extends FeatureReader[String] {
  def read(reader: CellReader) = reader.string
  override def writeOp(value: Option[String]) = op.String(value)
}

case object DateReader extends FeatureReader[Date] {
  def read(reader: CellReader) = reader.date
  override def writeOp(value: Option[Date]) = op.Date(value)
}

case object XBoolReader extends FeatureReader[Boolean] {
  def read(reader: CellReader) = reader.xBoolean
  override def writeOp(value: Option[Boolean]) = op.Boolean(value)
}

case class WithDefaultReader[A](
  baseFeatureReader: FeatureReader[A],
  defaultValue: A) extends FeatureReader[A] {
  def read(reader: CellReader) = baseFeatureReader.read(reader).orDefault(defaultValue)
  override def writeOp(value: Option[A]) =
    if (value.exists(_ == defaultValue))
      op.Skip
    else
      baseFeatureReader.writeOp(value)
}
