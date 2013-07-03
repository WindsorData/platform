package filter

import play.api.libs.json.JsValue
import persistence.query._

object ParserJsonQuery {

  def query(query: JsValue) : Query = Query(filters(query))
  def filters(json: JsValue) : Seq[Filter] = filtersFromJson(basicsFromJson(json)).map(_.map(conditionFromJson(_)))


  def conditionFromJson(jsonCondition: JsValue) : Condition = {
    val key = (jsonCondition \ "key").as[String]
    val jsonValue: JsValue = jsonCondition \ "value"
    val valueCondition = jsonValue.asOpt[Double].orElse(jsonValue.asOpt[String])

    valueCondition match {
      case None => ConditionWithOperators(key, operatorsFromJson((jsonCondition \ "operators").as[List[JsValue]]))
      case Some(value) => EqualCondition(key, value)
    }
  }

  def filtersFromJson(json: Seq[JsValue]) = json.map(_.\("filters").as[Seq[JsValue]])
  def basicsFromJson(json: JsValue) : Seq[JsValue] = (json \ "basics").as[Seq[JsValue]]
  def operatorsFromJson(operators: Seq[JsValue]) : Seq[Operator] = operators.map(operatorFromJson(_))
  def operatorFromJson(condition : JsValue) : Operator =  "$" + (condition \ "operator").as[String] -> (condition \ "value").as[Double]
}
