package controllers

import com.mongodb.casbah.MongoClient
import play.api.libs.json.Json._
import libt.Model
import persistence._
import play.api.mvc._


object Api extends Controller {

  implicit val db = MongoClient()("windsor")

  def tickers = Action {
    Ok(toJson(findAllCompaniesNames))
  }

}
