package input

import java.io.InputStream
import scala.collection.JavaConversions._
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import model.CarriedInterest
import model.Executive
import util.poi.Cells._
import model.CarriedInterest
import model.EquityCompanyValue
import scala.collection.immutable.Map
import model.Input
import model.AnualCashCompensation
import model.New8KData
import model.CompanyFiscalYear
import java.util.GregorianCalendar
import org.joda.time.DateTime
import model.SimpleInput
import play.Logger
import util.poi.Cells

object SpreadsheetLoader {
  
  def load(in: InputStream): Seq[CompanyFiscalYear] = {
    val wb = WorkbookFactory.create(in)
    /**Answers the seq of executives given a fiscal year offest*/
    def executivesByFiscalYear(fiscalYearOffest: Int) =
      rows(wb.getSheetAt(fiscalYearOffest)).drop(3).grouped(6).map(toExecutive).toSeq
      
    def dateCellToYear(r: Seq[Row]) = {
      val dateCell = r.get(0).getCell(2)
      try{        
    	Some(new DateTime(blankToNone(dateCell).get.getDateCellValue()).getYear())
      }
      catch{
        case e: NoSuchElementException => throw new NoSuchElementException(noFiscalYearErrorMessage(dateCell))
        case e: RuntimeException => throw new IllegalStateException(invalidCellTypeErrorMessage(e.getMessage(), dateCell))
      }      
    }

    val companiesSheet = wb.getSheetAt(0)

    val execDbYear = dateCellToYear(rows(companiesSheet).drop(8))
    val execDbYearMinusOne = dateCellToYear(rows(companiesSheet).drop(25))
    val execDbYearMinusTwo = dateCellToYear(rows(companiesSheet).drop(40))

    val years = Seq(execDbYear, execDbYearMinusOne, execDbYearMinusTwo).iterator

    for { fiscalYearOffset <- 2 to wb.getNumberOfSheets() - 1 }
      yield toCompany(executivesByFiscalYear(fiscalYearOffset), rows(companiesSheet).drop(1), years.next)
  }

  def toCompany(executives: Seq[Executive], rows: Seq[Row], fiscalYearOption: Option[Int]) = {
    val reader = new RowOrientedReader(rows)
    import reader._

    CompanyFiscalYear(
      ticker = { skip(1); string },
      name = string,
      disclosureFiscalYear = SimpleInput(fiscalYearOption, None, None),
      executives = executives)
  }

  def toExecutive(rows: Seq[Row]) = {
    val reader = new ColumnOrientedReader(rows)
    import reader._

    Executive(
      name = { skip(1); string },
      title = string,
      shortTitle = string,
      functionalMatches = Seq(string, string, string),
      founder = string,
      cashCompensations = AnualCashCompensation(
        baseSalary = numeric,
        actualBonus = numeric,
        targetBonus = numeric,
        thresholdBonus = numeric,
        maxBonus = numeric,
        new8KData = New8KData(
          baseSalary = numeric,
          targetBonus = numeric)),
      equityCompanyValue = EquityCompanyValue(
        optionsValue = numeric,
        options = numeric,
        exPrice = numeric,
        bsPercentage = numeric,
        timeVestRsValue = numeric,
        shares = numeric,
        price = numeric,
        perfRSValue = numeric,
        shares2 = numeric,
        price2 = numeric,
        perfCash = numeric),
      carriedInterest = CarriedInterest(
        ownedShares = numeric,
        vestedOptions = numeric,
        unvestedOptions = numeric,
        tineVest = numeric,
        perfVest = numeric))
  }

}

