package libt.spreadsheet.reader

import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.hssf.usermodel.HSSFWorkbook

object MyWorkbookFactory {
    import libt.spreadsheet.util._

    implicit def workbook2RichWorkbook(wb: Workbook) = new {

      def addSingleColumnOrientedValue(colIndex: Int, value: String = null) = {
        val sheet = wb.getSheetAt(0)

        if (value == null)
          sheet.cellAt(0, colIndex)
        else
          sheet.cellAt(0, colIndex).setCellValue(value)

        (1 to 4).foreach(sheet.cellAt(_, colIndex).setAsActiveCell())
        wb
      }

      def addSingleRowOrientedValue(rowIndex: Int, value: String = null) = {
        val sheet = wb.getSheetAt(0)

        if (value == null)
          sheet.cellAt(rowIndex, 0).setAsActiveCell()
        else
          sheet.cellAt(rowIndex, 0).setCellValue(value)

        (1 to 2).foreach(sheet.cellAt(rowIndex, _).setAsActiveCell())
        wb
      }
    }

    def createNewSingleSheetWorkbook = {
      val wb = new HSSFWorkbook
      wb.createSheet()
      wb
    }

  }