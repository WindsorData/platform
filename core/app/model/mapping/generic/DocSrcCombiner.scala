package model.mapping.generic

import util.WorkbookLogger._

import libt.spreadsheet.reader.workflow._
import libt.spreadsheet.reader._
import libt.spreadsheet.util._
import libt.error._
import libt._

import scala.collection.JavaConversions._
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Workbook
import org.joda.time.DateTime

/**
 * *
 * Windsor worksheets specific combiner, that assumes that the excel file
 * has as first sheet a DOC_SRC area, which has year metadata, that should be zipped
 * with the rest of the sheets.
 *
 * For instance, when a DOC_SRC states that there are three years  - 2008, 2009, and 2010 -
 * then this combiner will look for up to 3 other sheets with data, and zip them with those years
 *
 * @author mcorbanini
 */
class DocSrcCombiner(
    yearsPositionWithKeys: Seq[(Int, Symbol, Seq[Model] => Element)])
    extends Phase[Seq[Seq[Model]], Seq[Model]] {

  def dateCellToYear(r: Seq[Row]): Validated[Int] = {
    val dateCell = r.get(0).getCell(2)
    try {
      Valid(new DateTime(blankToNone(_.getDateCellValue)(dateCell).get).getYear())
    } catch {
      case e: NoSuchElementException =>
        Invalid(ReaderError().noFiscalYearProvidedAt(dateCell))
      case e: RuntimeException =>
        Invalid(ReaderError(e.getMessage()).description(dateCell))
    }
  }

  def years(sheet: Sheet) =
    yearsPositionWithKeys.map {
      case (yearIndex, key, elemWrap) => (dateCellToYear(sheet.rows.drop(yearIndex)), key, elemWrap)
    }

  override def apply(wb: Workbook, results: Seq[Seq[Model]]): Validated[Seq[Model]] = {
    val yearsWithKeys = years(wb.getSheetAt(0)).filter(!_._1.isInvalid)
    yearsWithKeys.concatMap(_._1) andThen {
      (yearsWithKeys, results.tail, Stream.continually(results.head.head)).zipped.map {
        case ((year, key, elemWrap), executives, company) =>
          year.map { it =>
            Model(company.elements
              + ('disclosureFiscalYear -> Value(it))
              + (key -> elemWrap(executives)))
          }
      }.concat
    }
  }
}

object DocSrcCombiner {
  def apply(years: (Int, Symbol, Seq[Model] => Element)*) = new DocSrcCombiner(years.toSeq)
}