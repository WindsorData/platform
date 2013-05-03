package output
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.usermodel.Sheet
import java.io.OutputStream
import org.apache.poi.ss.usermodel.Workbook
import model._
import util.FileManager
import libt.util._
import libt._
import libt.spreadsheet._
import libt.spreadsheet.util._
import libt.spreadsheet.reader._

//trait Writer {
//
//  def defineValidSheetCells(sheet: Sheet, x: Int = 50, y: Int = 100) = {
//    for {
//      m <- 1 to y
//      n <- 0 to x
//    } sheet.createRow(m).createCell(n).setAsActiveCell()
//  }
//}
//
//class DataWriter(wb: Workbook) extends Writer {
//  //TODO: Remove Hardcoded values
//  val sheet = { val s = wb.getSheet("ExecDB"); defineValidSheetCells(s); s }
//  val metaDataWriter = new MetaDataWriter(wb)
//  val rowIterator = sheet.rows.iterator
//  var cellIterator: Iterator[Cell] = null
//
//  def nextExecutiveRow = cellIterator = rowIterator.next.cells.iterator
//  val setCurrentCompany = metaDataWriter.setCurrentCompany
//
//  def writeData[T](value: T, cell: Cell): Unit = {
//    value match {
//      case v: String => cell.setCellValue(v)
//      case v: BigDecimal => cell.setCellValue(v.toDouble)
//      case v: Double => cell.setCellValue(v)
//      case v: Integer => cell.setCellValue(v.toDouble)
//      case v: Boolean => cell.setCellValue(v)
//      case v: Date => cell.setCellValue(v)
//    }
//  }
//
//  def writeInputValue[T](name: Value[T], cell: Cell, writeMetaData: Seq[Option[String]] => Unit): Unit = {
//    name match {
//      case Value(Some(value), calc, comment, note, link) => {
//        writeData(value, cell)
//        writeMetaData(Seq(calc, comment, note, link))
//      }
//      case _ => Unit
//    }
//  }
//
//}
//
//class MetaDataWriter(wb: Workbook) extends Writer {
//  val sheet = { val s = wb.getSheet("Notes"); defineValidSheetCells(s); s }
//  val rowIterator = sheet.rows.iterator
//  var company: Model = null
//  var isCompanyHeaderWritten = false
//  var lastName: String = null
//
//  val setCurrentCompany = (c: Model) => {
//    company = c
//    isCompanyHeaderWritten = false
//    lastName = null
//  }
//
//  def setLastName(lastNameField: String) = lastName = lastNameField
//
//  rowIterator.next
//
//  def write(titleName: String, itemName: String, metadata: Seq[Option[String]]) {
//    val row = rowIterator.next
//    if (!isCompanyHeaderWritten) {
//      row.cellAt(0).setCellValue(company.v[String]('ticker).value.get)
//      row.cellAt(1).setCellValue(company.v[String]('name).value.get)
//      row.cellAt(2).setCellValue(company.v[Int]('disclosureFiscalYear).value.get)
//      isCompanyHeaderWritten = true
//    }
//    row.cellAt(3).setCellValue(lastName)
//    row.cellAt(4).setCellValue(titleName)
//    row.cellAt(5).setCellValue(itemName)
//
//    metadata.foldLeft(5) { (acum, value) =>
//      value match {
//        case Some(v) => row.cellAt(acum).setCellValue(v)
//        case _ => Unit
//      }
//      acum + 1
//    }
//  }
//
//}

object SpreadsheetWriter {

  def write(out: OutputStream, companies: Seq[Model]): Unit = {
    FileManager.load("docs/external/EmptyOutputTemplate.xls") {
      x =>
        {
          val wb = WorkbookFactory.create(x)
          write(wb, companies)
          wb.write(out)
        }
    }
  }
  def write(out: Workbook, companies: Seq[Model]): Unit = {
    val areas = Seq(ValueAreaLayout(Offset(0,0)), MetadataAreaLayout(Offset(0,0))).map(
      FlattedArea(
        PK(Path('ticker), Path('name), Path('disclosureFiscalYear)),
        PK(Path('lastName)),
        Path('executives),
        TCompanyFiscalYear,
        _,
        Seq(
          Feature('firstName),
          Feature('lastName),
          Feature('title),
          Feature('functionalMatches, 'primary),
          Feature('functionalMatches, 'secondary),
          Feature('functionalMatches, 'level),
          Feature('functionalMatches, 'scope),
          Feature('functionalMatches, 'bod),
          Feature('founder),
          Feature('transitionPeriod))))
    WorkbookMapping(areas).write(companies, out)
  }

  def loadTemplateInto(out: OutputStream) = {
    FileManager.load("docs/external/EmptyOutputTemplate.xls") {
      x => WorkbookFactory.create(x).write(out)
    }
  }
}