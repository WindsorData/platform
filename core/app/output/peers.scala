package output

import libt.util.math._
import libt._

trait PeersReport {

  implicit def models2RichModels(models: Seq[Model]) = new {
    def sortByWeight = models.sortBy(_ /%/ 'weight).reverse
  }

  def makeReport(models: Seq[Model])(peerPeer: (String, Seq[(BigDecimal, String)])) = {
    peerPeer match {
      case (secondPeer, primaryPeers) =>
        Model(
          'secondPeer -> Value(secondPeer),
          'secondPeerName -> Value(models.find( _ /!/ 'peerTicker == secondPeer).get /!/ 'peerCoName),
          'weight -> Value(primaryPeers.map(it => it._1).sum.roundUp(2)),
          'primaryPeersWeights ->
            Col(primaryPeers.map {
              case (w, peer) =>
                Model(
                  'weight -> Value(w.roundUp(2)),
                  'primaryPeer -> Value(peer),
                  'primaryPeerName -> Value(models.find( _ /!/ 'ticker == peer).get /!/ 'companyName))
            }: _*)
        )
    }
  }

  def calculation(models: Seq[Model])(peers: (String, Seq[Model])) : (String, Seq[(BigDecimal, String)])

  def apply(models: Seq[Model]): Seq[Model] =
    models.groupBy(_ /!/ 'peerTicker)
      .toSeq
      .map(makeReport(models) _ compose calculation(models))
      .sortByWeight
}

object PeersPeersReport {
  def apply(models: (Seq[Model],Seq[Model])) =
    Model(
      'primaryPeers -> Col(models._1: _*),
      'normalized -> Col(NormalizedPeersOfPeersReport(models._2): _*),
      'unnormalized -> Col(UnnormalizedPeersOfPeersReport(models._2): _*))
}

object UnnormalizedPeersOfPeersReport extends PeersReport {

  override def calculation(models: Seq[Model])(peers: (String, Seq[Model])) = peers match {
    case (secondaryPeer, primaryPeers) =>
      secondaryPeer -> primaryPeers.map(peer => BigDecimal(1) -> peer /!/ 'ticker)
  }

}

object NormalizedPeersOfPeersReport extends PeersReport {

  override def calculation(models: Seq[Model])(peers: (String, Seq[Model])) = peers match {
    case (secondaryPeer, primaryPeers) => {
      val weights =
        models
          .groupBy(_ /!/ 'ticker)
          .map { case (primaryPeer, secondaryPeers) =>
            primaryPeer -> BigDecimal(100) / secondaryPeers.size }

      secondaryPeer -> primaryPeers.map { it => weights(it /!/ 'ticker) -> it /!/ 'ticker}
    }
  }
}
