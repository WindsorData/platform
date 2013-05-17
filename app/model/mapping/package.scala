package model

import util.ErrorHandler._
import util.WorkbookLogger._
import libt._
import libt.spreadsheet._
import libt.spreadsheet.reader._
import scala.collection.immutable.Stream
import org.apache.poi.ss.usermodel.Row
import org.joda.time.DateTime
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Workbook

package object mapping {

  implicit def pathToFeature(path: Path): Feature = Feature(path)

  def colOfModelsPath(basePath: Symbol, times: Int, paths: Symbol*): Seq[Strip] =
    for (index <- 0 to times; valuePath <- paths) yield Feature(Path(basePath, index, valuePath))

  val executiveMapping = 
    Seq[Strip](Path('firstName),
      Path('lastName),
      Path('title),
      Path('functionalMatches, 'primary),
      Path('functionalMatches, 'secondary),
      Path('functionalMatches, 'level),
      Path('functionalMatches, 'scope),
      Path('functionalMatches, 'bod),
      Path('founder),
      Path('transitionPeriod),

      Path('cashCompensations, 'baseSalary),
      Path('cashCompensations, 'actualBonus),
      Path('cashCompensations, 'retentionBonus),
      Path('cashCompensations, 'signOnBonus),
      Path('cashCompensations, 'targetBonus),
      Path('cashCompensations, 'thresholdBonus),
      Path('cashCompensations, 'maxBonus),
      Path('cashCompensations, 'nextFiscalYearData, 'baseSalary),
      Path('cashCompensations, 'nextFiscalYearData, 'targetBonus)) ++
      colOfModelsPath('optionGrants, 5, 'grantDate, 'expireDate, 'number, 'price, 'value, 'perf, 'type) ++
      colOfModelsPath('timeVestRS, 5, 'grantDate, 'number, 'price, 'value, 'type) ++
      colOfModelsPath('performanceVestRS, 2, 'grantDate, 'targetNumber, 'grantDatePrice, 'targetValue, 'type) ++
      colOfModelsPath('performanceCash, 2, 'grantDate, 'targetValue, 'payout) ++
      Seq[Strip](
        Path('carriedInterest, 'ownedShares, 'beneficialOwnership),
        Path('carriedInterest, 'ownedShares, 'options),
        Path('carriedInterest, 'ownedShares, 'unvestedRestrictedStock),
        Path('carriedInterest, 'ownedShares, 'disclaimBeneficialOwnership),
        Path('carriedInterest, 'ownedShares, 'heldByTrust),
        Path('carriedInterest, 'ownedShares, 'other),
        Path('carriedInterest, 'outstandingEquityAwards, 'vestedOptions),
        Path('carriedInterest, 'outstandingEquityAwards, 'unvestedOptions),
        Path('carriedInterest, 'outstandingEquityAwards, 'timeVestRS),
        Path('carriedInterest, 'outstandingEquityAwards, 'perfVestRS))

  class CompanyFiscalYearCombiner extends Combiner[Seq[ModelOrErrors]] {
    import scala.collection.JavaConversions._
    import libt.spreadsheet.util._

    val ROW_INDEX_FISCAL_YEAR = 25
    val ROW_INDEX_FISCAL_YEAR_MINUS_ONE = 40
    val ROW_INDEX_FISCAL_YEAR_MINUS_TWO = 55

    def dateCellToYear(r: Seq[Row]) = {
      val dateCell = r.get(0).getCell(2)
      try {
        Right(new DateTime(blankToNone(_.getDateCellValue)(dateCell).get).getYear())
      } catch {
        case e: NoSuchElementException =>
          Left(log(ReaderError().noFiscalYearProvidedAt(dateCell)))
        case e: RuntimeException =>
          Left(log(ReaderError(e.getMessage()).description(dateCell)))
      }
    }

    def years(sheet: Sheet) =
      Seq(
        ROW_INDEX_FISCAL_YEAR,
        ROW_INDEX_FISCAL_YEAR_MINUS_ONE,
        ROW_INDEX_FISCAL_YEAR_MINUS_TWO)
        .map(it => dateCellToYear(sheet.rows.drop(it)))

    def combineReadResult(wb: Workbook, results: Seq[Seq[ModelOrErrors]]) = {
      val ys = years(wb.getSheetAt(0))
      if (ys.hasErrors || results.exists(_.hasErrors)) {
    	  results.flatten :+ Left(ys.errors.map(error => error.left.get))
      }
      else {
        (ys, results.tail, Stream.continually(results.head.head)).zipped
          .map((year, executives, company) =>
            Right(Model(company.right.get.elements
              + ('disclosureFiscalYear -> Value(year.right.get))
              + ('executives -> Col(executives.map(_.right.get).toList: _*)))))
      }
    }
  }

  val CompanyFiscalYearReader = new WorkbookReader(
    WorkbookMapping(
      Area(TCompanyFiscalYear, Offset(2, 2), None,RowOrientedLayout, Seq(Feature(Path('ticker)), Feature(Path('name))))
        #::
        AreaGap
        #::
        Stream.continually[SheetDefinition](Area(TExecutive, Offset(3, 1), Some(5), ColumnOrientedLayout, executiveMapping))),
    new CompanyFiscalYearCombiner)

}