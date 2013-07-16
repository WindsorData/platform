package output

import libt._

trait PeersReport {

  implicit def models2RichModels(models: Seq[Model]) = new {
    def sortByWeight = models.sortBy(_ /%/ 'weight).reverse
  }

  def makeReport(peerPeer: (String, Seq[(BigDecimal, String)])) = {
    peerPeer match {
      case (secondPeer, primaryPeers) =>
        Model(
          'secondPeer -> Value(secondPeer),
          'weight -> Value(primaryPeers.map(it => it._1).sum.setScale(1, BigDecimal.RoundingMode.HALF_UP)),
          'primaryPeersWeights ->
            Col(primaryPeers.map {
              case (w, peer) => Model('weight -> Value(w), 'primaryPeer -> Value(peer))
            }: _*)
        )
    }
  }

  def doReport(models: Seq[Model])
              (calculation: ((String, Seq[Model])) => (String, Seq[(BigDecimal, String)])) =
    models.groupBy(_ /!/ 'peerTicker)
      .toSeq
      .map(makeReport _ compose calculation)
      .sortByWeight

  def apply(models: Seq[Model]): Seq[Model]
}

object UnnormalizedPeersOfPeersReport extends PeersReport {

  def apply(models: Seq[Model]) = doReport(models) {
    case (secondaryPeer, primaryPeers) =>
      (secondaryPeer, primaryPeers.map(peer => (1: BigDecimal, peer /!/ 'ticker)))
  }
}

object NormalizedPeersOfPeersReport extends PeersReport {

  def apply(models: Seq[Model]) : Seq[Model] = doReport(models) {
    case (secondaryPeer, primaryPeers) => {
      val weights =
        models
          .groupBy(_ /!/ 'ticker)
          .map { case (primaryPeer, secondaryPeers) =>
            (primaryPeer, BigDecimal(100) / secondaryPeers.size) }

      (secondaryPeer,
        primaryPeers.map { peer =>
          (weights(peer /!/ 'ticker), peer /!/ 'ticker)
        })
    }
  }
}
