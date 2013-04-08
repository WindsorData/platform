package util

import java.io.InputStream
import java.io.FileInputStream
import input.SpreadsheetReader
import input.SpreadsheetWriter
import java.io.FileOutputStream
import java.io.OutputStream
import model._
import libt.Model

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
      SpreadsheetReader.read(x)
    }
  }
  
  def generateNewFileWith(name: String, company: Model) = {
    write(name) { x => 
      SpreadsheetWriter.write(x, Seq(company))
    }
  }

}