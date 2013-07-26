package persistence

import com.mongodb.casbah.Imports._
import libt.persistence._
import libt._
import model.PeerCompanies._
import com.mongodb.casbah.MongoClient

case class PeersCompaniesDb(db: MongoDB) extends Persistence {
  val TDBSchema: TModel = TPeers
  protected val colName: String = "peers"
  protected val pk: Seq[Path] = peerId

  def indirectPeersOf(ticker: String) : Seq[Model] =
    findWith(MongoDBObject("peerTicker.value" -> ticker),
      MongoDBObject("ticker.value" -> 1, "companyName.value" -> 1))

  def peersOf(tickers: String*) : Seq[Model] =
    if(tickers.nonEmpty)
      find(MongoDBObject("$or" -> tickers.map(it => MongoDBObject("ticker.value" -> it))))
    else
      Seq()

  def peersOfPeersOf(ticker: String) : (Seq[Model],Seq[Model]) =
    (primaryPeers(ticker), peersOf(peersOf(ticker).flatMap(_ /! 'peerTicker): _*))

  def primaryPeers(tickers: String*): Seq[Model] =
    peersOf(tickers: _*).toSeq.map(_.intersect(Seq(Path('peerTicker), Path('peerCoName))))

  def allTickers: Seq[Model] = findAllWith(MongoDBObject("ticker.value" -> 1))

  def namesFromPrimaryPeers(tickers: String*): Seq[Model] =
    findWith(
      MongoDBObject("$or" -> tickers.map(it => MongoDBObject("peerTicker.value" -> it))),
      MongoDBObject("peerCoName.value" -> 1, "peerTicker.value" -> 1))
    .groupBy(_ /!/ 'peerTicker).map { case (peerTicker, peerName) =>
      Model(
        'peerTicker -> Value(peerTicker),
        'peerName -> Value(peerName.sortBy(_ /!/ 'peerCoName).head /!/ 'peerCoName))
    }.toSeq

}
