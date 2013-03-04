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
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.util.CellUtil
import org.apache.poi.ss.usermodel.Cell
import org.junit.Assert

@RunWith(classOf[JUnitRunner])
class SpreadsheetLoaderSpec extends FunSpec {
  describe("An importer") {

    it("should import a single executive") {
      Assert.assertEquals(
        loadSpreadsheet("FullValuesOnly.xlsx").take(1),
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
              rsValue = Input(Some(1), None, None, None, None),
              shares = Input(Some(1), None, None, None, None),
              price = Input(Some(1), None, None, None, None),
              perfRSValue = Input(Some(1), None, None, None, None),
              shares2 = Input(Some(1), None, None, None, None),
              price2 = Input(Some(1), None, None, None, None),
              perfCash = Input(Some(1), None, None, None, None)),
            cashCompensations = Seq(
              AnualCashCompensation(
                Input(Some(1000.0), None, None, None, None),
                Input(Some(1.0), None, None, None, None),
                Input(Some(1.0), None, None, None, None),
                Input(Some(1.0), None, None, None, None),
                Input(Some(1.0), None, None, None, None),
                New8KData(
                  Input(Some(1.0), None, None, None, None),
                  Input(Some(1.0), None, None, None, None)))))))
    }

    it("should import Executives") {
      assert(loadSpreadsheet("FullValuesOnly.xlsx") ===
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
              rsValue = Input(Some(1), None, None, None, None),
              shares = Input(Some(1), None, None, None, None),
              price = Input(Some(1), None, None, None, None),
              perfRSValue = Input(Some(1), None, None, None, None),
              shares2 = Input(Some(1), None, None, None, None),
              price2 = Input(Some(1), None, None, None, None),
              perfCash = Input(Some(1), None, None, None, None)),
            cashCompensations = Seq(
              AnualCashCompensation(
                Input(Some(1000.0), None, None, None, None),
                Input(Some(1.0), None, None, None, None),
                Input(Some(1.0), None, None, None, None),
                Input(Some(1.0), None, None, None, None),
                Input(Some(1.0), None, None, None, None),
                New8KData(
                  Input(Some(1.0), None, None, None, None),
                  Input(Some(1.0), None, None, None, None))))),

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
              rsValue = Input(Some(1), None, None, None, None),
              shares = Input(Some(1), None, None, None, None),
              price = Input(Some(1), None, None, None, None),
              perfRSValue = Input(Some(1), None, None, None, None),
              shares2 = Input(Some(1), None, None, None, None),
              price2 = Input(Some(1), None, None, None, None),
              perfCash = Input(Some(1), None, None, None, None)),
            cashCompensations = Seq(
              AnualCashCompensation(
                Input(Some(1000.0), None, None, None, None),
                Input(Some(1.0), None, None, None, None),
                Input(Some(1.0), None, None, None, None),
                Input(Some(1.0), None, None, None, None),
                Input(Some(1.0), None, None, None, None),
                New8KData(
                  Input(Some(1.0), None, None, None, None),
                  Input(Some(1.0), None, None, None, None)))))))
    }

    it("should import Executives with comments") {
      val executives = loadSpreadsheet("FullValuesAndComments.xlsx")
      assert(executives.take(1) ===
        Seq(
          Executive(
            name = Input(Some("ExecutiveName1"), None, Some("C1"), None, None),
            title = Input(Some("ExecutiveTitle1"), None, Some("C2"), None, None),
            shortTitle = Input(Some("ExTi1"), None, Some("C3"), None, None),
            functionalMatch = Input(Some("lala"), None, Some("C4"), None, None),
            founder = Input(Some("lala"), None, Some("C5"), None, None),
            cashCompensations = Seq(
              AnualCashCompensation(
                Input(Some(1000.0), None, Some("C6"), None, None),
                Input(Some(1.0), None, Some("C7"), None, None),
                Input(Some(1.0), None, Some("C8"), None, None),
                Input(Some(1.0), None, Some("C9"), None, None),
                Input(Some(1.0), None, Some("C10"), None, None),
                New8KData(
                  Input(Some(1.0), None, Some("C11"), None, None),
                  Input(Some(1.0), None, Some("C12"), None, None)))),
            equityCompanyValue = EquityCompanyValue(
              optionsValue = Input(Some(1), None, Some("C13"), None, None),
              options = Input(Some(1), None, Some("C14"), None, None),
              exPrice = Input(Some(1), None, Some("C15"), None, None),
              bsPercentage = Input(Some(1), None, None, None, None),
              timeVest = Input(Some(1), None, None, None, None),
              rsValue = Input(Some(1), None, None, None, None),
              shares = Input(Some(1), None, None, None, None),
              price = Input(Some(1), None, None, None, None),
              perfRSValue = Input(Some(1), None, None, None, None),
              shares2 = Input(Some(1), None, None, None, None),
              price2 = Input(Some(1), None, None, None, None),
              perfCash = Input(Some(1), None, None, None, None)),
            carriedInterest = CarriedInterest(
              ownedShares = Input(Some(100), None, None, None, None),
              vestedOptions = Input(Some(200), None, None, None, None),
              unvestedOptions = Input(Some(300), None, None, None, None),
              tineVest = Input(Some(400), None, None, None, None),
              perfVest = Input(Some(500), None, None, None, None)))))
    }

    it("should import Executives with extra information") {
      assert(loadSpreadsheet("FullValuesAndExtraInfo.xls").take(1) === Seq(
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
            optionsValue = Input(Some(1), Some("optionsValueCalc"), Some("optionsValueComment"), Some("optionsValueNote"), Some("http://optionsvaluelink.com")),
            options = Input(Some(1), None, None, None, None),
            exPrice = Input(Some(1), None, None, None, None),
            bsPercentage = Input(Some(1), None, None, None, None),
            timeVest = Input(Some(1), None, None, None, None),
            rsValue = Input(Some(1), None, None, None, None),
            shares = Input(Some(1), None, None, None, None),
            price = Input(Some(1), None, None, None, None),
            perfRSValue = Input(Some(1), None, None, None, None),
            shares2 = Input(Some(1), None, None, None, None),
            price2 = Input(Some(1), None, None, None, None),
            perfCash = Input(Some(1), Some("prefCashCalc"), None, Some("prefCashNote"), Some("http://prefCashLink.com/somethingelse"))),
          cashCompensations = Seq(
            AnualCashCompensation(
              baseSalary = Input(Some(1000.0), None, Some("baseSalaryComment"), None, None),
              actualBonus = Input(Some(1.0), None, Some("actualBonusComment"), None, None),
              targetBonus = Input(Some(1.0), None, None, None, None),
              thresholdBonus = Input(Some(1.0), None, None, None, None),
              maxBonus = Input(Some(1.0), None, None, None, None),
              New8KData(
                  Input(Some(1.0), None, None, None, None),
                  Input(Some(1.0), None, None, None, None)))))))
    }

    it("should not ommit blanks") {
      load("MatrixWithBlanks.xlsx") {
        x =>
          {
            import util.poi.Cells._
            val wb = WorkbookFactory.create(x)
            val sheet = wb.getSheetAt(0)

            val r =
              for (
                row <- rows(sheet);
                cell <- cells(row).take(6)
              ) yield blankToNone(cell).map(_.getStringCellValue).getOrElse("_")

            assert(r.toSeq ===
              List("_", "a", "b", "c", "_", "d",
                "e", "_", "f", "g", "_", "h",
                "I", "j", "_", "k", "_", "l",
                "_", "_", "_", "_", "_", "_",
                "m", "n", "_", "_", "_", "o"))
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
