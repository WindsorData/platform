package model.mapping

import libt.spreadsheet.reader._
import libt.workflow._
import libt.error._
import libt._

import org.apache.poi.ss.usermodel.Workbook

trait WorkflowFactory {
  def MappingPhase(mapping: WorkbookMapping): Phase[Workbook, Seq[Seq[Validated[Model]]]] =
    (wb, _) => mapping.read(wb).filter(!_.isEmpty)

  def Workflow = InputWorkflow(MappingPhase(Mapping) >> CombinerPhase >> ValidationPhase)
  def ValidationPhase : Phase[Seq[Validated[Model]], Seq[Validated[Model]]]  = (x, y) => y
  def CombinerPhase : Phase[Seq[Seq[Validated[Model]]], Seq[Validated[Model]]]
  def Mapping : WorkbookMapping 
}