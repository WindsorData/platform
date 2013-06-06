package output
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.usermodel.Sheet
import java.io.OutputStream
import org.apache.poi.ss.usermodel.Workbook
import model._
import util.FileManager
import libt.util._
import libt._
import libt.spreadsheet._
import libt.spreadsheet.util._
import libt.spreadsheet.reader._
import libt.reduction._
import output.mapping._

object SpreadsheetWriter {

  def outputArea(
      layout: FlattedAreaLayout, 
      outputMapping: Seq[Strip], 
      flatteningPK: Path, 
      flatteningPath: Path,
      writeStrategy: WriteStrategy) =
    FlattedArea(
      PK(Path('ticker), Path('name), Path('disclosureFiscalYear)),
      PK(flatteningPK),
      flatteningPath,
      TCompanyFiscalYear,
      layout,
      outputMapping,
      writeStrategy)

  def execDBArea(range: Int) =
    outputArea(
      ValueAreaLayout(Offset(6, 2)),
      execDbOutputMapping,
      Path('lastName),
      Path('executives, *),
      ExecutivesStrategy(range))

  def stBonusPlanArea =
    outputArea(
      ValueAreaLayout(Offset(6, 2)),
      stBonusPlanOutputMapping,
      Path('functionalMatches, 'primary),
      Path('stBonusPlan, *),
      LastYearStrategy)

  def executiveOwnershipArea =
    outputArea(
      ValueAreaLayout(Offset(6, 2)),
      executiveOwnershipMapping,
      Path('functionalMatches, 'primary),
      Path('guidelines, *),
      LastYearStrategy)

  def usageAndSVTDataArea =
    outputArea(
      ValueAreaLayout(Offset(6, 2)),
      usageAndSVTDataMapping,
      Path(),
      Path('usageAndSVTData, *),
      LastYearStrategy)

  def bsInputsArea =
    outputArea(
      ValueAreaLayout(Offset(6, 2)),
      bsInputsMapping,
      Path(),
      Path('bsInputs, *),
      LastYearStrategy)

  def dilutionArea =
    outputArea(
      ValueAreaLayout(Offset(6, 2)),
      dilutionMapping,
      Path(),
      Path('dilution, *),
      LastYearStrategy)

  def grantTypesArea =
    outputArea(
      ValueAreaLayout(Offset(7, 2)),
      grantTypesMapping,
      Path(),
      Path('grantTypes, *),
      LastYearStrategy)

  def metadataArea(range: Int) =
    outputArea(
      MetadataAreaLayout(Offset(1, 0)),
      execDbOutputMapping.filter(_ match {
        case Gap => false
        case _ => true
      }),
      Path('lastName),
      Path('executives, *),
      ExecutivesStrategy(range))

  def write(out: Workbook, companies: Seq[Model], executivesRange: Int): Unit = {
    WorkbookMapping(
      Seq(
        execDBArea(executivesRange),
        stBonusPlanArea,
        executiveOwnershipArea,
        usageAndSVTDataArea,
        bsInputsArea,
        dilutionArea,
        grantTypesArea,
        metadataArea(executivesRange))).write(companies, out)
  }

  def loadTemplateInto(out: OutputStream) =
    FileManager.loadResource("EmptyOutputTemplate.xls") {
      x => WorkbookFactory.create(x).write(out)
    }

  def write(out: OutputStream, companies: Seq[Model], executivesRange: Int): Unit = {
    FileManager.loadResource("EmptyOutputTemplate.xls") {
      x =>
        {
          val wb = WorkbookFactory.create(x)
          write(wb, companies, executivesRange)
          wb.write(out)
        }
    }
  }

  case class ExecutivesStrategy(range: Int) extends WriteStrategy {
    override def write(models: Seq[Model], area: FlattedArea, sheet: Sheet): Unit = {
      def Desc[T: Ordering] = implicitly[Ordering[T]].reverse
        val validModels =
          models.groupBy(model => model('ticker).asValue[String].value.get)
            .map {
              case (ticker, ms) =>
                (ticker,
                  ms
                  .sortBy(_.apply('disclosureFiscalYear).asValue[Int].value.get)(Desc)
                  .take(range))
            }

        area.layout.write(validModels.values.toSeq.flatten, sheet, area)
    }
  }

  object LastYearStrategy extends WriteStrategy {
    override def write(models: Seq[Model], area: FlattedArea, sheet: Sheet): Unit = {
      def Desc[T: Ordering] = implicitly[Ordering[T]].reverse
      val lastYear =
        models
          .map(_.apply('disclosureFiscalYear).asValue[Int].value.get)
          .sortBy(v => v)(Desc)
          .head

      area.layout.write(
        models.filter(_.apply('disclosureFiscalYear).asValue[Int].value.get == lastYear),
        sheet,
        area)
    }
  }
}