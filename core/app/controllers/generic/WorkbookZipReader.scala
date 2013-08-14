package controllers.generic

import java.util.zip._
import scala.collection.JavaConversions._
import libt.spreadsheet.reader.workflow._
import libt.error._
import libt._
import scala.Predef._

/**Trait that provides behavior for reading zipped spreadsheet sets */
trait WorkbookZipReader {

  type FileAndTicker = (String, String)

  //TODO use monadic validated error hadndling
  def readZipFileEntries(filePath: String, readers: Seq[EntryReader]): Seq[(FileAndTicker, Validated[Seq[Model]])] = readZipFile(new ZipFile(filePath), readers)

  protected def readZipFile[A](file: ZipFile, readers: Seq[EntryReader]) =
    readersWithEntries(file, readers)
      .map { case (reader, entry) => {
        val tickerName = entry.getName.split("/").last.split("-").applyOrElse(0, "Unknown").asInstanceOf[String]
        (entry.getName() -> tickerName, reader(file.getInputStream(entry)))
      } }
      .toSeq

  protected def readersWithEntries[A](file: ZipFile, entryReaders: Seq[EntryReader]) = {
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
