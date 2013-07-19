package output.peers

import org.scalatest.{BeforeAndAfterAll, FlatSpec, Tag}
import persistence._
import libt._
import com.mongodb.casbah.MongoClient

object DbTest extends Tag("com.windsor.tags.DbTest")

class PeersQueriesSpec extends FlatSpec with BeforeAndAfterAll {

  implicit val db = MongoClient()("windsor-peers-specs")

  val models = Seq(
    // A peers
    Model('ticker -> Value("A"),'peerTicker -> Value("B")),
    Model('ticker -> Value("A"),'peerTicker -> Value("C")),

    // B peers
    Model('ticker -> Value("B"),'peerTicker -> Value("A")),
    Model('ticker -> Value("B"),'peerTicker -> Value("C")),

    // C peers
    Model('ticker -> Value("C"),'peerTicker -> Value("A")),
    Model('ticker -> Value("C"),'peerTicker -> Value("B")))

  override def beforeAll() {
    PeersDb.clean
    PeersDb.save(models)
  }

  override def afterAll() {
    PeersDb.drop
  }

  behavior of "Peers Queries for Reports"

    it should "Get companies that have a target Company as a Peer" taggedAs(DbTest) in {
      assert(PeersDb.indirectPeersOf("A") ===
        Seq(Model('ticker -> Value("B"),'peerTicker -> Value("A")),
            Model('ticker -> Value("C"),'peerTicker -> Value("A"))))
    }

    it should "Get Direct Peers for a single target Company" taggedAs(DbTest) in {
      assert(PeersDb.peersOf("B") ===
        Seq(Model('ticker -> Value("B"),'peerTicker -> Value("A")),
            Model('ticker -> Value("B"),'peerTicker -> Value("C"))))
    }

    it should "Get empty seq for a target company with no peers" taggedAs(DbTest) in {
      assert(PeersDb.peersOf("D") === Seq())
    }

    it should "Get empty seq for no target company" taggedAs(DbTest) in {
      assert(PeersDb.peersOf() === Seq())
    }

    it should "Get Direct Peers for target companies" taggedAs(DbTest) in {
      assert(PeersDb.peersOf("A","B") ===
        Seq(Model('ticker -> Value("A"),'peerTicker -> Value("B")),
            Model('ticker -> Value("A"),'peerTicker -> Value("C")),
            Model('ticker -> Value("B"),'peerTicker -> Value("A")),
            Model('ticker -> Value("B"),'peerTicker -> Value("C"))))
    }

    it should "Get Peers of Peers companies for a target Company" taggedAs(DbTest) in {
      assert(PeersDb.peersOfPeersOf("A").toList ===
        List(
          // B peers
          Model('ticker -> Value("B"),'peerTicker -> Value("A")),
          Model('ticker -> Value("B"),'peerTicker -> Value("C")),

          // C peers
          Model('ticker -> Value("C"),'peerTicker -> Value("A")),
          Model('ticker -> Value("C"),'peerTicker -> Value("B"))))
    }

}
