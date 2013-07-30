package output.peers

import org.scalatest.{BeforeAndAfterAll, FlatSpec, Tag}
import persistence._
import libt._
import com.mongodb.casbah.MongoClient
import org.joda.time.DateTime

object DbTest extends Tag("com.windsor.tags.DbTest")

class PeersQueriesSpec extends FlatSpec with BeforeAndAfterAll {

  val db = PeersCompaniesDb(MongoClient()("windsor-peers-specs"))
  import db._

  val models = Seq(
    // A peers
    Model('ticker -> Value("A"), 'filingDate -> Value(new DateTime().toDate), 'fiscalYear -> Value(2013), 'peerCoName -> Value("B"), 'peerTicker -> Value("B")),
    Model('ticker -> Value("A"), 'filingDate -> Value(new DateTime().toDate), 'fiscalYear -> Value(2013), 'peerCoName -> Value("C"), 'peerTicker -> Value("C")),

    // B peers
    Model('ticker -> Value("B"), 'filingDate -> Value(new DateTime().toDate), 'fiscalYear -> Value(2013), 'peerCoName -> Value("A"), 'peerTicker -> Value("A")),
    Model('ticker -> Value("B"), 'filingDate -> Value(new DateTime().toDate), 'fiscalYear -> Value(2013), 'peerCoName -> Value("C"), 'peerTicker -> Value("C")),
    Model('ticker -> Value("B"), 'filingDate -> Value(new DateTime().minusDays(1).toDate), 'fiscalYear -> Value(2013), 'peerCoName -> Value("C"), 'peerTicker -> Value("WillNotAppear")),

    // C peers
    Model('ticker -> Value("C"), 'filingDate -> Value(new DateTime().toDate), 'fiscalYear -> Value(2013), 'peerCoName -> Value("A"), 'peerTicker -> Value("A")),
    Model('ticker -> Value("C"), 'filingDate -> Value(new DateTime().toDate), 'fiscalYear -> Value(2013), 'peerCoName -> Value("B"), 'peerTicker -> Value("B")))

  override def beforeAll() {
    clean
    insert(models:_*)
  }

  override def afterAll() {
    drop
  }

  behavior of "Peers Queries for Reports"

    it should "Get collection that have a target Company as a Peer" taggedAs(DbTest) in {
      assert(indirectPeersOf("A").map(_ - 'peerCoName).toSet ===
        Set(Model('ticker -> Value("B")), Model('ticker -> Value("C"))))
    }

    it should "Get Direct Peers for a single target Company" taggedAs(DbTest) in {
      assert(peersOf("B").toList.map(_ - 'peerCoName - 'filingDate - 'fiscalYear).toSet ===
        Set(
          Model('peerTicker -> Value("A"),'ticker -> Value("B")),
          Model('peerTicker -> Value("C"),'ticker -> Value("B"))))
    }

    it should "Get empty seq for a target company with no peers" taggedAs(DbTest) in {
      assert(peersOf("D") === Seq())
    }

    it should "Get empty seq for no target company" taggedAs(DbTest) in {
      assert(peersOf() === Seq())
    }

    it should "Get Direct Peers for target collection" taggedAs(DbTest) in {
      assert(peersOf("A","B").toList.map(_ - 'peerCoName- 'filingDate - 'fiscalYear).toSet ===
        Set(Model('ticker -> Value("A"),'peerTicker -> Value("B")),
            Model('ticker -> Value("A"),'peerTicker -> Value("C")),
            Model('ticker -> Value("B"),'peerTicker -> Value("A")),
            Model('ticker -> Value("B"),'peerTicker -> Value("C"))))
    }

    it should "Get Peers of Peers collection for a target Company" taggedAs(DbTest) in {
      assert(peersOfPeersOf("A")._2.map(_ - 'peerCoName- 'filingDate - 'fiscalYear).toSet ===
        Set(
          // B peers
          Model('ticker -> Value("B"),'peerTicker -> Value("A")),
          Model('ticker -> Value("B"),'peerTicker -> Value("C")),

          // C peers
          Model('ticker -> Value("C"),'peerTicker -> Value("A")),
          Model('ticker -> Value("C"),'peerTicker -> Value("B"))))
    }

}
