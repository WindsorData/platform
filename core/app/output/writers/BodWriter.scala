package output.writers

import libt._
import libt.spreadsheet._
import libt.spreadsheet.reader._
import libt.spreadsheet.writer.FullWriteStrategy

import output.MetadataAreaLayout
import output.writers.generic.OutputWriter
import output.writers.generic.StandardMapping

import model.TCompanyFiscalYear
import model.ExecutivesBod.TBod
import model.mapping.bod._
import org.apache.poi.ss.usermodel.Workbook

object BodWriter extends OutputWriter with StandardMapping {
  val schema = TCompanyFiscalYear
  val schema2 = TModel(
    TBod.elementTypes ++
    TModel('ticker -> TString, 'name -> TString, 'disclosureFiscalYear -> TInt).elementTypes : _*)

  val fileName = "EmptyBodOutputTemplate.xls"

  def bodArea =
    Area(schema= schema2,
      offset= Offset(4,0),
      limit= None,
      orientation= ColumnOrientedLayout(RawValueReader),
      columns= Seq[Strip](Path('ticker),
                          Path('name),
                          Path('disclosureFiscalYear),
                          Gap, Gap, Gap, Gap, Gap) ++ bodMapping)

  def metadataArea =
    outputArea(
      MetadataAreaLayout(Offset(1, 0)),
      bodMapping,
      Path('directorData, 'group),
      Path('bod, *),
      FullWriteStrategy)

  def write(out: Workbook, models: Seq[Model], yearRange: Int): Unit = {
    WorkbookMapping(Seq(bodArea)).write(
      Model.flattenWith(
        models,
        PK(Path('ticker), Path('name), Path('disclosureFiscalYear)),
        Path('bod, *)), out)
    WorkbookMapping(Seq(AreaGap, metadataArea)).write(models, out)

  }
}