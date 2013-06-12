package libt.spreadsheet.reader

import libt.error._
import libt.workflow._
import libt._
import org.apache.poi.ss.usermodel.Workbook

object IdentityCombiner extends Phase[Seq[Seq[Validated[Model]]], Seq[Seq[Validated[Model]]]] {
  def apply(wb: Workbook, results: Seq[Seq[Validated[Model]]]) = results
}