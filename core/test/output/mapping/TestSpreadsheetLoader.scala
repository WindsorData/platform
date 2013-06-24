package output.mapping
import java.io.InputStream
import util._

trait TestSpreadsheetLoader  {
  def loadResource[T](name: String)(action: InputStream => T) = FileManager.loadResource(s"input/$name")(action)
  
}