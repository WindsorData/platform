package util

import libt.spreadsheet.reader.workflow._
import libt.error._

import java.io.InputStream
import java.io.FileInputStream

import Closeables._

object FileManager {

  def loadResource[T](name: String)(action: InputStream => T) = {
    val stream = Thread.currentThread().getContextClassLoader.getResourceAsStream(name)
    assert(stream != null, s"No resource for $name")
    stream.processWith(action)
  }

  def loadFile[T](fileName: String)(action: InputStream => T) =
    new FileInputStream(fileName).processWith(action)

  implicit def reader2RichReader[A](self: FrontPhase[A]) = new {
    def readResource(filePath: String): Validated[A] = loadResource(filePath)(self(_))
    def readFile(fileName: String): Validated[A] = loadFile(fileName)(self(_))
  }
}