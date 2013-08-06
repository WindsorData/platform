package model.mapping.generic

import libt.spreadsheet.reader.workflow._
import libt.spreadsheet.reader._
import libt.spreadsheet._
import libt.error.generic.Validated._
import libt.error._
import libt._
import model.validation._

/**trait for objects that act as a factory of workflows
  * for reading standard Windsor workbooks-  workbooks that have
  * a doc_src sheet and then one or more sheets
  * with values and metadata - aka data sheets.*/
trait StandardWorkflowFactory {

  def Workflow: FrontPhase[Seq[Model]] =
	  MappingPhase(Mapping) >>
	  CombinerPhase >>
    AgreggationPhase >>
    SheetValidationPhase >>
	  WorkbookValidationPhase

  /**Phase for combining data sheets */
  def CombinerPhase : Phase[Seq[Seq[Model]], Seq[Model]]

  def AgreggationPhase : Phase[Seq[Model], Seq[Model]] = (_, models) => Valid(models)

  /**Phase for validating isolated sheets*/
  def SheetValidationPhase: Phase[Seq[Model], Seq[Model]] =
  (_, models) => {
    val result = models.concatMap(SheetValidation)
    if(models.exists(_ /! 'cusip nonEmpty))
      result
    else
      result andThen Invalid(err("ExecDb - DocSrc"
        ,"cusip can't be blank"))
  }

  /**Phase for validating bunchs of sheets*/
  def WorkbookValidationPhase: Phase[Seq[Model], Seq[Model]] = IdPhase

  def SheetValidation: Model => Validated[Model]

  def Mapping : WorkbookMapping

  /**Mapping for DocSrc sheet*/
  val DocSrcMapping = Seq(Feature(Path('cusip)), Feature(Path('ticker)), Feature(Path('name)))

  /**Layout for standard sheets of values and metadata*/
  object DataLayout
    extends ColumnOrientedLayout(WithSeparator(WithMetadataValueReader))

  /**Layout for DOC_SRC sheets*/
  object DocSrcLayout
    extends RowOrientedLayout(WithPartialMetadataValueReader)
}