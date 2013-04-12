package input

import java.io.InputStream
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileOutputStream
import java.io.OutputStream
import util.poi.Cells._
import org.apache.poi.ss.usermodel.Workbook
import util.FileManager
import org.apache.poi.ss.usermodel.Cell
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
import libt.Model
import libt.Value

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

  def writeInputValue[T](name: Value[T], cell: Cell, writeMetaData: Seq[Option[String]] => Unit): Unit = {
    name match {
      case Value(Some(value), calc, comment, note, link) => {
        writeData(value, cell)
        writeMetaData(Seq(calc, comment, note, link))
      }
      case _ => Unit
    }
  }

  def writeMetaDataIfExists(title: String, name: String)(metadata: Seq[Option[String]]) =
    if (!metadata.flatten.isEmpty)
      metaDataWriter.write(title, name, metadata)

  def writeCompanyMetaDataIfExists[T](i: Value[T], itemName: String) =
    writeMetaDataIfExists("Company Data", itemName)(Seq(i.calc, i.comment, i.note, i.link))

  def writeInput[T](i: Value[T], titleName: String, itemName: String) =
    writeInputValue(i, cellIterator.next, writeMetaDataIfExists(titleName, itemName)_)

  def writeExecData[T](i: Value[T], itemName: String) =
    writeInput(i, "Exec Data", itemName)

  def writeCashCompensation[T](i: Value[T], itemName: String) =
    writeInput(i, "Cash Compensations", itemName)

  def writeEquityCompanyValue[T](i: Value[T], itemName: String) =
    writeInput(i, "Equity Company Value", itemName)

  def writeCarriedInterest[T](i: Value[T], itemName: String) =
    writeInput(i, "Carried Interest", itemName)

  def writeCompanyData[T](input: Value[T]) =
    writeInputValue(input, cellIterator.next, (_) => Unit)

}

class MetaDataWriter(wb: Workbook) extends Writer {
  val sheet = { val s = wb.getSheet("Notes"); defineValidSheetCells(s); s }
  val rowIterator = rows(sheet).iterator
  var company: Model = null
  var isCompanyHeaderWritten = false
  var lastName: String = null

  val setCurrentCompany = (c: Model) => {
    company = c
    isCompanyHeaderWritten = false
    lastName = null
  }

  def setLastName(lastNameField: String) = lastName = lastNameField

  rowIterator.next

  def write(titleName: String, itemName: String, metadata: Seq[Option[String]]) {
    val row = rowIterator.next
    if (!isCompanyHeaderWritten) {
      CellUtil.getCell(row, 0).setCellValue(company.v[String]('ticker).value.get)
      CellUtil.getCell(row, 1).setCellValue(company.v[String]('name).value.get)
      CellUtil.getCell(row, 2).setCellValue(company.v[Int]('disclosureFiscalYear).value.get)
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

  def write(out: OutputStream, companies: Seq[Model]) = {
    FileManager.load("docs/external/EmptyOutputTemplate.xls") {
      x =>
        {
          val wb = WorkbookFactory.create(x)
          writeExecDB(wb, companies)
          wb.write(out)
        }
    }
  }

  def writeExecDB(wb: Workbook, companies: Seq[Model]) = {

    val dataWriter = new DataWriter(wb)

    companies.foreach { comp =>
      import dataWriter._

      setCurrentCompany(comp)
      writeCompanyMetaDataIfExists(comp.v('ticker), "Ticker")
      writeCompanyMetaDataIfExists(comp.v('name), "Name")
      writeCompanyMetaDataIfExists(comp.v('disclosureFiscalYear), "Disclosure Fiscal Year")

      comp.c('executives).foreach { e =>
        
        nextExecutiveRow
        
        metaDataWriter.setLastName(e.v('name).value.get)

        writeCompanyData(comp.v('ticker))
        writeCompanyData(comp.v('name))
        writeCompanyData(comp.v('disclosureFiscalYear))
        
        writeExecData(e.v('name), "Name")
        writeExecData(e.v('title), "Title")
        writeExecData(e.v('shortTitle), "Short Title")
        writeExecData(e.m('functionalMatches).v('primary), "Primary")
        writeExecData(e.m('functionalMatches).v('secondary), "Secondary")
        writeExecData(e.m('functionalMatches).v('level), "Level")
        writeExecData(e.m('functionalMatches).v('scope), "Scope")
        writeExecData(e.m('functionalMatches).v('bod), "Bod")
        
        writeCashCompensation(e.m('cashCompensations).v('baseSalary), "Base Salary")
        writeCashCompensation(e.m('cashCompensations).v('actualBonus), "Actual Bonus")
        writeCashCompensation(e.m('cashCompensations).v('targetBonus), "Target Bonus")
        writeCashCompensation(e.m('cashCompensations).v('thresholdBonus), "Threshold Bonus")
        writeCashCompensation(e.m('cashCompensations).v('maxBonus), "Max Bonus")
        writeCashCompensation(e.m('cashCompensations).m('new8KData).v('baseSalary), "8K Data - Base Salary")
        writeCashCompensation(e.m('cashCompensations).m('new8KData).v('targetBonus), "8K Data - Target Bonus")

        writeEquityCompanyValue(e.m('equityCompanyValue).v('optionsValue), "Options Value")
        writeEquityCompanyValue(e.m('equityCompanyValue).v('options), "Options")
        writeEquityCompanyValue(e.m('equityCompanyValue).v('exPrice), "Ex Price")
        writeEquityCompanyValue(e.m('equityCompanyValue).v('bsPercentage), "Bs Percentage")
        writeEquityCompanyValue(e.m('equityCompanyValue).v('timeVestRsValue), "Time VEst Rs Value")
        writeEquityCompanyValue(e.m('equityCompanyValue).v('shares), "Shares")
        writeEquityCompanyValue(e.m('equityCompanyValue).v('price), "Price")
        writeEquityCompanyValue(e.m('equityCompanyValue).v('perfRSValue), "Perf Rs Value")
        writeEquityCompanyValue(e.m('equityCompanyValue).v('shares2), "Shares 2")
        writeEquityCompanyValue(e.m('equityCompanyValue).v('price2), "Price 2")
        writeEquityCompanyValue(e.m('equityCompanyValue).v('perfCash), "Perf Cash")

        writeCarriedInterest(e.m('carriedInterest).v('ownedShares), "Owned Shares")
        writeCarriedInterest(e.m('carriedInterest).v('vestedOptions), "Vested Options")
        writeCarriedInterest(e.m('carriedInterest).v('unvestedOptions), "Unvested Options")
        writeCarriedInterest(e.m('carriedInterest).v('tineVest), "Tine Vest")
        writeCarriedInterest(e.m('carriedInterest).v('perfVest), "Perf Vest")
      }

    }
  }

  def loadTemplateInto(out: OutputStream) = {
    FileManager.load("docs/external/EmptyOutputTemplate.xls") {
      x => WorkbookFactory.create(x).write(out)
    }
  }
}