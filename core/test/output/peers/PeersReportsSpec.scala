package output.peers

import org.scalatest.FlatSpec
import libt.{Value, Model}
import output.RawPeersPeersReport

class PeersReportsSpec extends FlatSpec {
  val primaryPeersModels =
    Seq(
      Model(
        'ticker -> Value("X"),
        'src_doc -> Value("src_doc"),
        'peerTicker -> Value("A"),
        'peerCoName -> Value("A")),
      Model(
        'ticker -> Value("X"),
        'src_doc -> Value("src_doc"),
        'peerTicker -> Value("B"),
        'peerCoName -> Value("B")),
      Model(
        'ticker -> Value("X"),
        'src_doc -> Value("src_doc"),
        'peerTicker -> Value("C"),
        'peerCoName -> Value("C")))

  val secondaryPeersModels =
    Seq(
      Model(
        'ticker -> Value("A"),
        'peerTicker -> Value("N")),
      Model(
        'ticker -> Value("A"),
        'peerTicker -> Value("M")),
      Model(
        'ticker -> Value("B"),
        'peerTicker -> Value("W")))


  behavior of "Peer-Peer Raw Report"

    it should "Add a peerless record when a primary Peer has no peers" in {
      val result: Seq[Model] = RawPeersPeersReport(primaryPeersModels -> secondaryPeersModels)
      assert(result.map(_ /!/ 'ticker).toSet === primaryPeersModels.map(_ /!/ 'peerTicker).toSet)
      assert(result.find(model => model /!/ 'ticker == "C").get /!/ 'group === "NONE")
    }

}
