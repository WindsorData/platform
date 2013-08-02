package model.mapping

import libt.spreadsheet.reader._
import org.apache.poi.ss.usermodel.Workbook
import libt.spreadsheet.{Feature, Offset}
import libt.Path
import model._
import libt.error.generic.Valid

object ticker {

  /**Layout for DOC_SRC sheets*/
  object DocSrcLayout extends RowOrientedLayout(WithPartialMetadataValueReader)

  def apply(wb: Workbook) =
    WorkbookMapping(
      Seq(Area(TCompanyFiscalYear, Offset(2, 2), None, DocSrcLayout, Seq(Feature(Path('ticker)))))
    ).read(wb) match {
      case Valid(models) => models.head.head /!/ 'ticker
      case _ => "Unknown"
    }
}
