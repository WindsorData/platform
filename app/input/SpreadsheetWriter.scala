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

  def defineValidSheetCells(sheet: Sheet, x: Int = 50, y: Int = 100) = {
    for {
      m <- 1 to y
      n <- 0 to x
    } sheet.createRow(m).createCell(n).setAsActiveCell()
  }
}

class DataWriter(wb: Workbook) extends Writer {
  //TODO: Remove Hardcoded values
  val sheet = { val s = wb.getSheet("ExecDB"); defineValidSheetCells(s); s }
  val metaDataWriter = new MetaDataWriter(wb)
  val rowIterator = rows(sheet).iterator
  var cellIterator: Iterator[Cell] = null

  def nextExecutiveRow = cellIterator = cells(rowIterator.next).iterator
  val setCurrentCompany = metaDataWriter.setCurrentCompany

  def writeData[T](value: T, cell: Cell): Unit = {
    value match {
      case v: String => cell.setCellValue(v)
      case v: BigDecimal => cell.setCellValue(v.toDouble)
      case v: Double => cell.setCellValue(v)
      case v: Integer => cell.setCellValue(v.toDouble)
      case v: Boolean => cell.setCellValue(v)
      case v: Date => cell.setCellValue(v)
    }
  }

  def writeInputValue[T](name: Input[T], cell: Cell, writeMetaData: Seq[Option[String]] => Unit): Unit = {
    name match {
      case Input(Some(value), calc, comment, note, link) => {
        writeData(value, cell)
        writeMetaData(Seq(calc, comment, note, link))
      }
      case _ => Unit
    }
  }

  def writeMetaDataIfExists(title: String, name: String)(metadata: Seq[Option[String]]) =
    if (!metadata.flatten.isEmpty)
      metaDataWriter.write(title, name, metadata)

  def writeCompanyMetaDataIfExists[T](i: Input[T], itemName: String) =
    writeMetaDataIfExists("Company Data", itemName)(Seq(i.calc, i.comment, i.note, i.link))

  def writeInput[T](i: Input[T], titleName: String, itemName: String) =
    writeInputValue(i, cellIterator.next, writeMetaDataIfExists(titleName, itemName)_)

  def writeExecData[T](i: Input[T], itemName: String) =
    writeInput(i, "Exec Data", itemName)

  def writeCashCompensation[T](i: Input[T], itemName: String) =
    writeInput(i, "Cash Compensations", itemName)

  def writeEquityCompanyValue[T](i: Input[T], itemName: String) =
    writeInput(i, "Equity Company Value", itemName)

  def writeCarriedInterest[T](i: Input[T], itemName: String) =
    writeInput(i, "Carried Interest", itemName)

  def writeCompanyData[T](input: Input[T]) =
    writeInputValue(input, cellIterator.next, (_) => Unit)

}

class MetaDataWriter(wb: Workbook) extends Writer {
  val sheet = { val s = wb.getSheet("Notes"); defineValidSheetCells(s); s }
  val rowIterator = rows(sheet).iterator
  var company: CompanyFiscalYear = null
  var isCompanyHeaderWritten = false
  var lastName: String = null

  val setCurrentCompany = (c: CompanyFiscalYear) => {
    company = c
    isCompanyHeaderWritten = false
    lastName = null
  }

  def setLastName(lastNameField: String) = lastName = lastNameField

  rowIterator.next

  def write(titleName: String, itemName: String, metadata: Seq[Option[String]]) {
    val row = rowIterator.next
    if (!isCompanyHeaderWritten) {
      CellUtil.getCell(row, 0).setCellValue(company.ticker.value.get)
      CellUtil.getCell(row, 1).setCellValue(company.name.value.get)
      CellUtil.getCell(row, 2).setCellValue(company.disclosureFiscalYear.value.get)
      isCompanyHeaderWritten = true
    }
    CellUtil.getCell(row, 3).setCellValue(lastName)
    CellUtil.getCell(row, 4).setCellValue(titleName)
    CellUtil.getCell(row, 5).setCellValue(itemName)

    metadata.foldLeft(5) { (acum, value) =>
      value match {
        case Some(v) => CellUtil.getCell(row, acum).setCellValue(v)
        case _ => Unit
      }
      acum + 1
    }
  }

}

