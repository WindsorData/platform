package controllers

import java.util.zip._

import scala.collection.JavaConversions._

import libt.spreadsheet.reader._
import libt._

/**Trait that provides behavior for reading zipped spreadsheet sets */
trait WorkbookZipReader {
  
  var file : ZipFile = _

  /**answers a seq of file names and read results*/
  def readZipFileEntries[A](filePath: String, readers: Seq[(WorkbookReader[A], String)]) = {
    file = new ZipFile(filePath)
    readZipFile(getValidEntries(readers))
  }

  protected def readZipFile[A](readersWithEntries: Seq[(WorkbookReader[A], ZipEntry)]) =
    readersWithEntries
      .map { case (reader, entry) => (entry.getName(), reader.read(file.getInputStream(entry))) }
      .toSeq

  protected def getValidEntries[A](readers: Seq[(WorkbookReader[A], String)]) = {
    def suffix(entry: ZipEntry) = entry.getName().split("-").last
    def isValidEntry(validSuffix: String, entry: ZipEntry) = !entry.isDirectory() && validSuffix == suffix(entry)

    def validEntryWithReader[A](readers: Seq[(WorkbookReader[A], String)], entry: ZipEntry) =
      readers
        .collectFirst {
          case (reader, validSuffix) if isValidEntry(validSuffix, entry) => (reader, entry)
        }

    file
      .entries
      .map(validEntryWithReader(readers, _))
      .toSeq.flatten
  }
}