package output

import libt.util.math._
import libt._
import output.PeersWriter
import model.PeerCompanies._
import libt.spreadsheet.writer.{CustomWriteSheetDefinition, WriteStrategy, CustomWriteArea, FullWriteStrategy}
import org.apache.poi.ss.usermodel.Sheet
import libt.Col
import output.PeersWriteStrategy
import libt.spreadsheet.writer.CustomWriteArea
import output.PeersWriter

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

trait ReportBuilder {
  type InputData
  val fileName: String

  def peersTitles(titles: Seq[Symbol])(models: Seq[Model]) : Seq[Model] =
    models.toSeq.map(_.intersect(titles.map(key => Path(key))))

  def defineWriters(writer: PeersWriter): Seq[CustomWriteArea]

  def raw(models: InputData): Seq[Model]
}


case class PeersWriteStrategy(select: Seq[Model] => Seq[Model]) extends WriteStrategy {
  def write(models: Seq[Model], area: CustomWriteSheetDefinition, sheet: Sheet) =
    area.customWrite(select(models), sheet)
}

object PeersPeersReport extends ReportBuilder {
  type InputData = (Seq[Model], Seq[Model])
  val fileName = "PeersOutputTemplate.xls"

  def apply(models: (Seq[Model],Seq[Model])) =
    Model(
      'primaryPeers -> Col(peersTitles(Seq('peerTicker, 'peerCoName))(models._1): _*),
      'normalized -> Col(NormalizedPeersOfPeersReport(models._2): _*),
      'unnormalized -> Col(UnnormalizedPeersOfPeersReport(models._2): _*))

  private object RawPeersPeersTab extends PeersWriteStrategy(_.filterNot(_.contains('primaryPeers)))
  private object RawPrimaryPeersTab extends PeersWriteStrategy( peers =>
    peers.find(_.contains('primaryPeers)).map(_ / 'primaryPeers).getOrElse(Col())
      .asCol.elements.map(_.asModel)
  )

  def defineWriters(writer: PeersWriter) =
    Seq(
      writer.peersArea(RawPeersPeersTab),
      writer.peersArea(RawPrimaryPeersTab))

  def raw(models: InputData) =
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

object IncomingPeersReport extends ReportBuilder {
  type InputData = Seq[Model]
  val fileName = "IncomingPeersOutputTemplate.xls"

  def apply(models: Seq[Model]): Seq[Model] =
    peersTitles(Seq('ticker, 'companyName))(models)

  def raw(models: InputData) = models

  def defineWriters(writer: PeersWriter) =
    Seq(
      writer.peersArea(PeersWriteStrategy(
        peersTitles(Seq('ticker, 'companyName))(_).map(peers => TPeers.exampleWith(peers.elements.toSeq: _*))
      )),
      writer.peersArea(PeersWriteStrategy(peers => peers)))
}