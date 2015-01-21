package windsor.output

import libt._
import libt.Col
import libt.util.math._
import libt.spreadsheet.writer._

import model.PeerCompanies._

import windsor.output.writers.PeersWriter

import org.apache.poi.ss.usermodel.Sheet

trait ScorePeersReport {
  implicit def models2RichModels(models: Seq[Model]) = new {
    def sortByWeight = models.sortBy(_ /%/ 'weight).reverse
  }

  def makeReport(models: Seq[Model])(peerPeer: Model) =
    Model(
      'secondPeer -> peerPeer / 'secondaryPeer,
      'secondPeerName -> {
        val secondPeer = models.find( _ /!/ 'peerTicker == peerPeer /!/ 'secondaryPeer)
          .getOrElse(Model())
        secondPeer.get('peerCoName).getOrElse(Value("notfound"))
      },
      'weight -> Value(peerPeer('primaryPeers).asCol.elements.map(_.asModel /%/ 'weight).sum.roundUp(2)),
      'primaryPeersWeights ->
        peerPeer('primaryPeers).asCol.map { it =>
          Model(
            'weight -> (it / 'weight).asValue[BigDecimal].map(_.roundUp(2)),
            'primaryPeer -> it / 'ticker,
            'primaryPeerName -> {
              val primaryPeer = models.find( _ /!/ 'ticker == (it /!/ 'ticker)).get
              primaryPeer.get('companyName).getOrElse(Value("notfound"))
            },
            'link -> it.asModel.get('link).getOrElse(Value()))
        }
    )

  def calculation(currentModels: Model, models: Seq[Model]): Value[BigDecimal]

  def makePeerModel(models: Seq[Model])(peers: (String, Seq[Model])) : Model =
    peers match {
      case (secondaryPeer, primaryPeers) =>
        Model(
          'secondaryPeer -> Value(secondaryPeer),
          'primaryPeers ->
            Col(
              primaryPeers.map { it =>
                it.intersect(Seq(Path('ticker), Path('link))) +
                  ('weight, calculation(it, models))
              }: _*
            )
        )
    }

  def apply(models: Seq[Model]): Seq[Model] =
    models.groupBy(_ /!/ 'peerTicker)
      .toSeq
      .map(makeReport(models) _ compose makePeerModel(models))
      .sortByWeight
}

object UnnormalizedPeersOfPeersReport extends ScorePeersReport {
  override def calculation(model: Model, models: Seq[Model]) = Value(1: BigDecimal)
}

object NormalizedPeersOfPeersReport extends ScorePeersReport {

  override def calculation(currentModel: Model, models: Seq[Model]) = {
    val weights =
      models
        .groupBy(_ /!/ 'ticker)
        .map { case (primaryPeer, secondaryPeers) =>
          primaryPeer -> BigDecimal(100) / secondaryPeers.size
        }

    Value(weights(currentModel /!/ 'ticker): BigDecimal)
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
