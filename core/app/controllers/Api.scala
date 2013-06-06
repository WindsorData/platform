package controllers

import com.mongodb.casbah.MongoClient
import libt.util.Symbols.richWord
import model.ExecutivesTop5._
import model.Commons._
import play.api.libs.json.Json._
import persistence._
import play.api.mvc._


object Api extends Controller {

  implicit val db = MongoClient()("windsor")

  def tickers = Action {
    Ok(toJson(findAllCompaniesNames))
  }

  def roles = Action {
    Ok(toJson(TPrimaryValues.values))
  }

  def cashCompensations = Action {
    Ok(toJson {
      TExecutive('cashCompensations).asModel.elementTypes.map {
        case (key, _) => Map("field" -> key.name, "description" -> key.upperCaseFromCamelCase)
      }
    })
  }

}
