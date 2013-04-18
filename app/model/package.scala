import libt._
import libt.spreadsheet._
import input._
import scala.collection.immutable.Stream
import org.openqa.selenium.firefox.internal.Streams
import org.apache.poi.ss.usermodel.Row
import org.joda.time.DateTime
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Workbook
package object model {
  import persistence._

  val grantTypes = TEnum("Annual", "Retention", "Hire", "Promotion", "Special", "Acquisition", "Other")

  val validPrimaryValues = TEnum("CEO (Chief Executive Officer)",
    "CFO (Chief Financial Officer)",
    "COO (Chief Operating Officer)",
    "Sales",
    "Bus Dev (Business Development)",
    "CAO (Chief Admin Officer)",
    "GC (General Counsel-Legal)",
    "CAO (Chief Accounting Officer)",
    "CIO (Chief Investment-Asset Officer)",
    "CTO (Chief Technology Officer)",
    "Manufacturing",
    "Engineering",
    "Marketing",
    "CSO (Chief Science Officer)",
    "CSO (Chief Strategic Officer)",
    "CIO (Chief Information Officer)",
    "Product",
    "CRO (Chief Risk Officer)",
    "Treasurer/Secretary",
    "Executive Chairman",
    "Other")

  val validSecondaryValues = TEnum(
    "Sales",
    "Bus Dev (Business Development)",
    "CAO (Chief Admin Officer)",
    "GC (General Counsel-Legal)",
    "CAO (Chief Accounting Officer)",
    "CIO (Chief Investment-Asset Officer)",
    "CTO (Chief Technology Officer)",
    "Manufacturing",
    "Engineering",
    "Marketing",
    "CSO (Chief Science Officer)",
    "CSO (Chief Strategic Officer)",
    "CIO (Chief Information Officer)",
    "Product",
    "CRO (Chief Risk Officer)",
    "Treasurer/Secretary",
    "GM (General Manager)",
    "Other")

  val validLevelValues = TEnum(
    "President",
    "EVP (Executive Vice President)",
    "SVP (Senior Vice President)",
    "VP (Vice President)",
    "GM (General Manager)",
    "Group President")

  val validScopeValues = TEnum(
    "WW/Global/International",
    "US",
    "North America",
    "Europe",
    "Asia",
    "Americas")

  val validBodValues = TEnum("Chairman",
    "Vice Chairman",
    "Director")

  val TExecutive = TModel(
    'firstName -> TString,
    'lastName -> TString,
    'title -> TString,
    'functionalMatches ->
      TModel(
        'primary -> validPrimaryValues,
        'secondary -> validSecondaryValues,
        'level -> validLevelValues,
        'scope -> validScopeValues,
        'bod -> validBodValues),
    'founder -> TString,
    'transitionPeriod -> TString,

    'cashCompensations -> TModel(
      'baseSalary -> TNumber,
      'actualBonus -> TNumber,
      'retentionBonus -> TNumber,
      'signOnBonus -> TNumber,
      'targetBonus -> TNumber,
      'thresholdBonus -> TNumber,
      'maxBonus -> TNumber,
      'nextFiscalYearData -> TModel(
        'baseSalary -> TNumber,
        'targetBonus -> TNumber)),

    'optionGrants -> TCol(
      TModel(
        'grantDate -> TDate,
        'expireDate -> TDate,
        'number -> TNumber,
        'price -> TNumber,
        'value -> TNumber,
        'perf -> TXBool,
        'type -> grantTypes)),

    'timeVestRS -> TCol(
      TModel(
        'grantDate -> TDate,
        'number -> TNumber,
        'price -> TNumber,
        'value -> TNumber,
        'type -> grantTypes)),

    'performanceVestRS -> TCol(
      TModel(
        'grantDate -> TDate,
        'targetNumber -> TNumber,
        'grantDatePrice -> TDate,
        'targetValue -> TNumber,
        'type -> grantTypes)),

    'performanceCash -> TCol(
      TModel(
        'grantDate -> TDate,
        'targetValue -> TNumber,
        'payout -> TNumber)),

    'carriedInterest -> TModel(
      'ownedShares -> TModel(
        'beneficialOwnership -> TNumber,
        'options -> TNumber,
        'unvestedRestrictedStock -> TNumber,
        'disclaimBeneficialOwnership -> TNumber,
        'heldByTrust -> TNumber,
        'other -> TString),
      'outstandingEquityAwards -> TModel(
        'vestedOptions -> TNumber,
        'unvestedOptions -> TNumber,
        'timeVestRS -> TNumber,
        'perfVestRS -> TNumber)))

  val TCompanyFiscalYear = TModel(
    'ticker -> TString,
    'name -> TString,
    'disclosureFiscalYear -> TInt,

    'executives -> TCol(TExecutive))

  implicit def pathToFeature(path: Path): Feature = Feature(path)

  def colOfModelsPath(basePath: Symbol, times: Int, paths: Symbol*): Seq[Column] =
    for (index <- 0 to times; valuePath <- paths) yield Feature(Path(basePath, index, valuePath))

  val executiveMapping = Mapping(
    Seq[Column](Path('firstName),
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
      Path('cashCompensations, 'nextFiscalYearData, 'targetBonus))
      ++
      colOfModelsPath('optionGrants, 5, 'grantDate, 'expireDate, 'number, 'price, 'value, 'perf, 'type)
      ++
      colOfModelsPath('timeVestRS, 5, 'grantDate, 'number, 'price, 'value, 'type)
      ++
      colOfModelsPath('performanceVestRS, 2, 'grantDate, 'targetNumber, 'grantDatePrice, 'targetValue, 'type)
      ++
      colOfModelsPath('performanceCash, 2, 'grantDate, 'targetValue, 'payout)
      ++
      Seq[Column](
        Path('carriedInterest, 'ownedShares, 'beneficialOwnership),
        Path('carriedInterest, 'ownedShares, 'options),
        Path('carriedInterest, 'ownedShares, 'unvestedRestrictedStock),
        Path('carriedInterest, 'ownedShares, 'disclaimBeneficialOwnership),
        Path('carriedInterest, 'ownedShares, 'heldByTrust),
        Path('carriedInterest, 'ownedShares, 'other),
        Path('carriedInterest, 'outstandingEquityAwards, 'vestedOptions),
        Path('carriedInterest, 'outstandingEquityAwards, 'unvestedOptions),
        Path('carriedInterest, 'outstandingEquityAwards, 'timeVestRS),
        Path('carriedInterest, 'outstandingEquityAwards, 'perfVestRS)): _*)

  class CompanyFiscalYearCombiner extends Combiner[Seq[Model]] {
    import scala.collection.JavaConversions._
    import libt.spreadsheet.util._

    val ROW_INDEX_FISCAL_YEAR = 27
    val ROW_INDEX_FISCAL_YEAR_MINUS_ONE = 42
    val ROW_INDEX_FISCAL_YEAR_MINUS_TWO = 57

    def dateCellToYear(r: Seq[Row]) = {
      val dateCell = r.get(0).getCell(2)
      try {
        Some(new DateTime(blankToNone(_.getDateCellValue)(dateCell).get).getYear())
      } catch {
        case e: NoSuchElementException => throw new NoSuchElementException(noFiscalYearErrorMessage(dateCell))
        case e: RuntimeException => throw new IllegalStateException(invalidCellTypeErrorMessage(e.getMessage(), dateCell))
      }
    }

    def years(sheet: Sheet) =
      Seq(
        ROW_INDEX_FISCAL_YEAR,
        ROW_INDEX_FISCAL_YEAR_MINUS_ONE,
        ROW_INDEX_FISCAL_YEAR_MINUS_TWO)
        .map(it => dateCellToYear(sheet.rows.drop(it)))

    private def invalidCellTypeErrorMessage(baseMessage: String, cell: Cell) =
      baseMessage +
        " on Sheet: " + cell.getSheet().getSheetName +
        " -> Column: " + { cell.getColumnIndex + 1 } +
        ", Row: " + { cell.getRowIndex + 1 }

    private def noFiscalYearErrorMessage(cell: Cell) =
      "No Fiscal Year provided at Sheet " +
        cell.getSheet.getSheetName +
        " Column: " + cell.getColumnIndex +
        " Row: " + cell.getRowIndex

    def combineReadResult(wb: Workbook, results: Seq[Seq[Model]]) = {
      val ys = years(wb.getSheetAt(0))
      (ys, results.tail, Stream.continually(results.head.head)).zipped
        .map((year, executives, company) =>
          Model(company.elements + ('disclosureFiscalYear -> Value(year.get)) + ('executives -> Col(executives: _*))))
    }
  }

  val CompanyFiscalYearReader = new WorkbookReader(
    WorkbookMapping(
      Area(TCompanyFiscalYear, Offset(2, 2), RowOrientation, Mapping(Feature(Path('ticker)), Feature(Path('name))))
        #::
        AreaGap
        #::
        Stream.continually[SheetDefinition](Area(TExecutive, Offset(3, 1), ColumnOrientation, executiveMapping))),
    new CompanyFiscalYearCombiner)

}








