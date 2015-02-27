package windsor.persistence

import org.scalatest.FlatSpec
import com.mongodb.casbah.MongoClient
import libt.Model
import libt.Value

class CompanyIndexSpec extends FlatSpec {

  it should "get names" in {
    val client = MongoClient()("windsor-peers-specs")
    client.dropDatabase()
    val db = PeersCompaniesDb(client)
    db.IndexDb.insert(Model('ticker -> Value("bar"), 'name -> Value("baz")))

    assert(db.IndexDb.nameForTicker("foo").isEmpty)
    assert(db.IndexDb.nameForTicker("bar") == Some("baz"))
  }

}

