package model

import util.WorkbookLogger._
import libt.spreadsheet.reader._
import libt.spreadsheet._
import libt.error._
import libt._

import org.joda.time.DateTime

import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Workbook
import model.ExecutivesTop5._
import model.ExecutivesGuidelines._

package object mapping {

  implicit def pathToFeature(path: Path): Feature = Feature(path)

  def colOfModelsPath(basePath: Path, times: Int, paths: Symbol*): Seq[Strip] =
    for (index <- 0 to times; valuePath <- paths) yield Feature((basePath :+ Index(index)) :+ Route(valuePath))

  class DocSrcCombiner(yearsPositionWithKeys: Seq[(Int, Symbol)]) extends Combiner[Seq[ModelOrErrors]] {
    import scala.collection.JavaConversions._
    import libt.spreadsheet.util._

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
        case (yearIndex, key) => (dateCellToYear(sheet.rows.drop(yearIndex)), key)
      }

    def combineReadResult(wb: Workbook, results: Seq[Seq[ModelOrErrors]]) = {
      val flattenResults = results.flatten
      val yearsWithKeys = years(wb.getSheetAt(0))

      if (yearsWithKeys.map(_._1).hasErrors || flattenResults.hasErrors) {
        flattenResults :+ Invalid(yearsWithKeys.map(_._1).errors: _*)
      } else {
        (yearsWithKeys, results.tail, Stream.continually(results.head.head)).zipped
          .map((yearWithKey, executives, company) =>
            Valid(Model(company.get.elements
              + ('disclosureFiscalYear -> Value(yearWithKey._1.get))
              + (yearWithKey._2 -> Col(executives.map(_.get).toList: _*)))))
      }
    }
  }

  object DocSrcCombiner {
    def apply(years: (Int, Symbol)*) = new DocSrcCombiner(years.toSeq)
  }
  
}