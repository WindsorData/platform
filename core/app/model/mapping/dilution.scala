package model.mapping

import libt.Path
import model._
import model.ExecutivesSVTBSDilution._
import model.mapping._
import model.validation._
import libt.util._
import libt.spreadsheet.reader._
import libt.spreadsheet._
import libt.workflow._
import libt.error._
import libt._

package object dilution extends WorkflowFactory {

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

  def CombinerPhase =
    DocSrcCombiner(
      (10, 'usageAndSVTData, singleModelWrapping),
      (25, 'bsInputs, singleModelWrapping),
      (40, 'dilution, singleModelWrapping))

  def averageSharesValidation(model: Model) = {
    val results: Seq[Validated[Model]] =
      Seq(Path('year1), Path('year2), Path('year3)).map {
        year =>
          model(Path('usageAndSVTData, 'avgSharesOutstanding) ++ year).rawValue[BigDecimal] match {
            case Some(value) if value < 1000000 =>
              Doubtful(model, "Warning on Usage And SVT Data: Average Shares should be in millions")
            case _ => Valid(model)
          }
      }
    results.reduce((a, b) => a andThen b)
  }
  
  def optionsAndFullValueValidation(model: Model) = {
      val results : Seq[Validated[Model]] = 
      Seq(model(Path('optionsSARs, 'granted)),
          model(Path('optionsSARs, 'cancelled)),
          model(Path('fullValue, 'sharesGranted)),
          model(Path('fullValue, 'sharesCancelled)))
      .flatMap(model => Seq(model(Path('year1)).rawValue[BigDecimal], 
    		  				model(Path('year2)).rawValue[BigDecimal], 
    		  				model(Path('year3)).rawValue[BigDecimal])).flatten
      .map (value => if( value < 1000) 
    	  				Doubtful(model, 
    	  				    warning("Options and Full Value: granted and cancelled values should not be less than 1000")) 
    	  			 else Valid(model))
      
      results.reduce( (a, b) => a andThen b)
  }

  def totalValidation(model: Model) = {
    val option = model(Path('dilution, 'awardsOutstandings, 'option)).rawValue[BigDecimal]
    val full = model(Path('dilution, 'awardsOutstandings, 'fullValue)).rawValue[BigDecimal]
    val total = model(Path('dilution, 'awardsOutstandings, 'total)).rawValue[BigDecimal]
    
    (for( optionv <- option; fullv <- full; totalv <- total; if optionv + fullv == totalv )
      yield Valid(model))
      .getOrElse(
          Invalid("Error on Dilution and ISS SVT Data - " +
              "Awards Outstandings: column total must be equal to option + full value"))
  }
  
  def usageAndSVTValidations(model: Model): Validated[Model] = {
    if (model.hasElement('usageAndSVTData)) {
      averageSharesValidation(model) andThen
      optionsAndFullValueValidation(model)
    } else
      Valid(model)
  }
  
  def dilutionValidations(model: Model): Validated[Model] = {
    if (model.hasElement('dilution)) {
      totalValidation(model)
    } else
      Valid(model)
  }

  override def Validation =
    model => 
      usageAndSVTValidations(model.get) andThen
      dilutionValidations(model.get)
}