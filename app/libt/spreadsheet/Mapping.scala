package libt.spreadsheet

import libt._
import builder._
import reader._
import org.apache.poi.ss.usermodel.Sheet
import libt.spreadsheet.util._
import libt.spreadsheet.writer.CellWriter
import libt.spreadsheet.writer.op
import libt.spreadsheet.writer.op._

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
  def writeOps(schema: TElement, model: Model) : WriteOps
}

trait WriteOps {
  def value : WriteOp
  def titles : List[WriteOp]
  def metadata : List[WriteOp]
}

/**A column whose value is important and should be read or written from and to Model's Value*/
case class Feature(path: Path) extends Column {
  def read(reader: CellReader, schema: TElement, modelBuilder: ModelBuilder) =
    modelBuilder += (path -> readValue(schema, reader))

  def writeOps(schema: TElement, model: Model) = new WriteOps {
    val reader = featureReader[AnyRef](schema(path).asInstanceOf[TValue[AnyRef]])
    val element = model(path).asInstanceOf[Value[AnyRef]]
    def value = reader.writeOp(element.value)
    def metadata = element.metadataSeq.map(op.String(_)).toList
    def titles = path.titles match {
      case Nil => op.Skip :: op.Skip :: Nil
      case it => op.String(Some(it.init.mkString(" - "))) :: op.String(Some(it.last)) :: Nil
    }
  }
      
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

  override def writeOps(schema: TElement, model: Model) = new WriteOps {
    def value = Skip
    def titles = Skip :: Skip :: Nil
    def metadata = Skip :: Skip :: Skip :: Skip :: Nil
  }
}