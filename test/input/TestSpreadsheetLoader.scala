package input

import java.io.FileInputStream
import java.io.InputStream
import util.Closeables
import util._

trait TestSpreadsheetLoader {
  def load[T](name: String)(action: InputStream => T) = FileManager.load("test/input/" + name)(action)

  def loadSpreadsheet(name: String) = FileManager.loadSpreadsheet("test/input/" + name)
}