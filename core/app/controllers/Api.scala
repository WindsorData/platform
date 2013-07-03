package controllers

import _root_.persistence.query.QueryExecutives
import play.api.libs.json.JsValue
import com.mongodb.casbah.MongoClient
import filter._
import libt.util.Strings._
import model.Commons._
import play.api.libs.json.Json._
import persistence._
import play.api.mvc._
import libt._
import play.api.Logger


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

  def cashCompensations = pathsToJson(
    Relative(
      Path('executives, 'cashCompensations),
      Path('baseSalary),
      Path('bonus),
      Path('salaryAndBonus)
    ),
    _.last
  )

  def equityCompensations = pathsToJson(
    Relative(
      Path('executives),
      Path('optionGrants),
      Path('timeVestRS),
      Path('performanceVestRS)
    ),
    _.last
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
    val query: QueryExecutives = ParserJsonQuery.query(json)

    val results = query().map { company =>
      Map(
        "name" -> company(Path('name)).getRawValue[String],
        "ticker" -> company(Path('ticker)).getRawValue[String],
        "year" -> company(Path('disclosureFiscalYear)).getRawValue[Int].toString
      )
    }

    Ok(toJson(results.map(toJson(_))))
  }

}
