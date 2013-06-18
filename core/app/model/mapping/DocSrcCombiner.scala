package model.mapping

import util.WorkbookLogger._
import libt.spreadsheet.reader._
import libt.spreadsheet.util._
import libt.spreadsheet._
import libt.error._
import libt.workflow._
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
    yearsPositionWithKeys: Seq[(Int, Symbol, Seq[Validated[Model]] => Element)]) 
    extends Phase[Seq[Seq[Validated[Model]]], Seq[Validated[Model]]] {

  def dateCellToYear(r: Seq[Row]): Validated[Int] = {
    val dateCell = r.get(0).getCell(2)
    try {
      Valid(new DateTime(blankToNone(_.getDateCellValue)(dateCell).get).getYear())
    } catch {
      case e: NoSuchElementException =>
        Invalid(log(ReaderError().noFiscalYearProvidedAt(dateCell)))
      case e: RuntimeException =>
        Invalid(log(ReaderError(e.getMessage()).description(dateCell)))
    }
  }

  def years(sheet: Sheet) =
    yearsPositionWithKeys.map {
      case (yearIndex, key, elemWrap) => (dateCellToYear(sheet.rows.drop(yearIndex)), key, elemWrap)
    }

  def apply(wb: Workbook, results: Seq[Seq[Validated[Model]]]) = {
    val flattenResults = results.flatten
    val yearsWithKeys = years(wb.getSheetAt(0))
    (Validated.concat(yearsWithKeys.map(_._1)), Validated.concat(flattenResults)) match {
      case (i: Invalid, _) => Seq(i)
      case (_, i: Invalid) => flattenResults
      case _ => (yearsWithKeys, results.tail, Stream.continually(results.head.head)).zipped
        .map {
          case ((year, key, elemWrap), executives, company) =>
            Valid(Model(company.get.elements
              + ('disclosureFiscalYear -> Value(year.get))
              + (key -> elemWrap(executives))))
        }
    }
  }
}

object DocSrcCombiner {
  def apply(years: (Int, Symbol, Seq[Validated[Model]] => Element)*) = new DocSrcCombiner(years.toSeq)
}