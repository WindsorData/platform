package controllers

import java.util.zip._

import scala.collection.JavaConversions._

import libt.spreadsheet.reader._
import libt._

trait WorkbookZipReader[A] {
  val suffix: String
  val reader : WorkbookReader[A]

  def readZipFileEntries(filePath: String) = {
    val file = new ZipFile(filePath)
    readZipFile(file, getValidEntries(file).toSeq)
  }
  
  def readZipFile(file: ZipFile, entries: Seq[ZipEntry]) =
    entries
      .map { entry => (entry.getName(), reader.read(file.getInputStream(entry))) }
      .toSeq

  def getValidEntries(file: ZipFile) =
    file
      .entries
      .map { entry => file.getEntry(entry.getName()) }
      .filter { entry => !entry.isDirectory() && entry.getName().split("-").last == suffix }
}