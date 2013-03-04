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
import model.Company
import util.poi.Cells._
import model.CarriedInterest
import model.EquityCompanyValue
import scala.collection.immutable.Map
import model.Input
import model.AnualCashCompensation
object SpreadsheetLoader {

  def load(in: InputStream) :Seq[Executive] = {
    val wb = WorkbookFactory.create(in)
    wb.setMissingCellPolicy(Row.RETURN_BLANK_AS_NULL)
    val sheet: Sheet = wb.getSheetAt(1)

    sheet.rowIterator.drop(3).grouped(6).map(toExecutive).toSeq
  }

  class ColumnOrientedReader(rows: Seq[Row]) {
    val valueIterator = rows(0).iterator
    val metadataIterators = rows.drop(1).map(_.iterator.drop(1))
    
    def string = createInput(_.getStringCellValue)
    def boolean = createInput(_.getBooleanCellValue)
    def numeric = createInput(_.getNumericCellValue: BigDecimal)
    def skip(offset: Int) = for (_ <- 1 to offset) next
    
    private def next = blankToNone(valueIterator.next)
    private def nextMetadataStringValue(rowIndex : Int) = 
      blankToNone(metadataIterators(rowIndex).next).map(_.getStringCellValue)
      
    private def createInput[T](valueMapper: Cell => T)  =  {
      Input(next.map(valueMapper), 
        nextMetadataStringValue(0), 
        nextMetadataStringValue(1), 
        nextMetadataStringValue(2), 
        nextMetadataStringValue(3))
    }
    
  }

  def toExecutive(rows: Seq[Row]) = {
    val reader = new ColumnOrientedReader(rows)
    import reader._

    Executive(
      name = { skip(1); string }, 
      title = string, 
      shortTitle = string, 
      functionalMatch = string, 
      founder = string,
      cashCompensations = Seq(
          AnualCashCompensation(
    		  baseSalary = numeric,
    		  actualBonus = numeric,
    		  targetBonus = numeric,
    		  thresholdBonus = numeric,
    		  maxBonus = numeric
          )),
      equityCompanyValue = EquityCompanyValue(
        optionsValue = { skip(6); numeric },
        options = numeric,
        exPrice = numeric,
        bsPercentage = numeric,
        timeVest = numeric,
        shares = numeric,
        price = numeric,
        perf = numeric),
      carriedInterest = CarriedInterest(
        ownedShares = numeric,
        vestedOptions = numeric,
        unvestedOptions = numeric,
        tineVest = numeric,
        perfVest = numeric))
  }

}

		
		
//Cash Compensation ($000)	Current Year	Base Salary
//Cash Compensation ($000)	Current Year	Actual Bonus
//Cash Compensation ($000)	Current Year	Target Bonus
//Cash Compensation ($000)	Current Year	Threshold Bonus
//Cash Compensation ($000)	Current Year	Max Bonus
//Cash Compensation ($000)	New 8-K Data	Base Salary
//Cash Compensation ($000)	New 8-K Data	Target Bonus
//		
//Equity Comp Value ($000)	Current Year	Options Value
//Equity Comp Value ($000)	Current Year	Options
//Equity Comp Value ($000)	Current Year	Ex. Price
//Equity Comp Value ($000)	Current Year	BS%
//Equity Comp Value ($000)	Current Year	Time-Vest RS Value
//Equity Comp Value ($000)	Current Year	Shares
//Equity Comp Value ($000)	Current Year	Price
//Equity Comp Value ($000)	Current Year	Perf RS Value
//Equity Comp Value ($000)	Current Year	Shares
//Equity Comp Value ($000)	Current Year	Price
//Equity Comp Value ($000)	Current Year	Perf Cash
//		
