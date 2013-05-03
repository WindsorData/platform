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

object SpreadsheetWriter {

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
  def write(out: Workbook, companies: Seq[Model]): Unit = {
    val areas = Seq(ValueAreaLayout(Offset(0,0)), MetadataAreaLayout(Offset(0,0))).map(
      FlattedArea(
        PK(Path('ticker), Path('name), Path('disclosureFiscalYear)),
        PK(Path('lastName)),
        Path('executives),
        TCompanyFiscalYear,
        _,
        Seq(
          Feature('firstName),
          Feature('lastName),
          Feature('title),
          Feature('functionalMatches, 'primary),
          Feature('functionalMatches, 'secondary),
          Feature('functionalMatches, 'level),
          Feature('functionalMatches, 'scope),
          Feature('functionalMatches, 'bod),
          Feature('founder),
          Feature('transitionPeriod))))
    WorkbookMapping(areas).write(companies, out)
  }

  def loadTemplateInto(out: OutputStream) = {
    FileManager.load("docs/external/EmptyOutputTemplate.xls") {
      x => WorkbookFactory.create(x).write(out)
    }
  }
}