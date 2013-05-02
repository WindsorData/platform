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
import libt.spreadsheet.writer.ColumnOrientedValueWriter
import libt.spreadsheet.util._
import org.apache.poi.ss.usermodel.Cell

@RunWith(classOf[JUnitRunner])
class FeatureReaderSpec extends FunSpec with MockFactory with BeforeAndAfter {

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
      assert(WithDefaultReader(StringReader, "X").read(reader) === Value("Y"))
    }

    it("should read value if is not the default one") {
      reader = mock[CellReader]
      (reader.string _).expects.returns(Value())
      assert(WithDefaultReader(StringReader, "X").read(reader) === Value("X"))
    }
    
    it("should write the actual value if is not the default one") {
      writer = new ColumnOrientedValueWriter(0, sheet.rows(0))
      WithDefaultReader(StringReader, "X").write(writer, Value("f"))
      assert(sheet.cellAt(0, 0).getStringCellValue() === "f")
    }

    it("should skip the column when the value is the default") {
      writer = new ColumnOrientedValueWriter(0, sheet.rows(0))
      WithDefaultReader(StringReader, "X").write(writer, Value("X"))
      assert(sheet.cellAt(0, 0).getCellType() === Cell.CELL_TYPE_BLANK) 
    }

  }

}