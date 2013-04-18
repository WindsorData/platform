package input

import java.io.FileInputStream
import java.io.InputStream
import util.Closeables
import util._
import org.scalatest.BeforeAndAfterAll
import org.scalatest.Suite
import org.scalatest.BeforeAndAfter

trait TestSpreadsheetLoader  {
  def load[T](name: String)(action: InputStream => T) = FileManager.load("test/input/" + name)(action)
  
}