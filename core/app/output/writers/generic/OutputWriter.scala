package output.writers.generic

import libt.Path
import libt.PK
import libt.TModel
import libt.Model
import libt.spreadsheet.Strip
import libt.spreadsheet.writer.WriteStrategy
import util.FileManager
import output.FlattedAreaLayout
import output.FlattedArea
import java.io.OutputStream
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import libt.symbol2Route

trait OutputWriter {
  val schema: TModel
  val fileName: String
  def write(out: Workbook, models: Seq[Model], yearRange: Int): Unit

  def outputArea(
    layout: FlattedAreaLayout,
    outputMapping: Seq[Strip],
    flatteningPK: Path,
    flatteningPath: Path,
    writeStrategy: WriteStrategy) =
      FlattedArea(
        PK(Path('ticker), Path('name), Path('disclosureFiscalYearDate)),
        PK(flatteningPK),
        flatteningPath,
        schema,
        layout,
        outputMapping,
        writeStrategy)

  def write(out: OutputStream, companies: Seq[Model], range: Int): Unit = {
    FileManager.loadResource(fileName) {
      x =>
      {
        val wb = WorkbookFactory.create(x)
        write(wb, companies, range)
        wb.write(out)
      }
    }
  }
}