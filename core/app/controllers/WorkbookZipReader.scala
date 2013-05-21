package controllers

import java.util.zip._

import scala.collection.JavaConversions._

import libt.spreadsheet.reader._
import libt._

/**Trait that provides behavior for reading zipped spreadsheet sets */
trait WorkbookZipReader[A] {
  /**TODO semantics not clear*/
  val suffix: String
  /**the reader used to parse each spreadsheet*/
  val reader : WorkbookReader[A]

  /**answers a seq of file names and read results*/
  def readZipFileEntries(filePath: String) = {
    val file = new ZipFile(filePath)
    readZipFile(file, getValidEntries(file).toSeq)
  }
  
  protected def readZipFile(file: ZipFile, entries: Seq[ZipEntry]) =
    entries
      .map { entry => (entry.getName(), reader.read(file.getInputStream(entry))) }
      .toSeq

  protected def getValidEntries(file: ZipFile) =
    file
      .entries
      .map { entry => file.getEntry(entry.getName()) }
      .filter { entry => !entry.isDirectory() && entry.getName().split("-").last == suffix }
}