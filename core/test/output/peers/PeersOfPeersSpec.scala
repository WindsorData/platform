package output.peers

import org.scalatest.FlatSpec
import libt._
import output._

class PeersOfPeersSpec extends FlatSpec {


  // Direct Peers of some company A
  val models =
    Seq(
      // P peers
      Model('ticker -> Value("P"), 'companyName -> Value("P"), 'peerTicker -> Value("X"), 'peerCoName -> Value("X")),
      Model('ticker -> Value("P"), 'companyName -> Value("P"), 'peerTicker -> Value("Y"), 'peerCoName -> Value("Y")),
      Model('ticker -> Value("P"), 'companyName -> Value("P"), 'peerTicker -> Value("Z"), 'peerCoName -> Value("Z")),

      // Q peers
      Model('ticker -> Value("Q"), 'companyName -> Value("Q"), 'peerTicker -> Value("B"), 'peerCoName -> Value("B")),
      Model('ticker -> Value("Q"), 'companyName -> Value("Q"), 'peerTicker -> Value("C"), 'peerCoName -> Value("C")),
      Model('ticker -> Value("Q"), 'companyName -> Value("Q"), 'peerTicker -> Value("X"), 'peerCoName -> Value("X")),
      Model('ticker -> Value("Q"), 'companyName -> Value("Q"), 'peerTicker -> Value("Z"), 'peerCoName -> Value("Z")),

      // R peers
      Model('ticker -> Value("R"), 'companyName -> Value("R"), 'peerTicker -> Value("F"), 'peerCoName -> Value("F")),
      Model('ticker -> Value("R"), 'companyName -> Value("R"), 'peerTicker -> Value("G"), 'peerCoName -> Value("G")),
      Model('ticker -> Value("R"), 'companyName -> Value("R"), 'peerTicker -> Value("X"), 'peerCoName -> Value("X")))

  behavior of "Peer-Peer report calculations"

  it should "calculate non-normalized peers of peers report" in {
    assert(UnnormalizedPeersOfPeersReport(models).take(2).map(_ - 'primaryPeersWeights - 'secondPeerName) ===
      Seq(
        Model('secondPeer -> Value("X"), 'weight -> Value(3)),
        Model('secondPeer -> Value("Z"), 'weight -> Value(2))))
  }

  it should "calculate normalized peers of peers report" in {
    assert(NormalizedPeersOfPeersReport(models).take(2).map(_ - 'primaryPeersWeights - 'secondPeerName).toList ===
      List(
        Model('secondPeer -> Value("X"), 'weight -> Value(91.67: BigDecimal)),
        Model('secondPeer -> Value("Z"), 'weight -> Value(58.33: BigDecimal))))
  }

}
