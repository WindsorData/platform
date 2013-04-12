package libt.spreadsheet
import libt.TValue
import libt.TString
import libt.TNumber
import libt.TEnum
import libt.Value
import libt.TModel
import org.apache.poi.ss.usermodel.Sheet

package object reader {

  implicit def tModel2Reader(schema: TModel) = new {
    def read(mapping: Mapping, sheet: Sheet) = new TReader(mapping, schema).read(sheet)
  }
}