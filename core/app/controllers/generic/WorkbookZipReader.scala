package controllers.generic

import java.util.zip._
import scala.collection.JavaConversions._
import libt.spreadsheet.reader.workflow._
import libt.error._
import libt._
import scala.Predef._

/**Trait that provides behavior for reading zipped spreadsheet sets */
trait WorkbookZipReader {

  val entryReaders : Seq[EntryReader]

  //TODO use monadic validated error hadndling
  def readZipFileEntries(filePath: String): Seq[(String, Validated[Seq[Model]])] = {
    try {
      readZipFile(new ZipFile(filePath))
    } catch {
      case e : Exception => Seq(filePath -> Invalid(e.getMessage))
    }
  }

  protected def readZipFile[A](file: ZipFile) =
    readersWithEntries(file)
      .map { case (reader, entry) => (entry.getName(), reader(file.getInputStream(entry))) }
      .toSeq

  protected def readersWithEntries[A](file: ZipFile) = {
    def validEntryWithReader[A](entry: ZipEntry) =
      entryReaders.collectFirst { case e if e.canRead(entry) => (e.workflow, entry) }

    file
      .entries
      .map(validEntryWithReader)
      .toSeq.flatten
  }

  case class EntryReader(workflow: FrontPhase[Seq[Model]], suffix: String) {
    def canRead(entry: ZipEntry) = {
      !entry.isDirectory() && suffix == suffixOf(entry) && !entry.getName.contains("__MACOSX")
    }

    private def suffixOf(entry: ZipEntry) = entry.getName().split("-").last
  }
}
