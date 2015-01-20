package controllers

import play.api.libs.json._
import play.api.libs.json.Json._
import play.api.mvc._

import windsor.persistence.query._
import windsor.persistence._

import model.Commons._
import model.PeerCompanies._

import parser._

import libt._
import libt.json._

import output._
import controllers.generic.SpreadsheetDownloader
import output.PeersPeersReport
import output.StandardTop5Writer
import java.text.SimpleDateFormat
import org.joda.time.format.DateTimeFormat


object Api extends Controller with SpreadsheetDownloader {

  val pathsCashCompensations = Relative(
    Path('cashCompensations),
    Path('baseSalary),
    Path('actualBonus)
  ).:+(Path('calculated, 'salaryAndBonus)).map(_ ++ Path('value))

  val pathsEquityCompensations = Relative(
    Path('calculated, 'equityCompValue),
    Path('options),
    Path('timeVestRs),
    Path('perfRs)
  ).map(_ ++ Path('value))

  def companies = Action {
    Ok(toJson(
      List(ExecutivesDb, BodDb)
        .flatMap { _.findAllCompaniesIdWithNames }
        .distinct
        .map { case (cusip, ticker, name) => Map("cusip" -> cusip, "ticker" -> ticker, "name" -> name) }
    ))
  }

  def primaryRoles = valuesToJson(TPrimaryValues)
  def secondaryRoles = valuesToJson(TSecondaryValues)
  def level = valuesToJson(TLevelValues)
  def scope = valuesToJson(TScopeValues)
  def bod = valuesToJson(TBodValues)

  def cashCompensations = pathsToJson(
    pathsCashCompensations.zip(Seq("Base Salary", "Actual Bonus", "Salary And Bonus"))
  )

  def equityCompensations = pathsToJson(
    pathsEquityCompensations.zip(Seq("Option Grants", "Time Vest RS", "Performance Vest RS"))
  )

  def pathsToJson(paths: Seq[(Path, String)]) : Action[AnyContent] = {
    Action {
      Ok(toJson(paths.map {
        case (path, description) => Map("field" -> path.joinWithDots, "value" -> description)
      }))
    }
  }

  def valuesToJson(enums: TEnum[String]) : Action[AnyContent] = {
    Action {
      Ok(toJson(enums.values.map {
        name => Map("name" -> name)
      }))
    }
  }

  def allCompanies = Action { request =>
    val results =
      ExecutivesDb.findAllMap { model =>
        model.intersect(Seq(Path('cusip), Path('ticker), Path('name), Path('disclosureFiscalYear))) + ('type -> Value("Top5"))
      } ++
      BodDb.findAllMap { model =>
        model.intersect(Seq(Path('cusip), Path('ticker), Path('name), Path('disclosureFiscalYear))) + ('type -> Value("Bod"))
      } ++
      PeersDb.findAllMap { model =>
        model
          .intersect(Seq(Path('ticker), Path('companyName), Path('fiscalYear)))
          .modify {
            case ('companyName, value) => 'name -> value
            case ('fiscalYear, value) => 'disclosureFiscalYear -> value
          } + ('type -> Value("Peers"))
      }
    Ok(toJson(results.sortBy(_ /!/ 'ticker).map(_.asJson)))
  }

  def companiesSearch = Action { request =>
    val json : JsValue = request.body.asJson.get
    val query: QueryExecutives = QueryParser.query(json)

    val results = query(ExecutivesDb).map { company =>
      Map(
        "name" -> company /!/ 'name,
        "cusip" -> company /!/ 'cusip,
        "year" -> (company /#/ 'disclosureFiscalYear).toString
      )
    }

    Ok(toJson(results.map(toJson(_))))
  }

  def companiesReport(writer: OutputWriter, db: CompaniesDb) = Action { request =>
    request.body.asJson.map { json =>
      val range = (json \ "range").as[Int]
      val companies = (json \ "companies").as[Seq[String]]
      createCompanyBasedSpreadsheetResult(writer, companies, range)(db) match {
        case Some(response) => response
        case None => NotFound("not found companies")
      }
    }.getOrElse(BadRequest("invalid json"))
  }

  def top5Report = companiesReport(StandardTop5Writer, ExecutivesDb)
  def bodReport = companiesReport(BodWriter, BodDb)
  def fullReport = companiesReport(FullTop5Writer, ExecutivesDb)


  def rawDataReport(reportBuilder: ReportBuilder)(dataFrom: JsValue => Seq[Model]) =
    Action { request =>
      request.body.asJson.map { json =>
        val result = createSpreadsheetResult(
          PeersWriter(reportBuilder),
          dataFrom(json),
          0)
        result match {
          case Some(response) => response
          case None => NotFound("not found companies")
        }
      }.getOrElse(BadRequest("invalid json"))
    }

  def rawPeersPeers =
    rawDataReport(PeersPeersReport) { json =>
      val ticker = (json \ "ticker").as[String]
      PeersPeersReport.raw(PeersDb.peersOfPeersOf(ticker))
    }

  def rawIncomingPeers =
    rawDataReport(IncomingPeersReport) { json =>
      val ticker = (json \ "ticker").as[String]
      IncomingPeersReport.raw(PeersDb.indirectPeersOf(ticker))
    }

  def rawPeersPeersFromPrimaryPeers =
    rawDataReport(PeersPeersReport) { json =>
      val tickers = (json \ "tickers").as[Seq[String]]
      val peers = PeersDb.peersOfPeersFromPrimary(tickers: _*) match {
        case (primaryPeers, secondaryPeers) =>
          primaryPeers.map(ppeers => TPeers.exampleWith(ppeers.elements.toSeq: _*)) -> secondaryPeers
      }
      PeersPeersReport.raw(peers)
    }

  def incomingPeers = Action { request =>
    val ticker = (request.body.asJson.get \ "ticker").as[String]
    Ok(toJson(IncomingPeersReport(PeersDb.indirectPeersOf(ticker)).map(_.asJson)))
  }

  def peersPeers = Action { request =>
    val ticker = (request.body.asJson.get \ "ticker").as[String]
    Ok(toJson(PeersPeersReport(PeersDb.peersOfPeersOf(ticker)).asJson))
  }

  def peersPeersFromPrimaryPeers = Action { request =>
    val tickers = (request.body.asJson.get \ "tickers").as[Seq[String]]

    Ok(toJson(PeersPeersReport(
      PeersDb.peersOfPeersFromPrimary(tickers: _*)).asJson))
  }

  def allPeersTickers = Action { request =>
    Ok(toJson(PeersDb.allTickers.map(_.asJson).toSet))
  }

  def removeCompanyFiscalYear(cusip: String, disclosureFiscalYear:Int) = Action {
    ExecutivesDb.remove(cusip, disclosureFiscalYear)
    Ok
  }

  def removePeersData = Action {
    PeersDb.drop
    Ok
  }

  def removePeersCompany(ticker: String) = Action { request =>
    PeersDb.removeCompany(ticker) match {
      case Left(model) => NotFound(toJson(model.asJson))
    }
  }

  def removeSpecificPeer = Action { request =>
    request.body.asJson.map { json =>
      val fiscalYear = (json \ "fiscalYear").as[Int]
      val ticker = (json \ "from").as[String]
      val peerTicker = (json \ "peer").as[String]

      val fillingDate = DateTimeFormat.forPattern("MM/dd/YYYY").parseDateTime((json \ "fillingDate").as[String]).toDate
      val fillingDateFormatter = new SimpleDateFormat("yyyy-MM-dd")
      val formattedFillingDate =
        DateTimeFormat.forPattern("yyyy-MM-dd")
          .parseDateTime(fillingDateFormatter.format(fillingDate))

      PeersDb.removeSpecificPeer(ticker, peerTicker, fiscalYear, formattedFillingDate) match {
        case Left(model) => NotFound(toJson(model.asJson))
        case Right(models) => Ok(toJson(models.map(_.asJson)))
      }
    }.getOrElse(BadRequest(toJson(Model('error -> Value("invalid json")).asJson)))

  }

}


