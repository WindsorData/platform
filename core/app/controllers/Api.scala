package controllers

import com.mongodb.casbah.MongoClient
import model.Commons
import play.api.libs.json.Json._
import persistence._
import play.api.mvc._

object Api extends Controller {

  implicit val db = MongoClient()("windsor")

  def tickers = Action {
    Ok(toJson(findAllCompaniesNames))
  }

  def roles = Action {
    Ok(toJson(Commons.TPrimaryValues.values))
  }

}
