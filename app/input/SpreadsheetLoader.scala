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
import model.New8KData
object SpreadsheetLoader {

  def load(in: InputStream): Seq[Executive] = {
    val wb = WorkbookFactory.create(in)
    val sheet: Sheet = wb.getSheetAt(1)

    rows(sheet).drop(3).grouped(6).map(toExecutive).toSeq
  }

  class ColumnOrientedReader(rows: Seq[Row]) {
    private val cellIterators = rows.map(cells).map(_.iterator)

    def string = createInput(_.getStringCellValue)
    def boolean = createInput(_.getBooleanCellValue)
    def numeric = createInput(_.getNumericCellValue: BigDecimal)
    def skip(offset: Int) = for (_ <- 1 to offset) cellIterators.foreach(_.next)

    private def createInput[T](valueMapper: Cell => T) = {
      val nextCells = cellIterators.map(_.next).map(blankToNone)
      def nextStringValue(index: Int) = nextCells(index).map(_.getStringCellValue)
      Input(nextCells(0).map(valueMapper),
        nextStringValue(1),
        nextStringValue(2),
        nextStringValue(3),
        nextStringValue(4))
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
          maxBonus = numeric,
          new8KData = New8KData(
            baseSalary = numeric,
            targetBonus = numeric))),
      equityCompanyValue = EquityCompanyValue(
        optionsValue = numeric,
        options = numeric,
        exPrice = numeric,
        bsPercentage = numeric,
        timeVest = numeric,
        rsValue = numeric,
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
