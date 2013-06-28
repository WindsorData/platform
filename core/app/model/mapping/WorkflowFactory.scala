package model.mapping

import libt.spreadsheet.reader._
import libt.workflow._
import libt.error._
import libt._

import org.apache.poi.ss.usermodel.Workbook

trait WorkflowFactory {
  def MappingPhase(mapping: WorkbookMapping): Phase[Workbook, Seq[Seq[Validated[Model]]]] =
    (wb, _) => mapping.read(wb).filter(!_.isEmpty)

  def Workflow: FrontPhase[Seq[Validated[Model]]] = 
	  MappingPhase(Mapping) >> 
	  CombinerPhase >> 
	  SheetValidationPhase(Validation) >> 
	  WorkbookValidationPhase
      
  def SheetValidationPhase(validation: Validated[Model] => Validated[Model]): Phase[Seq[Validated[Model]], Seq[Validated[Model]]] =
    (_, models) => {
      if (!models.concat.isInvalid) {
        models.map(Validation)
      } else
        models
    }
    
  def WorkbookValidationPhase: Phase[Seq[Validated[Model]], Seq[Validated[Model]]]
  def CombinerPhase : Phase[Seq[Seq[Validated[Model]]], Seq[Validated[Model]]]
  def Mapping : WorkbookMapping 
  def Validation: Validated[Model] => Validated[Model]
}
