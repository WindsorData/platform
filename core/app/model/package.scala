import libt._
import libt.spreadsheet._
import scala.collection.immutable.Stream
import org.apache.poi.ss.usermodel.Row
import org.joda.time.DateTime
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Workbook

package object model {
  import persistence._
  import model.ExecutivesGuidelines._
  import model.ExecutivesTop5._
  
   val TCompanyFiscalYear = TModel(
    'ticker -> TString,
    'name -> TString,
    'disclosureFiscalYear -> TInt,

    'executives -> TCol(TExecutive),
    'guidelines -> TCol(TExecGuidelines))
}








