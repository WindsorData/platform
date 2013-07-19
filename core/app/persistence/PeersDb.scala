package persistence

import com.mongodb.casbah.Imports._
import libt.persistence._
import libt._
import model.PeerCompanies._

object PeersDb extends Persistence {
  val TDBSchema: TModel = TPeers
  protected val colName: String = "peers"
  protected val pk: Seq[libt.Path] = peerId

  def indirectPeersOf(ticker: String)(implicit db: MongoDB) : Seq[Model] =
    find(MongoDBObject("peerTicker.value" -> ticker))

  def peersOf(tickers: String*)(implicit db: MongoDB) : Seq[Model] =
    if(tickers.nonEmpty) find(MongoDBObject("$or" -> tickers.map(it => MongoDBObject("ticker.value" -> it))))
    else Seq()

  def peersOfPeersOf(ticker: String)(implicit db: MongoDB) : Seq[Model] =
    peersOf(peersOf(ticker).flatMap(_ /! 'peerTicker): _*)

}