package util

import java.io.InputStream
import java.io.FileInputStream
import output.SpreadsheetWriter
import java.io.FileOutputStream
import java.io.OutputStream
import model._
import libt.Model
import libt.workflow._
import output.SpreadsheetWriter


object FileManager {

  def load[T](name: String)(action: InputStream => T) = {
    import Closeables._
    new FileInputStream(name).processWith {
      x => action(x)
    }
  }
  
  implicit def reader2RichReader[A](wb: InputWorkflow[A]) = new {
    def read(filePath: String): A =
    load(filePath) { in =>
      wb(in)
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