package input

import java.io.InputStream
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileOutputStream
import java.io.OutputStream
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
import libt.Model
import libt.Value
import libt._
import libt.spreadsheet.util._
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
  val rowIterator = sheet.rows.iterator
  var cellIterator: Iterator[Cell] = null

  def nextExecutiveRow = cellIterator = rowIterator.next.cells.iterator
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

  def writeCompanyData[T](input: Value[T]) =
    writeInputValue(input, cellIterator.next, (_) => Unit)
}

class MetaDataWriter(wb: Workbook) extends Writer {
  val sheet = { val s = wb.getSheet("Notes"); defineValidSheetCells(s); s }
  val rowIterator = sheet.rows.iterator
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
      row.cellAt(0).setCellValue(company.v[String]('ticker).value.get)
      row.cellAt(1).setCellValue(company.v[String]('name).value.get)
      row.cellAt(2).setCellValue(company.v[Int]('disclosureFiscalYear).value.get)
      isCompanyHeaderWritten = true
    }
    row.cellAt(3).setCellValue(lastName)
    row.cellAt(4).setCellValue(titleName)
    row.cellAt(5).setCellValue(itemName)

    metadata.foldLeft(5) { (acum, value) =>
      value match {
        case Some(v) => row.cellAt(acum).setCellValue(v)
        case _ => Unit
      }
      acum + 1
    }
  }

}
import libt._
import libt.spreadsheet.util._

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
        
        metaDataWriter.setLastName(e.v('lastName).value.get)

        writeCompanyData(comp.v('ticker))
        writeCompanyData(comp.v('name))
        writeCompanyData(comp.v('disclosureFiscalYear))
        
        writeExecData(e.v('firstName), "First Name")
        writeExecData(e.v('lastName), "Last Name")
        writeExecData(e.v('title), "Title")
        writeExecData(e.m('functionalMatches).v('primary), "Primary")
        writeExecData(e.m('functionalMatches).v('secondary), "Secondary")
        writeExecData(e.m('functionalMatches).v('level), "Level")
        writeExecData(e.m('functionalMatches).v('scope), "Scope")
        writeExecData(e.m('functionalMatches).v('bod), "Bod")
        
        writeExecData(e.v('founder), "Founder")
        writeExecData(e.v('transitionPeriod), "Transition Period")
        
        writeCashCompensation(e.m('cashCompensations).v('baseSalary), "Base Salary")
        writeCashCompensation(e.m('cashCompensations).v('actualBonus), "Actual Bonus")
        writeCashCompensation(e.m('cashCompensations).v('targetBonus), "Target Bonus")
        writeCashCompensation(e.m('cashCompensations).v('thresholdBonus), "Threshold Bonus")
        writeCashCompensation(e.m('cashCompensations).v('maxBonus), "Max Bonus")
        writeCashCompensation(e.m('cashCompensations).m('nextFiscalYearData).v('baseSalary), "Next Fiscal Year Data - Base Salary")
        writeCashCompensation(e.m('cashCompensations).m('nextFiscalYearData).v('targetBonus), "Next Fiscal Year Data - Target Bonus")
        
        e.c('optionGrants).seq.foldLeft(0){ case (acum, elem) => 
          writeInput(elem.asInstanceOf[Model].v('grantDate), "OptionGrants - Grant " + acum.toString, "Grant Date")
          writeInput(elem.asInstanceOf[Model].v('expireDate), "OptionGrants - Grant " + acum.toString, "Expire Date")
          writeInput(elem.asInstanceOf[Model].v('number), "OptionGrants - Grant " + acum.toString, "Number")
          writeInput(elem.asInstanceOf[Model].v('price), "OptionGrants - Grant " + acum.toString, "Price")
          writeInput(elem.asInstanceOf[Model].v('value), "OptionGrants - Grant " + acum.toString, "Value")
          writeInput(elem.asInstanceOf[Model].v('perf), "OptionGrants - Grant " + acum.toString, "Perf")
          writeInput(elem.asInstanceOf[Model].v('type), "OptionGrants - Grant " + acum.toString, "Type")
          acum + 1
        }
        
        e.c('timeVestRS).seq.foldLeft(0){ case (acum, elem) => 
          writeInput(elem.asInstanceOf[Model].v('grantDate), "Time Vest Rs - Grant " + acum.toString, "Grant Date")
          writeInput(elem.asInstanceOf[Model].v('number), "Time Vest Rs - Grant " + acum.toString, "Number")
          writeInput(elem.asInstanceOf[Model].v('price), "Time Vest Rs - Grant " + acum.toString, "Price")
          writeInput(elem.asInstanceOf[Model].v('value), "Time Vest Rs - Grant " + acum.toString, "Value")
          writeInput(elem.asInstanceOf[Model].v('type), "Time Vest Rs - Grant " + acum.toString, "Type")
          acum + 1
        }
        
        e.c('performanceVestRS).seq.foldLeft(0){ case (acum, elem) => 
          writeInput(elem.asInstanceOf[Model].v('grantDate), "Performance Vest RS - Grant " + acum.toString, "Grant Date")
          writeInput(elem.asInstanceOf[Model].v('targetNumber), "Performance Vest RS - Grant " + acum.toString, "Target Value")
          writeInput(elem.asInstanceOf[Model].v('grantDatePrice), "Performance Vest RS - Grant " + acum.toString, "Grant Date Price")
          writeInput(elem.asInstanceOf[Model].v('targetValue), "Performance Vest RS - Grant " + acum.toString, "Target Value")
          writeInput(elem.asInstanceOf[Model].v('type), "Performance Vest RS - Grant " + acum.toString, "Type")
          acum + 1
        }
        
        e.c('performanceCash).seq.foldLeft(0){ case (acum, elem) => 
          writeInput(elem.asInstanceOf[Model].v('grantDate), "Performance Cash - Grant " + acum.toString, "Grant Date")
          writeInput(elem.asInstanceOf[Model].v('targetValue), "Performance Cash - Grant " + acum.toString, "Target Number")
          writeInput(elem.asInstanceOf[Model].v('payout), "Performance Cash - Grant " + acum.toString, "Payout")
          acum + 1
        }
        
        writeInput(e.m('carriedInterest).m('ownedShares).v('beneficialOwnership), "Carried Interest - Owned Shares", "Beneficial Ownership")
        writeInput(e.m('carriedInterest).m('ownedShares).v('options), "Carried Interest - Owned Shares", "Options")
        writeInput(e.m('carriedInterest).m('ownedShares).v('unvestedRestrictedStock), "Carried Interest - Owned Shares", "Unvested Restricted Stock")
        writeInput(e.m('carriedInterest).m('ownedShares).v('disclaimBeneficialOwnership), "Carried Interest - Owned Shares", "Disclaim Beneficial Ownership")
        writeInput(e.m('carriedInterest).m('ownedShares).v('heldByTrust), "Carried Interest - Owned Shares", "Held By Trust")
        writeInput(e.m('carriedInterest).m('ownedShares).v('other), "Carried Interest - Owned Shares", "Other")
        
        writeInput(e.m('carriedInterest).m('outstandingEquityAwards).v('vestedOptions), "Carried Interest - Outstanding Equity Awards", "Vested Options")
        writeInput(e.m('carriedInterest).m('outstandingEquityAwards).v('unvestedOptions), "Carried Interest - Outstanding Equity Awards", "Unvested Options")
        writeInput(e.m('carriedInterest).m('outstandingEquityAwards).v('timeVestRS), "Carried Interest - Outstanding Equity Awards", "Time Vest RS")
        writeInput(e.m('carriedInterest).m('outstandingEquityAwards).v('perfVestRS), "Carried Interest - Outstanding Equity Awards", "Perf Vest RS")
        
      }

    }
  }

  def loadTemplateInto(out: OutputStream) = {
    FileManager.load("docs/external/EmptyOutputTemplate.xls") {
      x => WorkbookFactory.create(x).write(out)
    }
  }
}