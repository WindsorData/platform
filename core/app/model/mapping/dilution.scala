package model.mapping

import _root_.mapping.DilutionMappingComponent
import model.mapping.generic._
import model.ExecutivesSVTBSDilution._
import model.validation._
import model._

import libt.spreadsheet.reader._
import libt.spreadsheet._
import libt.error._
import libt._

trait FullOutputDilutionMappingComponent extends DilutionMappingComponent {

  val usageAndSVTDataMapping = {
    def Path(ps:PathPart*) = RelativeTo('usageAndSVTData)(ps)
    Years(
      Path('avgSharesOutstanding),
      Path('optionsSARs, 'granted),
      Path('optionsSARs, 'exPrice),
      Path('optionsSARs, 'cancelled),
      Path('fullValue, 'sharesGranted),
      Path('fullValue, 'grantPrice),
      Path('fullValue, 'sharesCancelled),
      Path('cashLTIP, 'grants),
      Path('cashLTIP, 'payouts))
  }

  val bsInputsMapping = {
    def Path(ps: PathPart*) = RelativeTo('bsInputs)(ps)
    Years(
      Path('volatility),
      Path('expectedTerm),
      Path('riskFreeRate),
      Path('dividendYield),
      Path('bs))
  }

  val dilutionMapping = {
    def Path(ps:PathPart*) = RelativeTo('dilution)(ps)
    Seq[Strip](
      Path('awardsOutstandings, 'option),
      Path('awardsOutstandings, 'fullValue),
      Path('awardsOutstandings, 'total),
      Path('sharesAvailable, 'current),
      Path('sharesAvailable, 'new),
      Path('sharesAvailable, 'everGreen, 'anual),
      Path('sharesAvailable, 'everGreen, 'yearsLeft),
      Path('sharesAvailable, 'fungible, 'ratio),
      Path('sharesAvailable, 'fungible, 'fullValue))
  }
}


trait FullInputDilutionMappingComponent extends DilutionMappingComponent {

  val usageAndSVTDataMapping =
    Years(
      Path('avgSharesOutstanding),
      Path('optionsSARs, 'granted),
      Path('optionsSARs, 'exPrice),
      Path('optionsSARs, 'cancelled),
      Path('fullValue, 'sharesGranted),
      Path('fullValue, 'grantPrice),
      Path('fullValue, 'sharesCancelled),
      Path('cashLTIP, 'grants),
      Path('cashLTIP, 'payouts))

  val bsInputsMapping =
    Years(
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
}

trait DilutionValidations {

  def averageSharesValidation(model: Model) = {
    val results: Seq[Validated[Model]] =
      Seq('year1, 'year2, 'year3).map {
        year =>
          (model / 'usageAndSVTData / 'avgSharesOutstanding /% year) match {
            case Some(value) if value > 999 =>
              Doubtful(model, "Warning on Usage And SVT Data: Average Shares should be in millions")
            case _ => Valid(model)
          }
      }

    results.reduceValidations(model)
  }

  def optionsAndFullValueValidation(model: Model) = {
    val results: Seq[Validated[Model]] =
      Seq((model / 'usageAndSVTData / 'optionsSARs / 'granted),
        (model / 'usageAndSVTData / 'optionsSARs / 'cancelled),
        (model / 'usageAndSVTData / 'fullValue / 'sharesGranted),
        (model / 'usageAndSVTData / 'fullValue / 'sharesCancelled))
        .flatMap(m => Seq((m /% 'year1),
        (m /% 'year2),
        (m /% 'year3))).flatten
        .map(value => if (value < 1000)
        Doubtful(model,
          warning(
            "Usage And SVT Data",
            "Options and Full Value: granted and cancelled values should not be less than 1000"))
      else Valid(model))

    results.reduceValidations(model)
  }

  def totalValidation(model: Model) =
    (for {
      option <- (model / 'dilution / 'awardsOutstandings /% 'option)
      full <- (model / 'dilution / 'awardsOutstandings /% 'fullValue)
      total <- (model / 'dilution / 'awardsOutstandings /% 'total)
      if option + full == total
    }
    yield Valid(model))
      .getOrElse(
      Invalid("Error on Dilution and ISS SVT Data - " +
        "Awards Outstandings: column total must be equal to option + full value"))

  def optionAndFullValuesValidation(model: Model) =
    (for {
      option <- (model / 'dilution / 'awardsOutstandings /% 'option)
      fullvalue <- (model / 'dilution / 'awardsOutstandings /% 'fullValue)
      if option == 0 || fullvalue == 0
    }
    yield Doubtful(model,
        warning(
          "Dilution and ISS SVT Data",
          "Awards Outstandings: Options and Full values should not be 0")))
      .getOrElse(Valid(model))

  def usageAndSVTValidations(model: Model): Validated[Model] =
    model.validate('usageAndSVTData) {
      averageSharesValidation(model) andThen optionsAndFullValueValidation(model)
    }

  def dilutionValidations(model: Model): Validated[Model] =
    model.validate('dilution) {
      totalValidation(model) andThen optionAndFullValuesValidation(model)
    }

}

package object dilution extends StandardWorkflowFactory with FullInputDilutionMappingComponent with DilutionValidations {

  trait DilutionDocSrcCombiner extends DocSrcModelCombiner {
    def combineModels(pointers: Seq[SheetPointer[Validated[Year]]], models: Seq[Seq[Model]], docSrcModel: Model) =
      (pointers, models).zipped.map {
        case ((year, key, elemWrap), executives) => year.map {
          it => it ++ Model('companyDB -> Model(key -> elemWrap(executives)))
        }
      }.concat.map {
        models =>
          models.groupBy(_ /#/ 'disclosureFiscalYear).mapValues(_.reduce(_ mergeTypeSafe _) ++ docSrcModel).values.toSeq
      }
  }


  def Mapping = WorkbookMapping(
    Seq(Area(TCompanyFiscalYear, Offset(1, 2), None, DocSrcLayout, DocSrcMapping),
      Area(TUsageAndSVTData, Offset(3, 1), Some(1), DataLayout, usageAndSVTDataMapping),
      Area(TBlackScholesInputs, Offset(3, 1), Some(1), DataLayout, bsInputsMapping),
      Area(TDilution, Offset(4, 1), Some(1), DataLayout, dilutionMapping)))

  def CombinerPhase =
    new DocSrcCombiner with DilutionDocSrcCombiner {
      override val rowPointers = Seq(
        (10, 'usageAndSVTData, singleModelWrapping),
        (25, 'bsInputs, singleModelWrapping),
        (40, 'dilution, singleModelWrapping))
    }

  override def SheetValidation = model => usageAndSVTValidations(model) andThen dilutionValidations(model)
}
