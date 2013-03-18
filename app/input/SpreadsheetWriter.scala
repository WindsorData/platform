package input

import java.io.InputStream
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileOutputStream
import java.io.OutputStream
import model.Executive
import util.poi.Cells._
import org.apache.poi.ss.usermodel.Workbook
import util.FileManager
import org.apache.poi.ss.usermodel.Cell
import model.Input
import model._

object SpreadsheetWriter {

  def write(out: OutputStream, company: CompanyFiscalYear) = {
    FileManager.load("docs/external/EmptyOutputTemplate.xls") {
      x => {
        val wb = WorkbookFactory.create(x)
        writeCompany(wb, company)
        writeExecutives(wb, company)
        wb.write(out)
      }
    }
  }

  def writeCompany(wb: Workbook, company: CompanyFiscalYear) = {
    val companySheet = wb.getSheet("DOC_SRC")
    
    val rowsIter = rows(companySheet).drop(2).iterator
    
    rowsIter.next.getCell(2).setCellValue(company.ticker.value.get)
    rowsIter.next.getCell(2).setCellValue(company.name.value.get)
    rowsIter.next.getCell(2).setCellValue(company.disclosureFiscalYear.value.get)
  }
  
  def writeExecutives(wb: Workbook, company: CompanyFiscalYear) = {
    val executiveSheet = wb.getSheet("Executives")

    val cellIterators = rows(executiveSheet).drop(3).map(cells).map(_.iterator)
    //Skips first column
    cellIterators.map(_.next)

    //TODO: 
    // Put other input fields as comments for the value
    def getInputValue[T](toSomeValue: Executive => Input[T]) = company.executives.map(toSomeValue).map(_.value).toList

    def writeValue[T](names: Traversable[Option[T]], cells: Seq[Cell]): Unit = {
      names match {
        case Some(value) :: xs => {
          //TODO: don't convert every value toString
          cells.head.setCellValue(value.toString)
          writeValue(xs, cells.drop(2))
        }
        case None :: xs => writeValue(xs, cells.drop(2))
        case Nil => Unit
      }
    }

    def writeCellWithExecutiveValue[T](executive2Input: Executive => Input[T]) =
      writeValue(getInputValue(executive2Input), cellIterators.map(_.next))

    writeCellWithExecutiveValue(_.name)
    writeCellWithExecutiveValue(_.title)
    writeCellWithExecutiveValue(_.shortTitle)
    writeCellWithExecutiveValue(_.functionalMatch)
    writeCellWithExecutiveValue(_.functionalMatch1)
    writeCellWithExecutiveValue(_.functionalMatch2)
    writeCellWithExecutiveValue(_.founder)

    //TODO: write all the 3 years, we are just creating the first one for now.
    writeCellWithExecutiveValue(_.cashCompensations.baseSalary)
    writeCellWithExecutiveValue(_.cashCompensations.actualBonus)
    writeCellWithExecutiveValue(_.cashCompensations.targetBonus)
    writeCellWithExecutiveValue(_.cashCompensations.thresholdBonus)
    writeCellWithExecutiveValue(_.cashCompensations.maxBonus)
    writeCellWithExecutiveValue(_.cashCompensations.new8KData.baseSalary)
    writeCellWithExecutiveValue(_.cashCompensations.new8KData.targetBonus)

    writeCellWithExecutiveValue(_.equityCompanyValue.optionsValue)
    writeCellWithExecutiveValue(_.equityCompanyValue.options)
    writeCellWithExecutiveValue(_.equityCompanyValue.exPrice)
    writeCellWithExecutiveValue(_.equityCompanyValue.bsPercentage)
    writeCellWithExecutiveValue(_.equityCompanyValue.timeVestRsValue)
    writeCellWithExecutiveValue(_.equityCompanyValue.shares)
    writeCellWithExecutiveValue(_.equityCompanyValue.price)
    writeCellWithExecutiveValue(_.equityCompanyValue.perfRSValue)
    writeCellWithExecutiveValue(_.equityCompanyValue.shares2)
    writeCellWithExecutiveValue(_.equityCompanyValue.price2)
    writeCellWithExecutiveValue(_.equityCompanyValue.perfCash)

    writeCellWithExecutiveValue(_.carriedInterest.ownedShares)
    writeCellWithExecutiveValue(_.carriedInterest.vestedOptions)
    writeCellWithExecutiveValue(_.carriedInterest.unvestedOptions)
    writeCellWithExecutiveValue(_.carriedInterest.tineVest)
    writeCellWithExecutiveValue(_.carriedInterest.perfVest)
  }

  def loadTemplateInto(out: OutputStream) = {
    FileManager.load("docs/external/EmptyOutputTemplate.xls") {
      x => WorkbookFactory.create(x).write(out)
    }
  }
}