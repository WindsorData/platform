package persistence

import scala.math.BigDecimal
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import com.mongodb.DBObject
import com.mongodb.casbah.Imports._
import model._
import persistence._
import model.CarriedInterest
import model.Executive
import model.Input
import play.api.Play.current
import play.api.test.Helpers._
import java.util.Date

@RunWith(classOf[JUnitRunner])
class PersistenceTest extends FunSuite {

  import util.persistence._
  registerBigDecimalConverter()

  val db = MongoClient()("test")
  implicit val testCollection = db("test")

  val executiveWithCashCompensation = Executive(Some("name"),
    Some("title"),
    Some("short"),
    Some("CEO"),
    None,
    None,
    Some("founder"),
      AnualCashCompensation(
        Some(1: BigDecimal),
        Some(1: BigDecimal),
        Some(1: BigDecimal),
        Some(1: BigDecimal),
        Some(1: BigDecimal),
        New8KData(
          Some(1: BigDecimal),
          Some(1: BigDecimal))),
    EquityCompanyValue(
      Some(1: BigDecimal),
      Some(1: BigDecimal),
      Some(1: BigDecimal),
      Some(1: BigDecimal),
      Some(1: BigDecimal),
      Some(1: BigDecimal),
      Some(1: BigDecimal),
      Some(1: BigDecimal),
      Some(1: BigDecimal),
      Some(1: BigDecimal),
      Some(1: BigDecimal)),
    CarriedInterest(
      ownedShares = Some(4: BigDecimal),
      vestedOptions = Some(43: BigDecimal),
      unvestedOptions = None,
      tineVest = None,
      perfVest = Some(2: BigDecimal)))



  test("can persist executives with cash compensations") {
    executiveWithCashCompensation.save()
  }

  test("can persist companies") {
      CompanyFiscalYear(None,None,None, Seq(executiveWithCashCompensation)).save()
  }

  test("can serialize input") {
    val input = (Some(2): Input[Int]): DBO
    assert(input.toString === """{ "value" : 2}""")
  }

}
