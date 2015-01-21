package windsor.output.writers.generic

import libt._
import libt.spreadsheet.Gap
import libt.spreadsheet.reader.Area
import libt.spreadsheet.Offset
import libt.spreadsheet.reader.WorkbookMapping
import libt.spreadsheet.writer.CustomWriteSheetDefinition
import libt.spreadsheet.writer.WriteStrategy
import libt.spreadsheet.reader.ColumnOrientedLayout
import libt.spreadsheet.reader.RawValueReader

import model.TCompanyFiscalYear

import mapping.DilutionMappingComponent
import mapping.DocSrcMappingComponent
import mapping.Top5MappingComponent
import mapping.GuidelinesMappingComponent

import windsor.output.MetadataAreaLayout
import windsor.output.ValueAreaLayout

import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook

abstract class Top5Writer extends OutputWriter {
  self : DilutionMappingComponent
    with DocSrcMappingComponent
    with Top5MappingComponent
    with GuidelinesMappingComponent =>

  val schema = TCompanyFiscalYear
  val fileName = "EmptyStandardOutputTemplate.xls"

  def execDBArea(range: Int, yearOffset: Option[Int]) =
    outputArea(
      ValueAreaLayout(Offset(6, 2)),
      executiveMapping,
      Path('lastName),
      Path('executives, *),
      ExecutivesWriteStrategy(range, yearOffset))

  def stBonusPlanArea =
    outputArea(
      ValueAreaLayout(Offset(6, 2)),
      stBonusPlanMapping,
      Path('lastName),
      Path('stBonusPlan, *),
      LastYearWriteStrategy)

  def executivesGuidelinesArea =
    outputArea(
      ValueAreaLayout(Offset(6, 2)),
      guidelinesMapping,
      Path('lastName),
      Path('guidelines, *),
      LastYearWriteStrategy)


  def companyDBArea =
    outputArea(
      ValueAreaLayout(Offset(6, 2)),
      Seq(Gap) ++
      usageAndSVTDataMapping ++
      Seq(Gap) ++
      bsInputsMapping ++
      Seq(Gap) ++
      dilutionMapping,
      Path(),
      Path('companyDB, *),
      LastYearWriteStrategy)

  def companyDBMetadataArea =
    outputArea(
      MetadataAreaLayout(Offset(1, 0)),
      usageAndSVTDataMapping ++
      bsInputsMapping ++
      dilutionMapping
        .filter(_ match {
        case Gap => false
        case _ => true
      }),
      Path(),
      Path('companyDB, *),
      LastYearWriteStrategy)

  def grantTypesArea =
    outputArea(
      ValueAreaLayout(Offset(7, 2)),
      grantTypesMapping,
      Path(),
      Path('grantTypes, *),
      LastYearWriteStrategy)

  def execMetadataArea(range: Int) =
    outputArea(
      MetadataAreaLayout(Offset(1, 0)),
      executiveMapping
        .filter(_ match {
        case Gap => false
        case _ => true
      }),
      Path('lastName),
      Path('executives, *),
      ExecutivesWriteStrategy(range, None))

  def stBonusMetadataArea =
    outputArea(
      MetadataAreaLayout(Offset(1, 0)),
      stBonusPlanMapping
        .filter(_ match {
        case Gap => false
        case _ => true
      }),
      Path('lastName),
      Path('stBonusPlan, *),
      LastYearWriteStrategy)

  def executivesGuidelinesMetadataArea =
    outputArea(
      MetadataAreaLayout(Offset(1, 0)),
      guidelinesMapping
        .filter(_ match {
        case Gap => false
        case _ => true
      }),
      Path('lastName),
      Path('guidelines, *),
      LastYearWriteStrategy)

  def grantTypesMetadataArea =
    outputArea(
      MetadataAreaLayout(Offset(1, 0)),
      grantTypesMapping
        .filter(_ match {
        case Gap => false
        case _ => true
      }),
      Path(),
      Path('grantTypes, *),
      LastYearWriteStrategy)

  def docSrcArea =
    Area(schema= schema,
      offset= Offset(2,0),
      limit= None,
      orientation= ColumnOrientedLayout(RawValueReader),
      columns= docSrcMapping)


  def write(out: Workbook, companies: Seq[Model], executivesRange: Int): Unit = {
    WorkbookMapping(
      Seq(
        execDBArea(executivesRange - 1, Some(0)), //ExecDB
        execDBArea(executivesRange - 2, Some(1)), //ExecDB -1
        execDBArea(executivesRange - 3, Some(2)), //ExecDB -2
        execMetadataArea(executivesRange),
        stBonusPlanArea,
        stBonusMetadataArea,
        executivesGuidelinesArea,
        executivesGuidelinesMetadataArea,
        companyDBArea,
        companyDBMetadataArea,
        grantTypesArea,
        grantTypesMetadataArea,
        docSrcArea
      )).write(companies, out)
  }

  /**
    * [[libt.spreadsheet.writer.WriteStrategy]] that writes TCompanyFiscalYears for a given yearOffset
    * @param range
    * @param yearOffset the negative year offset (0 is last year, 1 is previous year, and so on)
    */
  case class ExecutivesWriteStrategy(/*TODO remove*/range: Int, yearOffset: Option[Int]) extends WriteStrategy {
    def modelsForCurrentYear(models: Seq[Seq[Model]], year: Int): Seq[Model] =
      models.flatMap(it => if(it.size > year) Some(it(year)) else None)

    def write(models: Seq[Model], area: CustomWriteSheetDefinition, sheet: Sheet) = {
      if (range >= 0) {
        val validModels = ModelGrouper(models).map(_._2)
        yearOffset match {
          case Some(pos) => area.customWrite(modelsForCurrentYear(validModels, pos), sheet)
        	case None => area.customWrite(validModels.flatten, sheet)
        }
      }
    }
  }

  /**
    * [[libt.spreadsheet.writer.WriteStrategy]] that writes only TCompanyFiscalYears for the last year
    */
  object LastYearWriteStrategy extends WriteStrategy {
    def write(models: Seq[Model], area: CustomWriteSheetDefinition, sheet: Sheet) {
      val validModels = ModelGrouper(models).map(_._2.head)
      area.customWrite(validModels, sheet)
    }
  }

  /**
   * Group models by cusip and these are ordered by disclosureFiscalYear (descending)
   */
  object ModelGrouper {
    def apply(models: Seq[Model]) : Seq[(String, Seq[Model])] = {
      def Desc[T: Ordering] = implicitly[Ordering[T]].reverse
      models
        .groupBy(_ /!/ 'cusip)
        .map { case (ticker, ms) => ticker -> ms.sortBy(_ /#/ 'disclosureFiscalYear)(Desc) }
        .toSeq
    }
  }

}