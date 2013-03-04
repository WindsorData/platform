package persistence

import org.scalatest.FunSuite
import model.Company
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import model.Company
import play.api.test._
import play.api.test.Helpers._
import org.squeryl.PrimitiveTypeMode._

@RunWith(classOf[JUnitRunner])
class PersistenceTest extends FunSuite {
  test("can persist") {

    inFakeTransaction {
//      val company = DB.companies.insert(Company("foo", 2, 3))
//      assert(company.id != 0)
    }

    inFakeTransaction {
//      import DB._
//      val company = DB.companies.insert(Company("foo", 2, 3))
//      DB.companies.get(1L)
    }

  }

  def inFakeTransaction(action: => Unit) = running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
    inTransaction {
      action
    }
  }
}