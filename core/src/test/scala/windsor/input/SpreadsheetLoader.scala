package windsor.input
import java.io.FileOutputStream
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.Row
import java.io.InputStream
import java.io.FileInputStream
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.WorkbookFactory
import scala.collection.JavaConversions._
object SpreadsheetLoader {

  def load(in: InputStream) = {
    val wb = WorkbookFactory.create(in)
    val sheet: Sheet = wb.getSheetAt(0)
    sheet.rowIterator.drop(1).map { x =>
      Company(
        x.getCell(0).getStringCellValue,
        x.getCell(1).getNumericCellValue,
        x.getCell(2).getNumericCellValue.toInt)
    }.toSeq
  }
}

