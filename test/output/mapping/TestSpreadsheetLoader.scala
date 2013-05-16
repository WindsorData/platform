package output.mapping
import java.io.InputStream
import util._

trait TestSpreadsheetLoader  {
  def load[T](name: String)(action: InputStream => T) = FileManager.load("test/input/" + name)(action)
  
}