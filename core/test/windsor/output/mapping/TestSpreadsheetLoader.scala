package windsor.output.mapping
import java.io.InputStream
import util._

trait TestSpreadsheetLoader  {
  def loadSheet[T](name: String)(action: InputStream => T) =
    FileManager.loadFile(s"test/input/$name")(action)
}