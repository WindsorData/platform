package libt.spreadsheet

import org.apache.poi.ss.usermodel.Sheet
import libt.spreadsheet.util._
import libt.spreadsheet.writer._
import libt.spreadsheet.writer.op._
import libt.spreadsheet.writer.op
import libt.spreadsheet.reader._
import libt.reduction.Reduction
import libt.builder._
import libt._

/**
 * The declaration of the content of a column, that may either important - Feature  - or unimportant - Gap
 */
sealed trait Strip {
  /**
   * *Reads from a CellReader, using the given TElement as schema, and collects
   * the results into the given ModelBuilder
   */
  def read(reader: CellReader, schema: TElement, modelBuilder: ModelBuilder)

  /***Writes with a CellWriter, using the given TElement as schema*/
  def writeOps(schema: TElement, model: Element) : WriteOps
}

trait WriteOps extends LibtSizes {
  def value : WriteOp
  def titles : List[WriteOp] = List.fill(TitlesSize)(Skip)
  def metadata : List[WriteOp] = List.fill(MetadataSize)(Skip)
}

/**A column whose value is important and should be read or written from and to Model's Value*/
case class Feature(path: Path) extends Strip {
  def read(reader: CellReader, schema: TElement, modelBuilder: ModelBuilder) =
    modelBuilder += (path -> readValue(schema, reader))

  override def writeOps(schema: TElement, model: Element) = new WriteOps {
    val mapping = TMapping[AnyRef](schema(path).asInstanceOf[TValue[AnyRef]])
    val element = model(path).asInstanceOf[Value[AnyRef]]
    override def value = mapping.writeOp(element.value)
    override def metadata = element.metadataSeq.map(op.String(_)).toList
    override def titles = path.titles match {
      case Nil => op.Skip :: op.Skip :: Nil
      case it => op.String(Some(it.init.mkString(" - "))) :: op.String(Some(it.last)) :: Nil
    }
  }
      
  private def readValue(schema: TElement, reader: CellReader) =
    TMapping(schema(path).asValue).read(reader)

}

object Feature {
  def apply(pathParts: PathPart*): Feature = Feature(pathParts.toList)
}

case class Tag(tag: String, column:Strip) extends Strip {
  override def read(reader: CellReader, schema: TElement, modelBuilder: ModelBuilder) = 
    column.read(reader, schema, modelBuilder)
  override def writeOps(schema: TElement, model: Element) = new WriteOps {
    private val writeOps = column.writeOps(schema, model)
    override def value =  op.String(Some(tag))
    override def metadata = writeOps.metadata
    override def titles = writeOps.titles
  }
}

/**
 * A strip that displays calculations. 
 * Supports writing only
 * */
case class Calc(reduction: Reduction) extends Strip {
  override def read(reader: CellReader, schema: TElement, modelBuilder: ModelBuilder) = ???
  override def writeOps(schema: TElement, model: Element) = new WriteOps {
    def value = Numeric(Some(reduction.reduce(model)))
  }
}

/**A column whose value is not important and should be skipped*/
case object Gap extends Strip {

  override def read(reader: CellReader, schema: TElement, modelBuilder: ModelBuilder) =
    reader.skip(1)

  override def writeOps(schema: TElement, model: Element) = new WriteOps {
    override def value = Skip
  }
}