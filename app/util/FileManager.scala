package util

import java.io.InputStream
import java.io.FileInputStream
import input.SpreadsheetLoader
import input.SpreadsheetWriter
import java.io.FileOutputStream
import java.io.OutputStream
import model.Executive
import model._

object FileManager {

  def load[T](name: String)(action: InputStream => T) = {
    import Closeables._
    new FileInputStream(name).processWith {
      x => action(x)
    }
  }
  
  def write[T](name: String)(action: OutputStream => T) = {
    import Closeables._
    new FileOutputStream(name).processWith {
      x => action(x)
    }
  }

  def loadSpreadsheet(name: String) = {
    load(name) { x =>
      SpreadsheetLoader.load(x)
    }
  }
  
  def generateNewFileWith(name: String, company: CompanyFiscalYear) = {
    write(name) { x => 
      SpreadsheetWriter.write(x, company)
    }
  }

}