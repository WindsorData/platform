package util

import java.io.InputStream
import java.io.FileInputStream
import input.SpreadsheetWriter
import java.io.FileOutputStream
import java.io.OutputStream
import model._
import libt.Model
import libt.spreadsheet.reader.WorkbookReader
import libt.spreadsheet.reader.WorkbookReader

object FileManager {

  def load[T](name: String)(action: InputStream => T) = {
    import Closeables._
    new FileInputStream(name).processWith {
      x => action(x)
    }
  }
  
  implicit def reader2RichReader[A](wb: WorkbookReader[A]) = new {
    def read(filePath: String): A =
    load(filePath) { x =>
      wb.read(x)
    }
  }
  
  def write[T](name: String)(action: OutputStream => T) = {
    import Closeables._
    new FileOutputStream(name).processWith {
      x => action(x)
    }
  }

  def generateNewFileWith(name: String, company: Model) = {
    write(name) { x => 
      SpreadsheetWriter.write(x, Seq(company))
    }
  }

}