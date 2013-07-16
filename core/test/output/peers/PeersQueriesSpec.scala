package output.peers

import org.scalatest.{BeforeAndAfterAll, FlatSpec}
import persistence._
import libt._
import com.mongodb.casbah.MongoClient

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

    it should "Get companies that have a target Company as a Peer" in {
      assert(PeersDb.indirectPeersOf("A") ===
        Seq(Model('ticker -> Value("B"),'peerTicker -> Value("A")),
            Model('ticker -> Value("C"),'peerTicker -> Value("A"))))
    }

    it should "Get Direct Peers for a single target Company" in {
      assert(PeersDb.peersOf("B") ===
        Seq(Model('ticker -> Value("B"),'peerTicker -> Value("A")),
            Model('ticker -> Value("B"),'peerTicker -> Value("C"))))
    }

    it should "Get Direct Peers for target companies" in {
      assert(PeersDb.peersOf("A","B") ===
        Seq(Model('ticker -> Value("A"),'peerTicker -> Value("B")),
            Model('ticker -> Value("A"),'peerTicker -> Value("C")),
            Model('ticker -> Value("B"),'peerTicker -> Value("A")),
            Model('ticker -> Value("B"),'peerTicker -> Value("C"))))
    }

    it should "Get Peers of Peers companies for a target Company" in {
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
