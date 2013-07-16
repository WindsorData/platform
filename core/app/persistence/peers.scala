package persistence

import com.mongodb.casbah.Imports._
import libt.persistence._
import libt._
import model.PeerCompanies._

object PeersDb {
  type DBO = DBObject

  implicit def mongoCollection2Models(result: MongoCollection#CursorType): Seq[Model] =
    result.toSeq.map(TPeers.unmarshall(_).asModel)

  private def peers(implicit db: MongoDB) = db("peers")

  def save(models: Seq[Model])(implicit db: MongoDB) =
    models.foreach(model => peers.insert(TPeers.marshall(model)))

  def update(models: Model*)(implicit db: MongoDB) = {
    models.foreach{ model =>
      peers.update(
        peerPKQuery(model),
        MongoDBObject("$set" -> TPeers.marshall(model)),
        true)
    }
  }

  def peerPKQuery(model: Model) =
    MongoDBObject(
      peerId.map { path =>
        peerKey(path) -> model(path).getRawValue[String]
      }:_*)

  def peerKey(path: Path) : String = path.titles.mkString(".") + ".value"

  def indirectPeersOf(ticker: String)(implicit db: MongoDB) : Seq[Model] =
    peers.find(MongoDBObject("peerTicker.value" -> ticker))

  def peersOf(tickers: String*)(implicit db: MongoDB) : Seq[Model] = {
    val results = tickers.toSeq.foldLeft(MongoDBList.newBuilder) {  (builder, it) =>
      builder += MongoDBObject("ticker.value" -> it)
    }.result
    peers.find(MongoDBObject("$or" -> results))
  }

  def peersOfPeersOf(ticker: String)(implicit db: MongoDB) : Seq[Model] =
    peersOf(peersOf(ticker).flatMap(_ /! 'peerTicker): _*)

  def clean(implicit db: MongoDB) = peers.drop()

  def drop(implicit db: MongoDB) = db.dropDatabase()

}
