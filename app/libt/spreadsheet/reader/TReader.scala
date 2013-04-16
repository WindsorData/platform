package libt.spreadsheet.reader

import libt.spreadsheet.util._
import libt.builder.ModelBuilder
import libt.TElement
import libt.Model
import libt.spreadsheet.Mapping
import libt.TValue
import libt.TString
import libt.TNumber
import libt.TEnum
import libt.Value
import libt.TModel
import org.apache.poi.ss.usermodel.Sheet

class TReader(
  mapping: Mapping,
  schema: TElement) {
  
  val fieldCount = 10
  
  def read(sheet: Sheet): Seq[Model] = 
  Seq( { 
      val modelBuilder = new ModelBuilder()
      val reader = new RowOrientedReader(sheet.rows)

      for (column <- mapping.columns)
        column.read(reader, schema, modelBuilder)

      modelBuilder.build 
  }
    )
//    sheet.rows.grouped(6).map { inputGroup =>
//      val modelBuilder = new ModelBuilder()
//      val reader = new ColumnOrientedReader(inputGroup)
//
//      for (column <- mapping.columns)
//        column.read(reader, schema, modelBuilder)
//
//      modelBuilder.build
//    }.toSeq
//    
}
