package filter

import play.api.libs.json.JsValue
import persistence.query._
import persistence.query.EqualCondition
import persistence.query.QueryExecutives
import persistence.query.ConditionWithOperators
import scala.Some

object ParserJsonQuery {

  val parsers = Stream(EqualParser, ConditionsParser)

  def query(query: JsValue) : QueryExecutives = QueryExecutives(executivesFilters(query), advancedFilters(query))
  def executivesFilters(json: JsValue) : Seq[Filter] = filtersFromJson(basicsFromJson(json)).map(_.map(parseCondition(_)))
  def advancedFilters(json: JsValue) : Filter = (json \ "advanced").as[Seq[JsValue]].map(parseCondition(_))


  def parseCondition(jsonCondition: JsValue) : Condition = parsers.flatMap(_(jsonCondition)).toList.head

  def filtersFromJson(json: Seq[JsValue]) = json.map(_.\("executivesFilters").as[Seq[JsValue]])
  def basicsFromJson(json: JsValue) : Seq[JsValue] = (json \ "executives").as[Seq[JsValue]]


}

trait Parser {
  def apply(json: JsValue) : Option[Condition]
  def property(json:JsValue) = (json \ "key").as[String]
}

object EqualParser extends Parser {
  def apply(json: JsValue) = {
    val value: JsValue = json \ "value"
    value.asOpt[Double].orElse(value.asOpt[String]).map(EqualCondition(property(json), _))
  }
}

object ConditionsParser extends Parser {
  def apply(json: JsValue) = (json \ "operators").asOpt[Seq[JsValue]].map(it => ConditionWithOperators(property(json), operators(it)))

  def operators(operators: Seq[JsValue]) : Seq[Operator] = operators.map(operator(_))
  def operator(condition : JsValue) : Operator =  "$" + (condition \ "operator").as[String] -> (condition \ "value").as[Double]
}


