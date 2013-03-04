package persistence

import scala.math.BigDecimal
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import com.mongodb.DBObject
import com.mongodb.casbah.Imports._
import model._
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
  val companies = db("companies")
  val interests = db("carriedInterests")

  val executiveNoCashCompensations = Executive(Some("name"),
    Some("title"),
    Some("short"),
    Some("functional"),
    Some("founder"),
    Seq(),
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
      Some(1: BigDecimal),
      Some(1: BigDecimal)),
    CarriedInterest(
      ownedShares = Some(4: BigDecimal),
      vestedOptions = Some(43: BigDecimal),
      unvestedOptions = None,
      tineVest = None,
      perfVest = Some(2: BigDecimal)))
  val executiveWithCashCompensation = Executive(Some("name"),
    Some("title"),
    Some("short"),
    Some("functional"),
    Some("founder"),
    Seq(
      AnualCashCompensation(
        Some(1: BigDecimal),
        Some(1: BigDecimal),
        Some(1: BigDecimal),
        Some(1: BigDecimal),
        Some(1: BigDecimal),
        New8KData(
          Some(1: BigDecimal),
          Some(1: BigDecimal)))),
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
      Some(1: BigDecimal),
      Some(1: BigDecimal)),
    CarriedInterest(
      ownedShares = Some(4: BigDecimal),
      vestedOptions = Some(43: BigDecimal),
      unvestedOptions = None,
      tineVest = None,
      perfVest = Some(2: BigDecimal)))

  test("can persist executives") {
    interests.insert(executiveNoCashCompensations)
  }

  test("can persist executives with cash compensations") {
    interests.insert(executiveWithCashCompensation)
  }

  test("can persist companies") {
    companies.insert(Company(
      "ticker",
      "name",
      new Date(),
      "gicsIndustry",
      2: BigDecimal,
      2: BigDecimal,
      2: BigDecimal,
      Seq(executiveNoCashCompensations)))
  }

  test("can serialize input") {
    val input = (Some(2): Input[Int]): DBO
    assert(input.toString === """{ "value" : 2}""")
  }

}
