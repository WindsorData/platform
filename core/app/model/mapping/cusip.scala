package model.mapping

import libt.spreadsheet.reader._
import org.apache.poi.ss.usermodel.Workbook
import libt.spreadsheet.{Feature, Offset}
import libt.Path
import model._
import libt.error.generic.Valid

object cusip {

  /**Layout for DOC_SRC sheets*/
  object DocSrcLayout extends RowOrientedLayout(WithPartialMetadataValueReader)

  def apply(wb: Workbook) =
    WorkbookMapping(
      Seq(Area(TCompanyFiscalYear, Offset(1, 2), None, DocSrcLayout, Seq(Feature(Path('cusip)))))
    ).read(wb) match {
      case Valid(models) => models.head.head /!/ 'cusip
      case _ => "Unknown"
    }
}
