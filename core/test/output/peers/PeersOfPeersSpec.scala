package output.peers

import org.scalatest.FlatSpec
import libt._
import output._

class PeersOfPeersSpec extends FlatSpec {


  // Direct Peers of some company A
  val models =
    Seq(
      // P peers
      Model('ticker -> Value("P"), 'companyName -> Value("P"), 'peerTicker -> Value("X"), 'peerCoName -> Value("X"), 'link -> Value("P-link")),
      Model('ticker -> Value("P"), 'companyName -> Value("P"), 'peerTicker -> Value("Y"), 'peerCoName -> Value("Y"), 'link -> Value("P-link")),
      Model('ticker -> Value("P"), 'companyName -> Value("P"), 'peerTicker -> Value("Z"), 'peerCoName -> Value("Z"), 'link -> Value("P-link")),

      // Q peers
      Model('ticker -> Value("Q"), 'companyName -> Value("Q"), 'peerTicker -> Value("B"), 'peerCoName -> Value("B"), 'link -> Value("Q-link")),
      Model('ticker -> Value("Q"), 'companyName -> Value("Q"), 'peerTicker -> Value("C"), 'peerCoName -> Value("C"), 'link -> Value("Q-link")),
      Model('ticker -> Value("Q"), 'companyName -> Value("Q"), 'peerTicker -> Value("X"), 'peerCoName -> Value("X"), 'link -> Value("Q-link")),
      Model('ticker -> Value("Q"), 'companyName -> Value("Q"), 'peerTicker -> Value("Z"), 'peerCoName -> Value("Z"), 'link -> Value("Q-link")),

      // R peers
      Model('ticker -> Value("R"), 'companyName -> Value("R"), 'peerTicker -> Value("F"), 'peerCoName -> Value("F"), 'link -> Value("R-link")),
      Model('ticker -> Value("R"), 'companyName -> Value("R"), 'peerTicker -> Value("G"), 'peerCoName -> Value("G"), 'link -> Value("R-link")),
      Model('ticker -> Value("R"), 'companyName -> Value("R"), 'peerTicker -> Value("X"), 'peerCoName -> Value("X"), 'link -> Value("R-link")))

  behavior of "Peer-Peer report calculations"

  it should "handle non defined values" in {
    val report = UnnormalizedPeersOfPeersReport(Seq(Model('ticker -> Value("P"), 'peerTicker -> Value("X") )))
    assert(report.forall(model => model.contains('secondPeerName)))
    assert(report.forall(model => (model / 'primaryPeersWeights).asCol(0).asModel.contains('link)))
  }

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
