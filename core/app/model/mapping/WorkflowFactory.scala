package model.mapping

import libt.workflow._
import libt.spreadsheet.reader._
import libt.error.Validated._
import libt.error._
import libt._

import org.apache.poi.ss.usermodel.Workbook

trait Mappeable {
  def MappingPhase(mapping: WorkbookMapping): Phase[Workbook, Seq[Seq[Model]]] =
    (wb, _) => mapping.read(wb).map(_.filter(!_.isEmpty))
}

trait WorkflowFactory extends Mappeable{

  def Workflow: FrontPhase[Seq[Model]] =
	  MappingPhase(Mapping) >> 
	  CombinerPhase >>
	  SheetValidationPhase >>
	  WorkbookValidationPhase
      
  def SheetValidationPhase: Phase[Seq[Model], Seq[Model]] =
    (_, models) => { models.concatMap(SheetValidation) }

  def WorkbookValidationPhase: Phase[Seq[Model], Seq[Model]] = IdPhase
  def CombinerPhase : Phase[Seq[Seq[Model]], Seq[Model]]

  def Mapping : WorkbookMapping 
  def SheetValidation: Model => Validated[Model]
}