package output.peers

import org.scalatest.FlatSpec
import libt._
import output._

class PeersOfPeersSpec extends FlatSpec {

  // Direct Peers of some company A
  val models =
    Seq(
      // P peers
      Model('ticker -> Value("P"),'peerTicker -> Value("X")),
      Model('ticker -> Value("P"),'peerTicker -> Value("Y")),
      Model('ticker -> Value("P"),'peerTicker -> Value("Z")),

      // Q peers
      Model('ticker -> Value("Q"),'peerTicker -> Value("B")),
      Model('ticker -> Value("Q"),'peerTicker -> Value("C")),
      Model('ticker -> Value("Q"),'peerTicker -> Value("X")),
      Model('ticker -> Value("Q"),'peerTicker -> Value("Z")),

      // R peers
      Model('ticker -> Value("R"),'peerTicker -> Value("F")),
      Model('ticker -> Value("R"),'peerTicker -> Value("G")),
      Model('ticker -> Value("R"),'peerTicker -> Value("X")))

  behavior of "Peer-Peer report calculations"

  it should "calculate non-normalized peers of peers report" in {
    assert(UnnormalizedPeersOfPeersReport(models).take(2).map(_ - 'primaryPeersWeights) ===
      Seq(
        Model('secondPeer -> Value("X"), 'weight -> Value(3)),
        Model('secondPeer -> Value("Z"), 'weight -> Value(2))))
  }

  it should "calculate normalized peers of peers report" in {
    assert(NormalizedPeersOfPeersReport(models).take(2).map(_ - 'primaryPeersWeights).toList ===
      List(
        Model('secondPeer -> Value("X"), 'weight -> Value(91.7: BigDecimal)),
        Model('secondPeer -> Value("Z"), 'weight -> Value(58.3: BigDecimal))))
  }
}
