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
 * The declaration of the content of a column, that may be important - Feature  -, unimportant - Gap,
 * calculated - Calc - or a tag over an existing strip - Tag - CheckStrip
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
    val mapping = TMapping[AnyRef](schema(path).asValue)
    val element = model(path).asValue
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
    override def value =  writeOps.value
    override def metadata = writeOps.metadata
    override def titles = List(Skip, op.String(Some(tag)))
  }
}

/**
 * A strip that displays calculations. 
 * Supports writing only
 * */
case class Calc(reduction: Reduction) extends Strip {
  override def read(reader: CellReader, schema: TElement, modelBuilder: ModelBuilder) = ???
  override def writeOps(schema: TElement, model: Element) = new WriteOps {
    override def value = Numeric(Some(reduction.reduce(model)))
    override def metadata = List(op.String(Some("Calculated")), Skip, Skip, Skip)
  }
}

trait CheckStrip extends Strip {
  override def read(reader: CellReader, schema: TElement, modelBuilder: ModelBuilder) = ???
  override def writeOps(schema: TElement, model: Element) = new WriteOps {
    override def value = checkColumn(schema, model)
  }
  def checkColumn(schema: TElement, model: Element) : WriteOp
}

/**
 * A strip that checks a value if it
 * exists on the valid ones inside an specific TEnum
 * Supports writing only
 */
case class EnumCheck(path: Path, check: String) extends CheckStrip {
  override def checkColumn(schema: TElement, model: Element) = {
    val selectedValues = model.applySeq(path).flatMap(_.asValue[String].value)
    if (selectedValues.contains(check))
      String(Some("X"))
    else
      Skip
  }
}

/**
 * A strip that write an specific value if its corresponding value to check
 * exists on the valid ones inside an specific TEnum
 * Supports writing only
 * 
 * @param basePath the path that points to the collection of models
 * @param checkPath the path relative to base path whose value will be checked against the check string
 * @param writePath the path of the value that will be written when the check path value matches the check string
 */
case class ComplexEnumCheck(
    basePath: Path,
    checkPath: Path,
    writePath: Path,
    check: String) extends CheckStrip {
  override def checkColumn(schema: TElement, model: Element) = {
    implicit def models2RichModels(models: Seq[Model]) = new {
      def modelToWrite = 
        models.find(_.apply(checkPath).asValue[String].value.exists(_ == check))
    }
    
    val selectedModels = model.applySeq(basePath).map(_.asModel)
    val mapping = TMapping(schema(basePath ++ writePath).asValue)
    selectedModels.modelToWrite match {
      case Some(m) => mapping.writeOp(m(writePath).asValue.value)
      case None => Skip
    } 
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