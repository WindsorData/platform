package input

import model._
import util.IndexedTraversables._
import java.io.FileOutputStream
import org.apache.poi.ss.util.WorkbookUtil
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.OutputStream
object SpreadsheetPersister {
 
  def persist(companies: Traversable[Company], out: OutputStream) {
//    val wb = new XSSFWorkbook()
//    val sheet = wb.createSheet("Report")
//    companies.foreachWithIndex { (company, index) =>
//      val row = sheet.createRow(index)
//      row.createCell(0).setCellValue(company.name)
//      row.createCell(1).setCellValue(company.)
//      row.createCell(2).setCellValue(company.foundingYear)
//    }
//    wb.write(out)
  }

}