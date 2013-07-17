package controllers

import java.util.zip._
import scala.collection.JavaConversions._
import libt.spreadsheet.reader.workflow._
import libt.error._
import libt._

/**Trait that provides behavior for reading zipped spreadsheet sets */
trait WorkbookZipReader {

  //TODO remove conversational state
  var file : ZipFile = _

  /**answers a seq of file names and read results*/
  //TODO use monadic validated error hadndling
  def readZipFileEntries(filePath: String, readers: Seq[(FrontPhase[Seq[Model]], String)]) : Seq[(String, Validated[Seq[Model]])] = {
    try {
      file = new ZipFile(filePath)
      readZipFile(getValidEntries(readers))
    } catch {
      case e => Seq(filePath -> Invalid(e.getMessage))
    }
  }

  protected def readZipFile[A](readersWithEntries: Seq[(FrontPhase[A], ZipEntry)]) =
    readersWithEntries
      .map { case (reader, entry) => (entry.getName(), reader(file.getInputStream(entry))) }
      .toSeq

  protected def getValidEntries[A](readers: Seq[(FrontPhase[A], String)]) = {
    def suffix(entry: ZipEntry) = entry.getName().split("-").last
    def isValidEntry(validSuffix: String, entry: ZipEntry) = {
      !entry.isDirectory() && validSuffix == suffix(entry) && !entry.getName.contains("__MACOSX")
    }


    def validEntryWithReader[A](readers: Seq[(FrontPhase[A], String)], entry: ZipEntry) =
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
