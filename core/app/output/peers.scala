package output

import libt.util.math._
import libt._
import model.PeerCompanies._

trait ScorePeersReport {
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

object UnnormalizedPeersOfPeersReport extends ScorePeersReport {

  override def calculation(models: Seq[Model])(peers: (String, Seq[Model])) = peers match {
    case (secondaryPeer, primaryPeers) =>
      secondaryPeer -> primaryPeers.map(peer => BigDecimal(1) -> peer /!/ 'ticker)
  }

}

object NormalizedPeersOfPeersReport extends ScorePeersReport {

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

object PeersPeersReport {
  def primaryPeersNames(models: Seq[Model]) =
    models.toSeq.map(_.intersect(Seq(Path('peerTicker), Path('peerCoName))))

  def apply(models: (Seq[Model],Seq[Model])) =
    Model(
      'primaryPeers -> Col(primaryPeersNames(models._1): _*),
      'normalized -> Col(NormalizedPeersOfPeersReport(models._2): _*),
      'unnormalized -> Col(UnnormalizedPeersOfPeersReport(models._2): _*))
}

object RawPeersPeersReport {
  def apply(models: (Seq[Model], Seq[Model])): Seq[Model] =
    models match {
      case (primaryPeers, secondaryPeers) => {
        val peersWithoutPeers = primaryPeers.filterNot { primaryPeer =>
          secondaryPeers.exists( secondaryPeer => secondaryPeer /!/ 'ticker == primaryPeer /!/ 'peerTicker )
        }
        secondaryPeers ++ peersWithoutPeers.map { model =>
          TPeers.exampleWith(
            'companyName -> model('peerCoName),
            'ticker -> model('peerTicker),
            'src_doc -> model('src_doc),
            'group -> Value("NONE"),
            'comments -> Value("No peer group disclosed."))
        } :+ Model('primaryPeers -> Col(primaryPeers: _*))
      }
    }
}
