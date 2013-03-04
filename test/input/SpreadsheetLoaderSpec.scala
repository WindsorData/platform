package input
import model._
import util._
import org.scalatest.path.FunSpec
import org.junit.runner.RunWith
import java.io.FileInputStream
import scala.math.BigDecimal.int2bigDecimal
import util.Closeables.closeable2RichCloseable
import org.scalatest.junit.JUnitRunner
import java.io.InputStream
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.usermodel.Row
import scala.collection.mutable.ListBuffer

@RunWith(classOf[JUnitRunner])
class SpreadsheetLoaderSpec extends FunSpec {
  describe("An importer") {

    it("should import Executives") {
      assert(loadSpreadsheet("Sample1.xls") ===
        Seq(
          Executive(
            name = Input(Some("ExecutiveName1"), None, None, None, None),
            title = Input(Some("ExecutiveTitle1"), None, None, None, None),
            shortTitle = Input(Some("ExTi1"), None, None, None, None),
            functionalMatch = Input(Some("lala"), None, None, None, None),
            founder = Input(Some("lala"), None, None, None, None),
            carriedInterest = CarriedInterest(
              ownedShares = Input(Some(100), None, None, None, None),
              vestedOptions = Input(Some(200), None, None, None, None),
              unvestedOptions = Input(Some(300), None, None, None, None),
              tineVest = Input(Some(400), None, None, None, None),
              perfVest = Input(Some(500), None, None, None, None)),
            equityCompanyValue = EquityCompanyValue(
              optionsValue = Input(Some(1), None, None, None, None),
              options = Input(Some(1), None, None, None, None),
              exPrice = Input(Some(1), None, None, None, None),
              bsPercentage = Input(Some(1), None, None, None, None),
              timeVest = Input(Some(1), None, None, None, None),
              shares = Input(Some(1), None, None, None, None),
              price = Input(Some(1), None, None, None, None),
              perf = Input(Some(1), None, None, None, None)),
            cashCompensations = Seq(
              AnualCashCompensation(
                Input(Some(1000.0), None, None, None, None),
                Input(Some(1.0), None, None, None, None),
                Input(Some(1.0), None, None, None, None),
                Input(Some(1.0), None, None, None, None),
                Input(Some(1.0), None, None, None, None)))),

          Executive(
            name = Input(Some("ExecutiveName2"), None, None, None, None),
            title = Input(Some("ExecutiveTitle2"), None, None, None, None),
            shortTitle = Input(Some("ExTi2"), None, None, None, None),
            functionalMatch = Input(Some("lala"), None, None, None, None),
            founder = Input(Some("lala"), None, None, None, None),
            carriedInterest = CarriedInterest(
              ownedShares = Input(Some(100), None, None, None, None),
              vestedOptions = Input(Some(200), None, None, None, None),
              unvestedOptions = Input(Some(300), None, None, None, None),
              tineVest = Input(Some(400), None, None, None, None),
              perfVest = Input(Some(500), None, None, None, None)),
            equityCompanyValue = EquityCompanyValue(
              optionsValue = Input(Some(1), None, None, None, None),
              options = Input(Some(1), None, None, None, None),
              exPrice = Input(Some(1), None, None, None, None),
              bsPercentage = Input(Some(1), None, None, None, None),
              timeVest = Input(Some(1), None, None, None, None),
              shares = Input(Some(1), None, None, None, None),
              price = Input(Some(1), None, None, None, None),
              perf = Input(Some(1), None, None, None, None)),
            cashCompensations = Seq(
              AnualCashCompensation(
                Input(Some(1000.0), None, None, None, None),
                Input(Some(1.0), None, None, None, None),
                Input(Some(1.0), None, None, None, None),
                Input(Some(1.0), None, None, None, None),
                Input(Some(1.0), None, None, None, None))))))
    }
//        it("should import Companies") {
//          assert(loadSpreadsheet("Sample1.xlsx") ===
//            Seq(
//              Company(
//                ticker = "X",
//                name = "Foo",
//                disclosureFiscalYear = DateTime.parse("2010-01-1").toDate,
//                gicsIndustry = "I",
//                annualRevenue = 10000,
//                marketCapital = 12000,
//                proxyShares = 1220,
//                executives = Seq()),
//              Company(
//                ticker = "Y",
//                name = "Bar",
//                disclosureFiscalYear = DateTime.parse("2010-02-02").toDate,
//                gicsIndustry = "I",
//                annualRevenue = 20000,
//                marketCapital = 32000,
//                proxyShares = 4220,
//                executives = Seq())))
//        }

    it("should ommit blanks") {
      load("Empty.xls") {
        x =>   {
          import util.poi.Cells._
          val wb = WorkbookFactory.create(x)
          val wbIterator = wb.getSheetAt(0).rowIterator
          wbIterator.next
          val cellIter = wbIterator.next.cellIterator
          cellIter.next
          val cell = cellIter.next
          assert( blankToNone(cell) === None)
        }
      }
    }

    it("should not ommit blanks") {
      load("Empty.xlsx") {
        x =>
          {
            import util.poi.Cells._
            import scala.collection.JavaConversions._
            val wb = WorkbookFactory.create(x)
            var r = ListBuffer[String]()
            val sheet = wb.getSheetAt(0)
            for (rowIndex <- 0 to sheet.getLastRowNum() - 1) {
              val row = sheet.getRow(rowIndex)
              if (row != null) {
                for (cellIndex <- 0 to row.getLastCellNum() - 1) {
                  val cell = row.getCell(cellIndex)
                  if (cell != null) {
                    if (cell.getCellType() == 3)
                      r += "_"
                    else
                      r += cell.getStringCellValue()
                  }
                }
              }
            }

            assert(r.toSeq === List("_", "a", "b", "c", "_", "d", "e", "_", "f", "g", "_", "h", "i", "j", "_", "k", "_", "l", "_", "_", "_", "_", "_", "_", "m", "n", "_", "_", "_", "o"))
          }
      }
    }

  }

  def load[T](name: String)(action: InputStream => T) = {
    import Closeables._
    new FileInputStream("test/input/" + name).processWith {
      x => action(x)
    }
  }

  def loadSpreadsheet(name: String) = {
    load(name) { x =>
      SpreadsheetLoader.load(x)
    }
  }
  
  
}
