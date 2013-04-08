package input

import java.io.InputStream
import scala.collection.JavaConversions._
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import scala.collection.immutable.Map
import java.util.GregorianCalendar
import org.joda.time.DateTime
import play.Logger
import libt._
import libt.export.spreadsheet.RowOrientedReader
import libt.export.spreadsheet.util._
import libt.export.spreadsheet.ColumnOrientedReader

object SpreadsheetReader {

  val ROW_INDEX_FISCAL_YEAR = 27
  val ROW_INDEX_FISCAL_YEAR_MINUS_ONE = 42
  val ROW_INDEX_FISCAL_YEAR_MINUS_TWO = 57

  def read(in: InputStream): Seq[Model] = read(WorkbookFactory.create(in))

  def read(wb: Workbook): Seq[Model] = {
    /**Answers the seq of executives given a fiscal year offest*/
    def executivesByFiscalYear(fiscalYearOffest: Int) =
      wb.getSheetAt(fiscalYearOffest).rows.drop(3).grouped(6).map(toExecutive).toSeq

    def dateCellToYear(r: Seq[Row]) = {
      val dateCell = r.get(0).getCell(2)
      try {
        Some(new DateTime(blankToNone(_.getDateCellValue)(dateCell).get).getYear())
      } catch {
        case e: NoSuchElementException => throw new NoSuchElementException(noFiscalYearErrorMessage(dateCell))
        case e: RuntimeException => throw new IllegalStateException(invalidCellTypeErrorMessage(e.getMessage(), dateCell))
      }
    }

    val companiesSheet = wb.getSheetAt(0)

    val years = Seq(
      ROW_INDEX_FISCAL_YEAR,
      ROW_INDEX_FISCAL_YEAR_MINUS_ONE,
      ROW_INDEX_FISCAL_YEAR_MINUS_TWO)
      .map(it => dateCellToYear(companiesSheet.rows.drop(it)))
      .iterator

    for { fiscalYearOffset <- 2 to wb.getNumberOfSheets() - 1 }
      yield toCompany(executivesByFiscalYear(fiscalYearOffset), companiesSheet.rows.drop(1), years.next)
  }

  private def invalidCellTypeErrorMessage(baseMessage: String, cell: Cell) =
    baseMessage +
      " on Sheet: " + cell.getSheet().getSheetName +
      " -> Column: " + { cell.getColumnIndex + 1 } +
      ", Row: " + { cell.getRowIndex + 1 }

  private def noFiscalYearErrorMessage(cell: Cell) = 
    "No Fiscal Year provided at Sheet " +
      cell.getSheet.getSheetName +
      " Column: " + cell.getColumnIndex +
      " Row: " + cell.getRowIndex

  private def toCompany(executives: Seq[Model], rows: Seq[Row], fiscalYearOption: Option[Int]) = {
    val reader = new RowOrientedReader(rows)
    import reader._

    Model(
      'ticker -> { skip(1); string },
      'name -> string,
      'disclosureFiscalYear -> Value(fiscalYearOption, None, None),
      'originalCurrency -> { skip(1); string },
      'currencyConversionDate -> date,
      'executives -> Col(executives: _*))
  }

  private def toExecutive(rows: Seq[Row]) = {
    val reader = new ColumnOrientedReader(rows)
    import reader._

    Model(
      'name -> { skip(1); string },
      'title -> string,
      'shortTitle -> string,
      'functionalMatches -> Model(
          'primary -> string,
          'secondary -> string,
          'level -> string,
          'scope -> string,
          'bod -> string),
      'founder -> string,
      'transitionPeriod -> string,
      'cashCompensations -> Model(
        'baseSalary -> numeric,
        'actualBonus -> numeric,
        'targetBonus -> numeric,
        'thresholdBonus -> numeric,
        'maxBonus -> numeric,
        'new8KData -> Model(
          'baseSalary -> numeric,
          'targetBonus -> numeric)),
      'equityCompanyValue -> Model(
        'optionsValue -> numeric,
        'options -> numeric,
        'exPrice -> numeric,
        'bsPercentage -> numeric,
        'timeVestRsValue -> numeric,
        'shares -> numeric,
        'price -> numeric,
        'perfRSValue -> numeric,
        'shares2 -> numeric,
        'price2 -> numeric,
        'perfCash -> numeric),
      'carriedInterest -> Model(
        'ownedShares -> numeric,
        'vestedOptions -> numeric,
        'unvestedOptions -> numeric,
        'tineVest -> numeric,
        'perfVest -> numeric))
  }

}

