package util

import java.io.InputStream
import java.io.FileInputStream
import input.SpreadsheetLoader

object FileManager {

  def load[T](name: String)(action: InputStream => T) = {
    import Closeables._
    new FileInputStream(name).processWith {
      x => action(x)
    }
  }

  def loadSpreadsheet(name: String) = {
    load(name) { x =>
      SpreadsheetLoader.load(x)
    }
  }

}