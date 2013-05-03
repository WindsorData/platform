package input

import org.scalatest.BeforeAndAfter
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner
import output.MetadataAreaLayout
import libt.spreadsheet.reader.Offset
import org.apache.poi.ss.usermodel.Row
import libt._
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import libt.spreadsheet.util._
import org.apache.poi.ss.usermodel.Sheet
import java.io.FileOutputStream
import org.apache.poi.ss.usermodel.Cell
import libt.spreadsheet.writer.RowOrientedWriter
import libt.spreadsheet.writer.op

@RunWith(classOf[JUnitRunner])
class RowOrientedWriterSpec extends FlatSpec with BeforeAndAfter {
  
  def metadata(value: Value[_]) = value.metadataSeq.map(op.String(_))

  it should "write feature metadata in a row basis" in {
    val wb = new HSSFWorkbook()
    val sheet = wb.createSheet();
    sheet.defineLimits(Offset(0,0), 3, 4)
    val rows  = sheet.rows
    
    val writer = new RowOrientedWriter(Offset(0, 0), rows)
    writer.write(metadata(Value(2)))
    writer.write(metadata(Value(Some("v1"), Some("v2"), Some("v3"), Some("v4"), Some("v5"))))
    writer.write(metadata(Value(Some("v6"), Some("v7"), Some("v8"), Some("v9"), Some("v10"))))
    
    assert(sheet.cellAt(0, 0).getCellType === Cell.CELL_TYPE_BLANK)
    assert(sheet.cellAt(1, 0).getStringCellValue === "v2")
    assert(sheet.cellAt(1, 3).getStringCellValue === "v5")
    assert(sheet.cellAt(2, 0).getStringCellValue === "v7")
  }

}