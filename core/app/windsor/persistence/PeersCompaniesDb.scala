package windsor.persistence

import com.mongodb.casbah.Imports._
import libt._
import model.PeerCompanies._
import org.joda.time.DateTime
import com.mongodb.casbah.commons.conversions.scala._
import java.util.Date
import windsor.generic.persistence.Database

case class PeersCompaniesDb(db: MongoDB) extends Database {

  val TDBSchema: TModel = TPeers
  protected val colName: String = "peers"
  protected val pk: Seq[Path] = peerId

  object IndexDb extends CompanyIndexDb(db)

  def indirectPeersOf(ticker: String) : Seq[Model] = {
    find(MongoDBObject("peerTicker.value" -> ticker, "filingDate.value" -> MongoDBObject("$gte" -> DateTime.now().minusMonths(18).toDate)))
  }


  def peersOf(tickers: String*) : Seq[Model] = {
    /**
     * TODO: Don't do this. Insted, find a way to register DateTime convertions
     * in an isolated context or a simple method call
     */

    DeregisterJodaTimeConversionHelpers()

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
              .map(f => f.modify { entry => entry match {
                case ('companyName, name) => ('companyName -> nameValueFromIndex(f /!/ 'ticker, name))
                case ('peerCoName, name) =>  ('peerCoName -> nameValueFromIndex(f /!/ 'peerTicker, name))
                case e => e
              }} )
      }
      .toSeq
    }
    else
      Seq()
  }

  def nameValueFromIndex(ticker: String, default: Element) =
    Value(IndexDb.nameForTickerOrElse(ticker, default.getRawValue))

  def peersOfPeersOf(ticker: String) : (Seq[Model],Seq[Model]) =
    peersOf(ticker) -> peersOf(peersOf(ticker).flatMap(_ /! 'peerTicker): _*)

  def peersOfPeersFromPrimary(tickers: String*) : (Seq[Model],Seq[Model]) =
    namesFromPrimaryPeers(tickers: _*) -> peersOf(tickers: _*)

  def allTickers: Seq[Model] = findAllWith(MongoDBObject("ticker.value" -> 1))

  def namesFromPrimaryPeers(tickers: String*): Seq[Model] =
    findWith(
      MongoDBObject("$or" -> tickers.map(it => MongoDBObject("peerTicker.value" -> it))),
      MongoDBObject("peerCoName.value" -> 1, "peerTicker.value" -> 1))
    .groupBy(_ /!/ 'peerTicker).map { case (ticker, peers) =>
      Model(
        'peerTicker -> Value(ticker),
        'peerCoName -> nameValueFromIndex(ticker, peers.sortBy(_ /!/ 'peerCoName).head / 'peerCoName))
    }.toSeq

  def removePeers(query: MongoDBObject, projection: MongoDBObject, errorMsg: String) = {
    val tickers = findWith(query, projection)
    val result = collection.remove(query)

    if(tickers.nonEmpty && result.getLastError.ok)
      Right(tickers)
    else
      Left(Model('error -> Value(errorMsg)))
  }


  def removeCompany(ticker: String): Either[Model, Seq[Model]] =
    removePeers(
      MongoDBObject("ticker.value" -> ticker),
      MongoDBObject("peerTicker.value" -> 1, "peerCoName.value" -> 1),
      s"couldn't remove peer company $ticker. Perhaps the ticker does not exists in the database")

  def removeSpecificPeer(company: String, peer: String, fiscalYear: Int, fillingDate: DateTime) = {

    // Serialization for DateTime
    RegisterJodaTimeConversionHelpers()

     removePeers(
      MongoDBObject(
        "ticker.value" -> company,
        "peerTicker.value" -> peer,
        "fiscalYear.value" -> fiscalYear,
        "filingDate.value" -> fillingDate),
      MongoDBObject("peerTicker.value" -> 1),
      s"couldn't remove peer company $peer from company $company. Perhaps the ticker does not exists in the database")

  }


}