object SpreadsheetWriter {

  def write(out: OutputStream, companies: Seq[CompanyFiscalYear]) = {
    FileManager.load("docs/external/EmptyOutputTemplate.xls") {
      x =>
        {
          val wb = WorkbookFactory.create(x)
          writeExecDB(wb, companies)
          wb.write(out)
        }
    }
  }

  def writeExecDB(wb: Workbook, companies: Seq[CompanyFiscalYear]) = {

    val dataWriter = new DataWriter(wb)

    companies.foreach { comp =>
      import dataWriter._

      setCurrentCompany(comp)
      writeCompanyMetaDataIfExists(comp.ticker, "Ticker")
      writeCompanyMetaDataIfExists(comp.name, "Name")
      writeCompanyMetaDataIfExists(comp.disclosureFiscalYear, "Disclosure Fiscal Year")

      comp.executives.foreach { e =>

        nextExecutiveRow
        
        metaDataWriter.setLastName(e.name.value.get)

        writeCompanyData(comp.ticker)
        writeCompanyData(comp.name)
        writeCompanyData(comp.disclosureFiscalYear)

        writeExecData(e.name, "Name")
        writeExecData(e.title, "Title")
        writeExecData(e.shortTitle, "Short Title")
        writeExecData(e.functionalMatches.primary, "Primary")
        writeExecData(e.functionalMatches.secondary, "Secondary")
        writeExecData(e.functionalMatches.level, "Level")
        writeExecData(e.functionalMatches.scope, "Scope")
        writeExecData(e.functionalMatches.bod, "Bod")

        writeCashCompensation(e.cashCompensations.baseSalary, "Base Salary")
        writeCashCompensation(e.cashCompensations.actualBonus, "Actual Bonus")
        writeCashCompensation(e.cashCompensations.targetBonus, "Target Bonus")
        writeCashCompensation(e.cashCompensations.thresholdBonus, "Threshold Bonus")
        writeCashCompensation(e.cashCompensations.maxBonus, "Max Bonus")
        writeCashCompensation(e.cashCompensations.new8KData.baseSalary, "8K Data - Base Salary")
        writeCashCompensation(e.cashCompensations.new8KData.targetBonus, "8K Data - Target Bonus")

        writeEquityCompanyValue(e.equityCompanyValue.optionsValue, "Options Value")
        writeEquityCompanyValue(e.equityCompanyValue.options, "Options")
        writeEquityCompanyValue(e.equityCompanyValue.exPrice, "Ex Price")
        writeEquityCompanyValue(e.equityCompanyValue.bsPercentage, "Bs Percentage")
        writeEquityCompanyValue(e.equityCompanyValue.timeVestRsValue, "Time VEst Rs Value")
        writeEquityCompanyValue(e.equityCompanyValue.shares, "Shares")
        writeEquityCompanyValue(e.equityCompanyValue.price, "Price")
        writeEquityCompanyValue(e.equityCompanyValue.perfRSValue, "Perf Rs Value")
        writeEquityCompanyValue(e.equityCompanyValue.shares2, "Shares 2")
        writeEquityCompanyValue(e.equityCompanyValue.price2, "Price 2")
        writeEquityCompanyValue(e.equityCompanyValue.perfCash, "Perf Cash")

        writeCarriedInterest(e.carriedInterest.ownedShares, "Owned Shares")
        writeCarriedInterest(e.carriedInterest.vestedOptions, "Vested Options")
        writeCarriedInterest(e.carriedInterest.unvestedOptions, "Unvested Options")
        writeCarriedInterest(e.carriedInterest.tineVest, "Tine Vest")
        writeCarriedInterest(e.carriedInterest.perfVest, "Perf Vest")
      }

    }
  }

  def loadTemplateInto(out: OutputStream) = {
    FileManager.load("docs/external/EmptyOutputTemplate.xls") {
      x => WorkbookFactory.create(x).write(out)
    }
  }
}