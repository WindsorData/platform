package controllers

import controllers.generic.SpreadsheetDownloader

import play.api.libs.json._
import play.api.libs.json.Json._
import play.api.mvc._

import persistence.query._
import persistence._

import model.Commons._

import parser._

import libt._
import libt.json._

import output._
import controllers.generic.SpreadsheetDownloader
import output.PeersPeersReport
import output.StandardTop5Writer


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
      createSpreadsheetResult(StandardTop5Writer, companies, range)(ExecutivesDb) match {
        case Some(response) => response
        case None => NotFound("not found companies")
      }
    }.getOrElse(BadRequest("invalid json"))
  }

  def top5Report = companiesReport(StandardTop5Writer,ExecutivesDb)
  def bodReport = companiesReport(BodWriter, BodDb)

  def incomingPeers = Action { request =>
    val ticker = (request.body.asJson.get \ "ticker").as[String]
    Ok(toJson(PeersDb.indirectPeersOf(ticker).map(_.asJson)))
  }

  def peersPeers = Action { request =>
    val ticker = (request.body.asJson.get \ "ticker").as[String]
    Ok(toJson(PeersPeersReport(PeersDb.peersOfPeersOf(ticker)).asJson))
  }

  def peersPeersFromPrimaryPeers = Action { request =>
    val tickers = (request.body.asJson.get \ "tickers").as[Seq[String]]
    Ok(toJson(PeersPeersReport(
      PeersDb.namesFromPrimaryPeers(tickers: _*) -> PeersDb.peersOf(tickers: _*)).asJson))
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

}

