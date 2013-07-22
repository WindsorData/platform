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
    find(MongoDBObject("peerTicker.value" -> ticker))

  def peersOf(tickers: String*) : Seq[Model] =
    if(tickers.nonEmpty)
      find(MongoDBObject("$or" -> tickers.map(it => MongoDBObject("ticker.value" -> it))))
    else
      Seq()

  def peersOfPeersOf(ticker: String) : Seq[Model] =
    peersOf(peersOf(ticker).flatMap(_ /! 'peerTicker): _*)

}
