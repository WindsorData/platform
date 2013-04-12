package input

import java.io.InputStream
import scala.collection.JavaConversions._
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import util.poi.Cells._
import scala.collection.immutable.Map
import java.util.GregorianCalendar
import org.joda.time.DateTime
import play.Logger
import util.poi.Cells
import libt.Model
import libt.Value
import libt.Col

object SpreadsheetLoader {

  val ROW_INDEX_FISCAL_YEAR = 27
  val ROW_INDEX_FISCAL_YEAR_MINUS_ONE = 42
  val ROW_INDEX_FISCAL_YEAR_MINUS_TWO = 57

  def load(in: InputStream): Seq[Model] = {
    val wb = WorkbookFactory.create(in)
    /**Answers the seq of executives given a fiscal year offest*/
    def executivesByFiscalYear(fiscalYearOffest: Int) =
      rows(wb.getSheetAt(fiscalYearOffest)).drop(3).grouped(6).map(toExecutive).toSeq

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

    val execDbYear = dateCellToYear(rows(companiesSheet).drop(ROW_INDEX_FISCAL_YEAR))
    val execDbYearMinusOne = dateCellToYear(rows(companiesSheet).drop(ROW_INDEX_FISCAL_YEAR_MINUS_ONE))
    val execDbYearMinusTwo = dateCellToYear(rows(companiesSheet).drop(ROW_INDEX_FISCAL_YEAR_MINUS_TWO))

    val years = Seq(execDbYear, execDbYearMinusOne, execDbYearMinusTwo).iterator

    for { fiscalYearOffset <- 2 to wb.getNumberOfSheets() - 1 }
      yield toCompany(executivesByFiscalYear(fiscalYearOffset), rows(companiesSheet).drop(1), years.next)
  }

  def toCompany(executives: Seq[Model], rows: Seq[Row], fiscalYearOption: Option[Int]) = {
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

  def toExecutive(rows: Seq[Row]) = {
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

