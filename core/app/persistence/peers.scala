package persistence

import com.mongodb.casbah.Imports._
import libt.persistence._
import libt._
import model.PeerCompanies.TPeers

//TODO: Merge these with package object
object PeersDb {
  type DBO = DBObject

  implicit def mongoCollection2Models(result: MongoCollection#CursorType): Seq[Model] =
    result.toSeq.map(TPeers.unmarshall(_).asModel)

  private def peers(implicit db: MongoDB) = db("peers")

  def save(models: Seq[Model])(implicit db: MongoDB) =
    models.foreach(model => peers.insert(TPeers.marshall(model)))

  def update(models: Model*)(implicit db: MongoDB) = {
    models.foreach{
      model =>
        peers.update(
          MongoDBObject(
            "ticker.value" -> model(Path('ticker)).getRawValue[String],
            "peerTicker.value" -> model(Path('peerTicker)).getRawValue[String]),
          TPeers.marshall(model), true)
    }
  }

  def indirectPeersOf(ticker: String)(implicit db: MongoDB) : Seq[Model] =
    peers.find(MongoDBObject("peerTicker.value" -> ticker))

  def peersOf(tickers: String*)(implicit db: MongoDB) : Seq[Model] = {
    val builder = MongoDBList.newBuilder
    tickers.toSeq.foreach(it => builder += MongoDBObject("ticker.value" -> it))
    peers.find(MongoDBObject("$or" -> builder.result))
  }

  def peersOfPeersOf(ticker: String)(implicit db: MongoDB) : Seq[Model] =
    peersOf(peersOf(ticker)
      .flatMap(_(Path('peerTicker)).rawValue[String]): _*)

  def clean(implicit db: MongoDB) = peers.drop()

  def drop(implicit db: MongoDB) = db.dropDatabase()

}
