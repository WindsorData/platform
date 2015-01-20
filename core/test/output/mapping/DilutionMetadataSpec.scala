package output.mapping

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.scalatest.FunSpec

import libt.spreadsheet.writer._
import libt.spreadsheet._
import libt._

import output._

class DilutionMetadataSpec extends FunSpec {

  describe("DilutionMetadata") {
    it("can write") {
      val wb = new HSSFWorkbook
      wb.createSheet()
      val sheet = wb.getSheetAt(0)

      val models = Seq(Model(
        'ticker -> Value("1222"),
        'name -> Value("foo"),
        'disclosureFiscalYearDate -> Value(1990),
        'bod -> Col(
          Model(),
          Model())))
          
      val area = FlattedArea(
        PK(Path('ticker), Path('name), Path('disclosureFiscalYearDate)),
        PK(),
        Path('bod, *),
        model.TCompanyFiscalYear,
        MetadataAreaLayout(Offset(0, 0)),
        model.mapping.bod.bodMapping,
        FullWriteStrategy)
        
        assert(area.completePKSize === 3)

      area.write(models)(sheet)
    }
  }

}