package output
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.usermodel.Sheet
import java.io.OutputStream
import org.apache.poi.ss.usermodel.Workbook
import model._
import util.FileManager
import libt.util._
import libt._
import libt.spreadsheet._
import libt.spreadsheet.util._
import libt.spreadsheet.reader._
import libt.reduction._

object SpreadsheetWriter {

  val outputMapping = Seq(
    Gap,
    //Exec Data
    Feature('lastName),
    Feature('title),
    Gap, //Leave Blank
    Feature('functionalMatches, 'primary),
    Feature('founder),
    Gap, //TTDC Calculation

    //Cash Compensation
    //Current Year
    Gap,
    Feature('cashCompensations, 'baseSalary),
    Feature('cashCompensations, 'actualBonus),
    Gap, //TTDC Calculation
    Feature('cashCompensations, 'targetBonus),
    Feature('cashCompensations, 'thresholdBonus),
    Feature('cashCompensations, 'maxBonus),
    //New 8-K Data
    Feature('cashCompensations, 'nextFiscalYearData, 'baseSalary),
    Feature('cashCompensations, 'nextFiscalYearData, 'targetBonus),

    //Equity Comp Value
    //Options Value
    Gap,
    Calc(Sum(Path('optionGrants, *, 'value))),
    Calc(Sum(Path('optionGrants, *, 'number))),
    Calc(Average(Path('optionGrants, *, 'price))),
    Gap, //Leave Blank
    //Time Vest RS
    Calc(Sum(Path('timeVestRS, *, 'value))),
    Calc(Sum(Path('timeVestRS, *, 'number))),
    Calc(Average(Path('timeVestRS, *, 'price))),
    //Perf RS
    Calc(Sum(Path('performanceVestRS, *, 'targetValue))),
    Calc(Sum(Path('performanceVestRS, *, 'targetNumber))),
    Calc(Average(Path('performanceVestRS, *, 'grantDatePrice))),
    //Perf Cash
    Calc(Sum(Path('performanceCash, *, 'targetValue))),

    //Carried Interest
    Gap,
    Calc(SubstractAll(
      Path('carriedInterest, 'ownedShares),
      Path('beneficialOwnership),
      Path('options),
      Path('unvestedRestrictedStock),
      Path('disclaimBeneficialOwnership))),
    Feature('carriedInterest, 'outstandingEquityAwards, 'vestedOptions),
    Feature('carriedInterest, 'outstandingEquityAwards, 'unvestedOptions),
    Feature('carriedInterest, 'outstandingEquityAwards, 'timeVestRS),
    Feature('carriedInterest, 'outstandingEquityAwards, 'perfVestRS))

  def outputArea(layout: FlattedAreaLayout, outputMapping: Seq[Strip]) =
    FlattedArea(
      PK(Path('ticker), Path('name), Path('disclosureFiscalYear)),
      PK(Path('lastName)),
      Path('executives),
      TCompanyFiscalYear,
      layout,
      outputMapping)

  def valueArea = outputArea(ValueAreaLayout(Offset(6, 2)), outputMapping)
  def metadataArea = outputArea(MetadataAreaLayout(Offset(1, 0)), outputMapping.filter(_ match {
    case Gap => false
    case _ => true
  }))

  def write(out: Workbook, companies: Seq[Model]): Unit = {
    WorkbookMapping(Seq(valueArea, metadataArea)).write(companies, out)
  }

  def loadTemplateInto(out: OutputStream) = {
    FileManager.load("docs/external/EmptyOutputTemplate.xls") {
      x => WorkbookFactory.create(x).write(out)
    }
  }

  def write(out: OutputStream, companies: Seq[Model]): Unit = {
    FileManager.load("docs/external/EmptyOutputTemplate.xls") {
      x =>
        {
          val wb = WorkbookFactory.create(x)
          write(wb, companies)
          wb.write(out)
        }
    }
  }
}