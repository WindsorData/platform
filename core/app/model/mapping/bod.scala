package model.mapping

import libt.spreadsheet.reader._
import libt.spreadsheet._
import libt.error._
import libt._
import model.ExecutivesBod._
import model._

package object bod extends WorkflowFactory {
  
	def relativeStrip(base: Path, relativeSchema: TModel) =
	  Relative(base, relativeSchema.keys.map(Path(_)): _*).map(pathToFeature)
  
    val bodMapping: Seq[Strip] =
    Seq[Strip](
      Path('directorData, 'group),
      Path('directorData, 'ceo, 'exists),
      Path('directorData, 'ceo, 'chairman),
      Path('directorData, 'numberOfDirectors, 'employee),
      Path('directorData, 'numberOfDirectors, 'nonEmployee),
      Path('meetings, 'numberOfMeetings, 'regular),
      Path('meetings, 'numberOfMeetings, 'special),
      Path('meetings, 'numberOfMeetings, 'telephonic)) ++
      relativeStrip(
          Path('meetings, 'meetingsFees),
          TMeetingsFees) ++
      relativeStrip(
          Path('meetings, 'meetingsFeesPriorValues),
          TMeetingsFees) ++
      relativeStrip(
          Path('annualRetainers, 'cashRetainer),
    	  TCashRetainer) ++
      relativeStrip(
          Path('annualRetainersPriorValues, 'cashRetainer),
    	  TCashRetainer) ++
      relativeStrip(
          Path('stockOptions, 'annual),
          TAnnual) ++
      relativeStrip(
          Path('stockOptions, 'initial),
          TInitial) ++
      relativeStrip(
          Path('stockOptionsPriorValues, 'annual),
          TAnnual) ++
      relativeStrip(
          Path('stockOptionsPriorValues, 'initial),
          TInitial) ++
      relativeStrip(
          Path('fullValues, 'annual),
          TAnnual) ++
      relativeStrip(
          Path('fullValues, 'initial),
          TInitial) ++
      relativeStrip(
          Path('fullValuesPriorValues, 'annual),
          TAnnual) ++
      relativeStrip(
          Path('fullValuesPriorValues, 'initial),
          TInitial)
      Seq[Strip](
          Path('other, 'cashDeferrals, 'toCash),
          Path('other, 'cashDeferrals, 'toStock),
          Path('ownershipGuidelines, 'achieve, 'timeTo),
          Path('ownershipGuidelines, 'achieve, 'years),
          Path('ownershipGuidelines, 'achieve, 'multiple),
          Path('ownershipGuidelines, 'achieve, 'value),
          Path('ownershipGuidelines, 'achieve, 'shares),
          Path('ownershipGuidelines, 'achieve, 'lesserOf),
          Path('ownershipGuidelines, 'retention, 'ratio),
          Path('ownershipGuidelines, 'retention, 'period))

  override def Mapping = WorkbookMapping(
    Seq(Area(TCompanyFiscalYear, Offset(1, 2), None, WithPartialMetadataRowOrientedLayout, docSrcMapping),
        Area(TBod, Offset(3, 1), Some(10), WithMetadataAndSeparatorColumnOrientedLayout, bodMapping)))
          
  override def CombinerPhase = DocSrcCombiner((10, 'bod, colWrapping))
  
  override def SheetValidation = Valid(_)

}