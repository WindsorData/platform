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

  def outputArea(layout: FlattedAreaLayout, outputMapping: Seq[Strip], flatteningPK: Path, flatteningPath: Path) =
    FlattedArea(
      PK(Path('ticker), Path('name), Path('disclosureFiscalYear)),
      PK(flatteningPK),
      flatteningPath,
      TCompanyFiscalYear,
      layout,
      outputMapping)

  def execDBArea =
    outputArea(
      ValueAreaLayout(Offset(6, 2)),
      execDbOutputMapping,
      Path('lastName),
      Path('executives))

  def stBonusPlanArea =
    outputArea(
      ValueAreaLayout(Offset(6, 2)),
      stBonusPlanOutputMapping,
      Path('functionalMatches, 'primary),
      Path('stBonusPlan))

  def executiveOwnershipArea =
    outputArea(
      ValueAreaLayout(Offset(6, 2)),
      executiveOwnershipMapping,
      Path('functionalMatches, 'primary),
      Path('guidelines))

  def usageAndSVTDataArea =
    outputArea(
      ValueAreaLayout(Offset(6, 2)),
      usageAndSVTDataMapping,
      Path('avgSharesOutstanding),
      Path('usageAndSVTData))

  def bsInputsArea =
    outputArea(
      ValueAreaLayout(Offset(6, 2)),
      bsInputsMapping,
      Path(),
      Path('bsInputs))

  def metadataArea = outputArea(MetadataAreaLayout(Offset(1, 0)), execDbOutputMapping.filter(_ match {
    case Gap => false
    case _ => true
  }), Path('lastName), Path('executives))

  def write(out: Workbook, companies: Seq[Model]): Unit = {
    WorkbookMapping(
      Seq(
        execDBArea, //ExecDB
        stBonusPlanArea,
        executiveOwnershipArea,
        usageAndSVTDataArea,
        bsInputsArea,
        metadataArea)).write(companies, out)
  }

  def loadTemplateInto(out: OutputStream) = {
    FileManager.load("docs/external/EmptyOutputTemplate.xls") {
      x => WorkbookFactory.create(x).write(out)
    }
  }

  def write(out: OutputStream, companies: Seq[Model]): Unit = {
    FileManager.load("docs/external/EmptyOutputTemplate.xls") {
      x =>
        {
          val wb = WorkbookFactory.create(x)
          write(wb, companies)
          wb.write(out)
        }
    }
  }
}