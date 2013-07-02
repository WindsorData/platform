package libt.spreadsheet
import libt._
import java.util.Date
import libt.spreadsheet.writer.op
import libt.spreadsheet.reader.CellReader

/**
 * Mapping from {{TValue}}'s to read and write
 * spreadsheet operations
 */
trait TMapping[A] {
  def read(reader: CellReader): Value[A]
  def writeOp(value: Option[A]): op.WriteOp
}

object TMapping {
  def apply[A](tValue: TValue[A]): TMapping[A] = tValue match {
    case TString => TStringMapping
    case TAny => TAnyMapping
    case TEnum(valueType, _) => this(valueType)
    case TNumber => TNumberMapping
    case TInt => TIntMapping
    case TBool => ???
    case TXBool => TXBoolMapping
    case TDate => TDateMapping
  }
}

case object TNumberMapping extends TMapping[BigDecimal] {
  def read(reader: CellReader) = reader.numeric
  override def writeOp(value: Option[BigDecimal]) = op.Numeric(value)
}

case object TIntMapping extends TMapping[Int] {
  def read(reader: CellReader) = reader.int
  override def writeOp(value: Option[Int]) = op.Int(value)
}

case object TStringMapping extends TMapping[String] {
  def read(reader: CellReader) = reader.string
  override def writeOp(value: Option[String]) = op.String(value)
}

case object TAnyMapping extends TMapping[String] {
  override def read(reader: CellReader) = reader.any
  override def writeOp(value: Option[String]) = op.String(value)
}

case object TDateMapping extends TMapping[Date] {
  def read(reader: CellReader) = reader.date
  override def writeOp(value: Option[Date]) = op.Date(value)
}

case object TXBoolMapping extends TMapping[Boolean] {
  def read(reader: CellReader) = reader.xBoolean
  override def writeOp(value: Option[Boolean]) = op.XBoolean(value)
}

case class TWithDefaultMapping[A](
  baseFeatureReader: TMapping[A],
  defaultValue: A) extends TMapping[A] {
  def read(reader: CellReader) = baseFeatureReader.read(reader).orDefault(defaultValue)
  override def writeOp(value: Option[A]) =
    if (value.exists(_ == defaultValue))
      op.Skip
    else
      baseFeatureReader.writeOp(value)
}


