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

import Closeables._

object FileManager {

  def loadResource[T](name: String)(action: InputStream => T) = {
    val stream = Thread.currentThread().getContextClassLoader.getResourceAsStream(name)
    assert(stream != null, s"No resource for $name")
    stream.processWith(action)
  }

  def loadFile[T](fileName: String)(action: InputStream => T) =
    new FileInputStream(fileName).processWith(action)

  implicit def reader2RichReader[A](self: InputWorkflow[A]) = new {
    def readResource(filePath: String): A = loadResource(filePath)(self(_))
    def readFile(fileName: String): A = loadFile(fileName)(self(_))
  }
}