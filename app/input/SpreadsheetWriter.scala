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
import org.apache.poi.ss.util.CellUtil

trait Writer {

  def defineValidSheetCells(sheet: Sheet, x: Int = 50, y: Int = 50) = {
    for {
      m <- 1 to y
      n <- 0 to x
    } sheet.createRow(m).createCell(n).setAsActiveCell()
  }
}

class DataWriter(wb: Workbook, company: CompanyFiscalYear) extends Writer {
  //TODO: Remove Hardcoded values
  val sheet = {val s = wb.getSheet("ExecDB"); defineValidSheetCells(s); s} 
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

  def writeInputValue[T](names: Traversable[Input[T]],titleName: String, itemName: String, cells: Seq[Cell]): Unit = {
    names match {
      case Input(Some(value), calc, comment, note, link) :: xs => {
        val metadata = Seq(calc, comment, note, link)
        writeData(value, cells.head)
        if(!metadata.flatten.isEmpty){          
        	metaDataWriter.write(titleName, itemName, metadata)
        }
        writeInputValue(xs, titleName, itemName, cells.tail)
      }
      case Input(None, _, _, _, _) :: xs => writeInputValue(xs, titleName, itemName, cells.tail)
      case Nil => Unit
    }
  }

  def writeInput[T](executive2Input: Executive => Input[T], titleName: String, itemName: String) = 
    writeInputValue(getInputValue(executive2Input), titleName, itemName, cellIterators.map(_.next))
    
  def writeExecData[T](executive2Input: Executive => Input[T], itemName: String) =
    writeInput(executive2Input, "Exec Data", itemName)
    
  def writeCashCompensation[T](executive2Input: Executive => Input[T], itemName: String) =
    writeInput(executive2Input, "Cash Compensations", itemName)
    
  def writeEquityCompanyValue[T](executive2Input: Executive => Input[T], itemName: String) =
    writeInput(executive2Input, "Equity Company Value", itemName)
    
  def writeCarriedInterest[T](executive2Input: Executive => Input[T], itemName: String) =
    writeInput(executive2Input, "Carried Interest", itemName)    
    
}

class MetaDataWriter(wb: Workbook) extends Writer {
  val sheet = {val s = wb.getSheet("Notes"); defineValidSheetCells(s); s}
  val rowIterator = rows(sheet).iterator
  
  rowIterator.next
  
  def write(titleName: String, itemName: String, metadata: Seq[Option[String]]){
    val row = rowIterator.next
    CellUtil.getCell(row, 3).setCellValue(titleName)
    CellUtil.getCell(row, 4).setCellValue(itemName)
    
    metadata.foldLeft(5){ (acum, value) =>
    	value match {
    		case Some(v) => CellUtil.getCell(row, acum).setCellValue(v) 
    		case _ => Unit
        } 
    	acum + 1
    }
  }
  
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
    
    writeExecData(_.name, "Name")
    writeExecData(_.title, "Title")
    writeExecData(_.shortTitle, "Short Title")
    writeExecData(_.functionalMatches.primary, "Primary")
    writeExecData(_.functionalMatches.secondary, "Secondary")
    writeExecData(_.functionalMatches.level, "Level")
    writeExecData(_.functionalMatches.scope, "Scope")
    writeExecData(_.functionalMatches.bod, "Bod")

    writeCashCompensation(_.cashCompensations.baseSalary, "Base Salary")
    writeCashCompensation(_.cashCompensations.actualBonus, "Actual Bonus")
    writeCashCompensation(_.cashCompensations.targetBonus, "Target Bonus")
    writeCashCompensation(_.cashCompensations.thresholdBonus, "Threshold Bonus")
    writeCashCompensation(_.cashCompensations.maxBonus, "Max Bonus")
    writeCashCompensation(_.cashCompensations.new8KData.baseSalary, "8K Data - Base Salary")
    writeCashCompensation(_.cashCompensations.new8KData.targetBonus, "8K Data - Target Bonus")

    writeEquityCompanyValue(_.equityCompanyValue.optionsValue, "Options Value")
    writeEquityCompanyValue(_.equityCompanyValue.options, "Options")
    writeEquityCompanyValue(_.equityCompanyValue.exPrice, "Ex Price")
    writeEquityCompanyValue(_.equityCompanyValue.bsPercentage, "Bs Percentage")
    writeEquityCompanyValue(_.equityCompanyValue.timeVestRsValue, "Time VEst Rs Value")
    writeEquityCompanyValue(_.equityCompanyValue.shares, "Shares")
    writeEquityCompanyValue(_.equityCompanyValue.price, "Price")
    writeEquityCompanyValue(_.equityCompanyValue.perfRSValue, "Perf Rs Value")
    writeEquityCompanyValue(_.equityCompanyValue.shares2, "Shares 2")
    writeEquityCompanyValue(_.equityCompanyValue.price2, "Price 2")
    writeEquityCompanyValue(_.equityCompanyValue.perfCash, "Perf Cash")

    writeCarriedInterest(_.carriedInterest.ownedShares, "Owned Shares")
    writeCarriedInterest(_.carriedInterest.vestedOptions, "Vested Options")
    writeCarriedInterest(_.carriedInterest.unvestedOptions, "Unvested Options")
    writeCarriedInterest(_.carriedInterest.tineVest, "Tine Vest")
    writeCarriedInterest(_.carriedInterest.perfVest, "Perf Vest")
  }

  def loadTemplateInto(out: OutputStream) = {
    FileManager.load("docs/external/EmptyOutputTemplate.xls") {
      x => WorkbookFactory.create(x).write(out)
    }
  }
}