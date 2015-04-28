package windsor.persistence

import org.scalatest.BeforeAndAfter
import org.scalatest.FlatSpec

import com.mongodb.casbah.MongoClient

import libt.Model
import libt.Value

class CompanyIndexSpec extends FlatSpec with BeforeAndAfter {

  var db: PeersCompaniesDb = _

  before {
    val client = MongoClient()("windsor-peers-specs")
    client.dropDatabase()
    db = PeersCompaniesDb(client)
  }

  it should "get names when present on db" in {
    db.IndexDb.insert(Model('ticker -> Value("bar"), 'name -> Value("baz")))
    val result = db.IndexDb.nameForTicker("bar")
    assert(result == Some("baz"))
  }

  it should "get nothing when not present on db" in {
    assert(db.IndexDb.nameForTicker("foo").isEmpty)
  }

  it should "get default when not present on db" in {
    assert(db.IndexDb.nameForTickerOrElse("foo", "baz") === "baz")
  }
}

