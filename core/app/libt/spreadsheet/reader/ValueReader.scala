package libt.spreadsheet.reader

import libt.Value

trait ValueReader {
  /** creates a functor with the read value */
  def read[T](value: Option[T], nextValue: Int => Option[String]): Value[T]

  /** the size of the read block in term of number of cells */
  def blockSize: Int
}

case class WithSeparator(valueReader:ValueReader) extends ValueReader {
  override def read[T](value: Option[T], nextValue: Int => Option[String]) =
    valueReader.read(value, nextValue)
  override def blockSize = valueReader.blockSize + 1
}

object WithMetadataValueReader extends ValueReader {
  override def read[T](value: Option[T], nextValue: Int => Option[String]) =
    Value(value, nextValue(1), nextValue(2), nextValue(3), nextValue(4))

  override def blockSize = 5
}

object WithPartialMetadataValueReader extends ValueReader {
  override def read[T](value: Option[T], nextValue: Int => Option[String]) =
    Value(value, None, None, nextValue(1), nextValue(2))

  override def blockSize = 3
}

object RawValueReader extends ValueReader {
  override def read[T](value: Option[T], nextValue: Int => Option[String]) =
    Value(value, None, None, None, None)

  override def blockSize = 1
}