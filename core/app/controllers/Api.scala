package controllers

import play.api.libs.json.{JsString, JsValue}
import play.api.libs.json.Json._
import play.api.mvc._

import persistence.query._
import persistence._

import model.Commons._

import libt.util.Strings._
import parser._
import libt._
import libt.json._
import controllers.generic.SpreadsheetDownloader
import output.PeersPeersReport


object Api extends Controller with SpreadsheetDownloader {

  def companies = Action {
    Ok(toJson(ExecutivesDb.findAllCompaniesIdWithNames.map { case (cusip, ticker, name) =>
      Map("cusip" -> cusip, "ticker" -> ticker, "name" -> name)
    }))
  }

  def primaryRoles = valuesToJson(TPrimaryValues)
  def secondaryRoles = valuesToJson(TSecondaryValues)
  def level = valuesToJson(TLevelValues)
  def scope = valuesToJson(TScopeValues)
  def bod = valuesToJson(TBodValues)

  def cashCompensations = pathsToJson(
    Relative(
      Path('executives, 'cashCompensations),
      Path('baseSalary),
      Path('bonus),
      Path('salaryAndBonus)
    ).map(_ ++ Path('value)),
    _(2)
  )

  def equityCompensations = pathsToJson(
    Relative(
      Path('executives),
      Path('optionGrants),
      Path('timeVestRS),
      Path('performanceVestRS)
    ).map(_ ++ Path('value)),
    _(1)
  )

  def pathsToJson(paths: Seq[Path], description : Path => PathPart) : Action[AnyContent] = {
    Action {
      Ok(toJson(paths.map {
         path => Map("field" -> path.joinWithDots, "value" -> description(path).name.upperCaseFromCamelCase.trim)
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

  def companiesReport = Action { request =>
    request.body.asJson.map { json =>
      val range = (json \ "range").as[Int]
      val companies = (json \ "companies").as[Seq[String]]
      createSpreadsheetResult(companies, range)(ExecutivesDb) match {
        case Some(response) => response
        case None => NotFound("not found companies")
      }
    }.getOrElse(BadRequest("invalid json"))
  }

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

}

