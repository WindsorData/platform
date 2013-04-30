package libt.spreadsheet

import libt._
import builder._
import reader._
import org.apache.poi.ss.usermodel.Sheet
import libt.spreadsheet.util._
import libt.spreadsheet.writer.CellWriter

/**
 * The declaration of the content of a column, that may either important - Feature  - or unimportant - Gap
 */
sealed trait Column { //TODO rename
  /**
   * *Reads from a CellReader, using the given TElement as schema, and collects
   * the results into the given ModelBuilder
   */
  def read(reader: CellReader, schema: TElement, modelBuilder: ModelBuilder)

  /***Writes with a CellWriter, using the given TElement as schema*/
  def write(writer: CellWriter, schema: TElement, model: Model)
  def title: Option[String]
}

/**A column whose value is important and should be read or written from and to Model's Value*/
case class Feature(path: Path) extends Column {
  def read(reader: CellReader, schema: TElement, modelBuilder: ModelBuilder) =
    modelBuilder += (path -> readValue(schema, reader))

  def write(writer: CellWriter, schema: TElement, model: Model) = {
    def foo[A] = featureReader(schema(path).asInstanceOf[TValue[A]]).
    			 write(writer, model(path).asInstanceOf[Value[A]])
    foo
  }

  override def title =
    Some(
      path.map {
        case Route(s) => s.name.foldLeft("")( (acc, ch) => (if (ch.isUpper) " " else "") + ch + acc.capitalize)
        case Index(i) => i.toString
      }.mkString(" - "))

  private def readValue(schema: TElement, reader: CellReader) =
    featureReader(schema(path).asValue).read(reader)
  private def featureReader[A](tValue: TValue[A]): FeatureReader[A] = tValue match {
    case TString => StringReader
    case _: TEnum => StringReader
    case TNumber => NumericReader
    case TWithDefault(baseReader, default) => WithDefaultReader(featureReader(baseReader), default)
    case TInt => ???
    case TBool => ???
    case TXBool => XBoolReader
    case TDate => DateReader
  }
}

object Feature {
  def apply(pathParts: PathPart*): Feature = Feature(pathParts.toList)
}

/**A column whose value is not important and should be skipped*/
case object Gap extends Column {

  override def read(reader: CellReader, schema: TElement, modelBuilder: ModelBuilder) =
    reader.skip(1)

  override def write(writer: CellWriter, schema: TElement, model: Model) =
    writer.skip(1)

  override def title = None
}

abstract class Calculation extends Column {
  def read(reader: CellReader, schema: TElement, modelBuilder: ModelBuilder) = ???
  override def write(writer: CellWriter, schema: TElement, model: Model) = ???
  override def title = Some("Calculated")
}
case class Sum(path: Path) extends Calculation
case class Averge(path: Path) extends Calculation
case class Custom(path: Path, calculation: Any) extends Calculation
