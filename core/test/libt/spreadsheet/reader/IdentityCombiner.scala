package libt.spreadsheet.reader

import libt.error._
import libt._
import org.apache.poi.ss.usermodel.Workbook

class IdentityCombiner extends Combiner[Seq[Seq[Validated[Model]]]] {
  def combineReadResult(wb: Workbook, results: Seq[Seq[Validated[Model]]]) = results
}