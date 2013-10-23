package persistence

import com.mongodb.casbah.Imports._
import libt._
import model.PeerCompanies._

case class PeersCompaniesDb(db: MongoDB) extends Persistence {
  val TDBSchema: TModel = TPeers
  protected val colName: String = "peers"
  protected val pk: Seq[Path] = peerId

  def indirectPeersOf(ticker: String) : Seq[Model] =
    findWith(MongoDBObject("peerTicker.value" -> ticker),
      MongoDBObject("ticker.value" -> 1, "companyName.value" -> 1))

  def peersOf(tickers: String*) : Seq[Model] =
    if(tickers.nonEmpty) {
      find(MongoDBObject("$or" -> tickers.map(it => MongoDBObject("ticker.value" -> it))))
      .groupBy(_ /!/ 'ticker)
      .flatMap {
        case (_, Nil) => Seq()
        case (_, xs) =>
          val maxFilingDate = xs.map(_ /@/ 'filingDate).max
          val maxFiscalYear = xs.map(_ /#/ 'fiscalYear).max
          xs
            .filter(_ /@/ 'filingDate == maxFilingDate)
            .filter(_ /#/ 'fiscalYear == maxFiscalYear)
      }
      .toSeq
    }
    else
      Seq()

  def peersOfPeersOf(ticker: String) : (Seq[Model],Seq[Model]) =
    peersOf(ticker) -> peersOf(peersOf(ticker).flatMap(_ /! 'peerTicker): _*)

  def peersOfPeersFromPrimary(tickers: String*) : (Seq[Model],Seq[Model]) =
    namesFromPrimaryPeers(tickers: _*) -> peersOf(tickers: _*)

  def allTickers: Seq[Model] = findAllWith(MongoDBObject("ticker.value" -> 1))

  def namesFromPrimaryPeers(tickers: String*): Seq[Model] =
    findWith(
      MongoDBObject("$or" -> tickers.map(it => MongoDBObject("peerTicker.value" -> it))),
      MongoDBObject("peerCoName.value" -> 1, "peerTicker.value" -> 1))
    .groupBy(_ /!/ 'peerTicker).map { case (peerTicker, peerName) =>
      Model(
        'peerTicker -> Value(peerTicker),
        'peerCoName -> Value(peerName.sortBy(_ /!/ 'peerCoName).head /!/ 'peerCoName))
    }.toSeq

}

