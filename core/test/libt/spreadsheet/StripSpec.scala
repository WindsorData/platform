package libt.spreadsheet

import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfter
import org.scalatest.FunSpec
import libt.Value
import libt.spreadsheet.reader._
import libt.spreadsheet.writer.CellWriter
import org.scalatest.junit.JUnitRunner
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Sheet
import libt.spreadsheet.util._
import org.apache.poi.ss.usermodel.Cell
import libt.spreadsheet.writer.ColumnOrientedWriter
import libt.spreadsheet.writer.ColumnOrientedWriter

class StripSpec extends FunSpec with MockFactory with BeforeAndAfter {

    var reader: CellReader = null
    var writer: CellWriter = null
    val wb = new HSSFWorkbook
    var sheet: Sheet = _
    
  describe("WithDefault") {

	before{
	  wb.createSheet()
	  sheet = wb.getSheetAt(0)
	}
	
	after{
	  wb.removeSheetAt(0)
	}
      
    it("should read the actual value if is not the default one") {
      reader = mock[CellReader]
      (reader.string _).expects.returns(Value("Y"))
      assert(TWithDefaultMapping(TStringMapping, "X").read(reader) === Value("Y"))
    }

    it("should read value if is not the default one") {
      reader = mock[CellReader]
      (reader.string _).expects.returns(Value())
      assert(TWithDefaultMapping(TStringMapping, "X").read(reader) === Value("X"))
    }
    
    it("should write the actual value if is not the default one") {
      writer = new ColumnOrientedWriter(0, sheet.rows)
      writer.write(TWithDefaultMapping(TStringMapping, "X").writeOp(Some("f")) :: Nil)
      assert(sheet.cellAt(0, 0).getStringCellValue() === "f")
    }

    it("should skip the column when the value is the default") {
      writer = new ColumnOrientedWriter(0, sheet.rows)
      writer.write(TWithDefaultMapping(TStringMapping, "X").writeOp(Some("X")) :: Nil)
      assert(sheet.cellAt(0, 0).getCellType() === Cell.CELL_TYPE_BLANK) 
    }

  }

}