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

  def colOfModelsPath(basePath: Symbol, times: Int, paths: Symbol*): Seq[Strip] =
    for (index <- 0 to times; valuePath <- paths) yield Feature(Path(basePath, index, valuePath))

  class DocSrcCombiner(yearsIndexes: Seq[Int], toCombine: Symbol) extends Combiner[Seq[ModelOrErrors]] {
    import scala.collection.JavaConversions._
    import libt.spreadsheet.util._

    val ROW_INDEX_FISCAL_YEAR = 25
    val ROW_INDEX_FISCAL_YEAR_MINUS_ONE = 40
    val ROW_INDEX_FISCAL_YEAR_MINUS_TWO = 55

    def dateCellToYear(r: Seq[Row]) : Validated[Int] = {
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
      yearsIndexes.map(it => dateCellToYear(sheet.rows.drop(it)))

    def combineReadResult(wb: Workbook, results: Seq[Seq[ModelOrErrors]]) = {
      val flattenResults = results.flatten
      val ys = years(wb.getSheetAt(0))
      
      if (ys.hasErrors || flattenResults.hasErrors) {
    	  flattenResults :+ Invalid(ys.errors : _*)
      }
      else {
        (ys, results.tail, Stream.continually(results.head.head)).zipped
          .map((year, executives, company) =>
            Valid(Model(company.get.elements
              + ('disclosureFiscalYear -> Value(year.get))
              + ('executives -> Col(executives.map(_.get).toList: _*)))))
      }
    }
  }

  def companyFiscalYearCombiner = new DocSrcCombiner(Seq(25,40,55), 'executives)
  def execGuidelinesCombiner = new DocSrcCombiner(Seq(10), 'guidelines)

}