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
import libt.spreadsheet.reader.RowOrientedReader
import libt.spreadsheet.util._
import libt.spreadsheet.reader.ColumnOrientedReader

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
      'executives -> Col(executives: _*))
  }

  private def toExecutive(rows: Seq[Row]) = {
    val reader = new ColumnOrientedReader(rows)
    import reader._

    //    repeatModel(times: Int, )

    def createOptionGrants(times: Int) = {
      val models =
        for (_ <- 1 to times)
          yield Model(
          'grantDate -> date,
          'expireDate -> date,
          'number -> numeric,
          'price -> numeric,
          'value -> numeric,
          'perf -> xBoolean,
          'type -> string)

      Col(models: _*)
    }

    def createTimeVestRS(times: Int) = {
      val models =
        for (_ <- 1 to times)
          yield Model(
          'grantDate -> date,
          'number -> numeric,
          'price -> numeric,
          'value -> numeric,
          'type -> string)

      Col(models: _*)
    }

    def createPerformanceVestRS(times: Int) = {
      val models =
        for (_ <- 1 to times)
          yield Model(
          'grantDate -> date,
          'targetNumber -> numeric,
          'grantDatePrice -> date,
          'targetValue -> numeric,
          'type -> string)

      Col(models: _*)
    }

    def createPerformanceCash(times: Int) = {
      val models =
        for (_ <- 1 to times)
          yield Model(
          'grantDate -> date,
          'targetValue -> numeric,
          'payout -> numeric)

      Col(models: _*)
    }

    Model(

      'firstName -> { skip(1); string },
      'lastName -> string,
      'title -> string,
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
        'retentionBonus -> numeric,
        'signOnBonus -> numeric,
        'targetBonus -> numeric,
        'thresholdBonus -> numeric,
        'maxBonus -> numeric,
        'nextFiscalYearData -> Model(
          'baseSalary -> numeric,
          'targetBonus -> numeric)),

      'optionGrants -> createOptionGrants(6),

      'timeVestRS -> createTimeVestRS(6),

      'performanceVestRS -> createPerformanceVestRS(3),

      'performanceCash -> createPerformanceCash(3),

      'carriedInterest -> Model(
        'ownedShares -> Model(
          'beneficialOwnership -> numeric,
          'options -> numeric,
          'unvestedRestrictedStock -> numeric,
          'disclaimBeneficialOwnership -> numeric,
          'heldByTrust -> numeric,
          'other -> string),
        'outstandingEquityAwards -> Model(
          'vestedOptions -> numeric,
          'unvestedOptions -> numeric,
          'timeVestRS -> numeric,
          'perfVestRS -> numeric)))
  }

}

