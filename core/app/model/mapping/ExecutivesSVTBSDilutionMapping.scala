package model.mapping

import libt.Path
import model._
import model.ExecutivesSVTBSDilution._
import model.mapping._
import libt.spreadsheet.reader._
import libt.spreadsheet._
import libt.workflow._
import libt._

object ExecutivesSVTBSDilutionMapping {

  val usageAndSVTDataMapping =
    addTYears(
      Path('avgSharesOutstanding),
      Path('optionsSARs, 'granted),
      Path('optionsSARs, 'exPrice),
      Path('optionsSARs, 'cancelled),
      Path('fullValue, 'sharesGranted),
      Path('fullValue, 'grantPrice),
      Path('fullValue, 'sharesCancelled),
      Path('cashLTIP, 'grants),
      Path('cashLTIP, 'payouts))

  val blackScholesInputsMapping =
    addTYears(
      Path('valuationModel),
      Path('volatility),
      Path('expectedTerm),
      Path('riskFreeRate),
      Path('dividendYield),
      Path('bs))

  val dilutionMapping = Seq[Strip](
    Path('awardsOutstandings, 'option),
    Path('awardsOutstandings, 'fullValue),
    Path('awardsOutstandings, 'total),
    Path('sharesAvailable, 'current),
    Path('sharesAvailable, 'new),
    Path('sharesAvailable, 'everGreen, 'anual),
    Path('sharesAvailable, 'everGreen, 'yearsLeft),
    Path('sharesAvailable, 'fungible, 'ratio),
    Path('sharesAvailable, 'fungible, 'fullValue))

  def Mapping = WorkbookMapping(
      Seq(Area(TCompanyFiscalYear, Offset(2, 2), None, RowOrientedLayout, Seq(Feature(Path('ticker)), Feature(Path('name)))),
        Area(TUsageAndSVTData, Offset(3, 1), Some(1), ColumnOrientedLayout, usageAndSVTDataMapping),
        Area(TBlackScholesInputs, Offset(3, 1), Some(1), ColumnOrientedLayout, blackScholesInputsMapping),
        Area(TDilution, Offset(4, 1), Some(1), ColumnOrientedLayout, dilutionMapping))) 
    
  def SVTBSDilutionReader = InputWorkflow(MappingPhase(Mapping) >> CombinerPhase)
  
  def CombinerPhase =
    DocSrcCombiner(
      (10, 'usageAndSVTData, singleModelWrapping),
      (25, 'bsInputs, singleModelWrapping),
      (40, 'dilution, singleModelWrapping))
}