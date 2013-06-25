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

  def execDBArea(range: Int, yearOffset: Option[Int]) =
    outputArea(
      ValueAreaLayout(Offset(6, 2)),
      execDbOutputMapping,
      Path('lastName),
      Path('executives, *),
      ExecutivesWriteStrategy(range, yearOffset))

  def stBonusPlanArea =
    outputArea(
      ValueAreaLayout(Offset(6, 2)),
      stBonusPlanOutputMapping,
      Path('functionalMatches, 'primary),
      Path('stBonusPlan, *),
      LastYearWriteStrategy)

  def executiveOwnershipArea =
    outputArea(
      ValueAreaLayout(Offset(6, 2)),
      executiveOwnershipMapping,
      Path('functionalMatches, 'primary),
      Path('guidelines, *),
      LastYearWriteStrategy)

  def usageAndSVTDataArea =
    outputArea(
      ValueAreaLayout(Offset(6, 2)),
      usageAndSVTDataMapping,
      Path(),
      Path('usageAndSVTData, *),
      LastYearWriteStrategy)

  def bsInputsArea =
    outputArea(
      ValueAreaLayout(Offset(6, 2)),
      bsInputsMapping,
      Path(),
      Path('bsInputs, *),
      LastYearWriteStrategy)

  def dilutionArea =
    outputArea(
      ValueAreaLayout(Offset(6, 2)),
      dilutionMapping,
      Path(),
      Path('dilution, *),
      LastYearWriteStrategy)

  def grantTypesArea =
    outputArea(
      ValueAreaLayout(Offset(7, 2)),
      grantTypesMapping,
      Path(),
      Path('grantTypes, *),
      LastYearWriteStrategy)

  def metadataArea(range: Int) =
    outputArea(
      MetadataAreaLayout(Offset(1, 0)),
      execDbOutputMapping.filter(_ match {
        case Gap => false
        case _ => true
      }),
      Path('lastName),
      Path('executives, *),
      ExecutivesWriteStrategy(range, None))

  def write(out: Workbook, companies: Seq[Model], executivesRange: Int): Unit = {
    WorkbookMapping(
      Seq(
        execDBArea(executivesRange - 1, Some(0)), //ExecDB
        execDBArea(executivesRange - 2, Some(1)), //ExecDB -1 
        execDBArea(executivesRange - 3, Some(2)), //ExecDB -2
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

  /**
    * [[output.WriteStrategy]] that writes TCompanyFiscalYears for a given yearOffset
    * @param range
    * @param yearOffset the negative year offset (0 is last year, 1 is previous year, and so on)
    */
  case class ExecutivesWriteStrategy(/*TODO remove*/range: Int, yearOffset: Option[Int]) extends WriteStrategy {
    override def write(models: Seq[Model], area: FlattedArea, sheet: Sheet)  {
      def Desc[T: Ordering] = implicitly[Ordering[T]].reverse
      if (range >= 0) {
        val validModels = models
          .groupBy(model => model('ticker).asValue[String].value.get)
          .map { case (ticker, ms) => (ticker, ms.sortBy(_('disclosureFiscalYear).asValue[Int].value.get)(Desc)) }
        yearOffset match {
        	case Some(p) => area.layout.write(validModels.values.toSeq.map(_(p)), sheet, area)
        	case None => area.layout.write(validModels.values.toSeq.flatten, sheet, area) 
        }		
      }
    }
  }

  /**
    * [[output.WriteStrategy]] that writes only TCompanyFiscalYears for the last year
    */
  object LastYearWriteStrategy extends WriteStrategy {
    override def write(models: Seq[Model], area: FlattedArea, sheet: Sheet) {
      val lastYear = models.map(_('disclosureFiscalYear).asValue[Int].value.get).max
      area.layout.write(
        models.filter(_('disclosureFiscalYear).asValue[Int].value.get == lastYear),
        sheet,
        area)
    }
  }
}