package output.writers

import libt._
import libt.spreadsheet._
import libt.spreadsheet.reader._
import libt.spreadsheet.writer._

import output.writers.generic.OutputWriter
import output.ReportBuilder
import output.PeersWriteStrategy

import model.PeerCompanies._
import model.mapping.peers._

import org.apache.poi.ss.usermodel.Workbook

case class PeersWriter(reportBuilder: ReportBuilder) extends OutputWriter {
  val schema = TPeers
  val fileName = reportBuilder.fileName

  def peersArea(writeStrategy: PeersWriteStrategy) =
    CustomWriteArea(
      schema = schema,
      offset = Offset(1, 0),
      limit = None,
      orientation = ColumnOrientedLayout(RawValueReader),
      columns = peersMapping,
      writeStrategy = writeStrategy)

  def write(out: Workbook, models: Seq[Model], yearRange: Int = 0): Unit =
    WorkbookMapping(reportBuilder.defineWriters(this)).write(models, out)
}