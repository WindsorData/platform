package libt.spreadsheet.reader

import libt.ModelOrErrors
import org.apache.poi.ss.usermodel.Workbook

class IdentityCombiner extends Combiner[Seq[Seq[ModelOrErrors]]] {
  def combineReadResult(wb: Workbook, results: Seq[Seq[ModelOrErrors]]) = results
}