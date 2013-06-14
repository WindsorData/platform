package controllers

import com.mongodb.casbah.MongoClient
import libt.util.Symbols.richWord
import libt.TStringEnum
import model.ExecutivesTop5._
import model.Commons._
import play.api.libs.json.Json._
import persistence._
import play.api.mvc._
import libt._


object Api extends Controller {

  implicit val db = MongoClient()("windsor")

  def tickers = Action {
    Ok(toJson(findAllCompaniesNames.map {name => Map("name" -> name)}))
  }

  def primaryRoles = valuesToJson(TPrimaryValues)
  def secondaryRoles = valuesToJson(TSecondaryValues)
  def level = valuesToJson(TLevelValues)
  def scope = valuesToJson(TScopeValues)
  def bod = valuesToJson(TBodValues)

  def cashCompensations = Action {
    Ok(toJson {
      TExecutive('cashCompensations).asModel.elementTypes.map {
        case (key, _) => Map("field" -> key.name, "description" -> key.upperCaseFromCamelCase)
      }
    })
  }

  def valuesToJson(enums: TEnum[String]) : Action[AnyContent] = {
    Action {
      Ok(toJson(enums.values.map {
        name => Map("name" -> name)
      }))
    }
  }

}
