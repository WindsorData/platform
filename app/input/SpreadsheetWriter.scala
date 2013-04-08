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
import org.apache.poi.ss.usermodel.Comment
import org.apache.poi.xssf.usermodel.XSSFComment
import org.apache.poi.hssf.usermodel.HSSFPatriarch
import org.apache.poi.hssf.usermodel.HSSFCell
import org.apache.poi.hssf.usermodel.HSSFComment
import org.apache.poi.hssf.usermodel.HSSFClientAnchor
import org.apache.poi.hssf.usermodel.HSSFRichTextString
import java.util.Date

trait Writer {

  def defineValidSheetCells(x: Int, y: Int, sheet: Sheet) = {
    for {
      n <- 0 to (x - 1)
      m <- 0 to (y - 1)
    } sheet.createRow(m).createCell(n).setAsActiveCell()
  }
}

class DataWriter(wb: Workbook, company: CompanyFiscalYear) extends Writer {
  val sheet = {val s = wb.getSheet("ExecDB"); defineValidSheetCells(50, 50, s); s} 
  val metaDataWriter = new MetaDataWriter(wb)
  val cellIterators = rows(sheet).map(cells).map(_.iterator)

  def writeData[T](value: T, cell: Cell): Unit = {
    value match {
      case v: String => cell.setCellValue(v)
      case v: BigDecimal => cell.setCellValue(v.toDouble)
      case v: Boolean => cell.setCellValue(v)
      case v: Date => cell.setCellValue(v)
    }
  }

  def getInputValue[T](toSomeValue: Executive => Input[T]) = company.executives.map(toSomeValue).toList

  def writeInputValue[T](names: Traversable[Input[T]], cells: Seq[Cell]): Unit = {
    names match {
      case Input(Some(value), _, _, _, _) :: xs => {
        writeData(value, cells.head)
        //writeMetaData
        writeInputValue(xs, cells.tail)
      }
      case Input(None, _, _, _, _) :: xs => writeInputValue(xs, cells.tail)
      case Nil => Unit
    }
  }

  def writeCellWithExecutiveValue[T](executive2Input: Executive => Input[T]) =
    writeInputValue(getInputValue(executive2Input), cellIterators.map(_.next))
}

class MetaDataWriter(wb: Workbook) extends Writer {
  val sheet = wb.getSheet("Notes")
}

object SpreadsheetWriter {

  def write(out: OutputStream, company: CompanyFiscalYear) = {
    FileManager.load("docs/external/EmptyOutputTemplate.xls") {
      x =>
        {
          val wb = WorkbookFactory.create(x)
          writeExecDB(wb, company)
          wb.write(out)
        }
    }
  }

  def writeExecDB(wb: Workbook, company: CompanyFiscalYear) = {
    val dataWriter = new DataWriter(wb, company)
    import dataWriter._
    
    writeCellWithExecutiveValue(_.name)
    writeCellWithExecutiveValue(_.title)
    writeCellWithExecutiveValue(_.shortTitle)
    writeCellWithExecutiveValue(_.functionalMatches.primary)
    writeCellWithExecutiveValue(_.functionalMatches.secondary)
    writeCellWithExecutiveValue(_.functionalMatches.level)
    writeCellWithExecutiveValue(_.functionalMatches.scope)
    writeCellWithExecutiveValue(_.functionalMatches.bod)

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