import libt.Path
import persistence.query._
import play.api.libs.json.JsValue

package object parser {

  object ParserJsonQuery {

    val parsers = Stream(EqualParser, ConditionsParser)

    def query(query: JsValue) : QueryExecutives = QueryExecutives(executivesFilters(query), advancedFilters(query))
    def executivesFilters(json: JsValue) : Seq[Filter] = basicsFromJson(json).map(filtersFromJson(_).map(_.map(parseCondition))).getOrElse(Seq())
    def advancedFilters(json: JsValue) : Filter = (json \ "advanced").asOpt[Seq[JsValue]].map(_.map(parseCondition)).getOrElse(Seq())


    def parseCondition(jsonCondition: JsValue) : Condition = parsers.flatMap(_(jsonCondition)).toList.head

    def filtersFromJson(json: Seq[JsValue]) = json.map(_.\("executivesFilters").as[Seq[JsValue]])
    def basicsFromJson(json: JsValue) : Option[Seq[JsValue]] = (json \ "executives").asOpt[Seq[JsValue]]
  }

  val translations = Map(
    "role" -> Path('executives, 'functionalMatches, 'primary)
  ).mapValues(path => (path ++ Path('value)).joinWithDots)

  trait Parser {
    def apply(json: JsValue) : Option[Condition]
    def property(json:JsValue) = translations.get((json \ "key").as[String]).getOrElse((json \ "key").as[String])
  }

  object EqualParser extends Parser {
    def apply(json: JsValue) = {
      val value: JsValue = json \ "value"
      value.asOpt[Double].orElse(value.asOpt[String]).map(EqualCondition(property(json), _))
    }
  }

  object ConditionsParser extends Parser {
    def apply(json: JsValue) = (json \ "operators").asOpt[Seq[JsValue]].map(it => ConditionWithOperators(property(json), operators(it)))

    def operators(operators: Seq[JsValue]) : Seq[Operator] = operators.map(operator)
    def operator(condition : JsValue) : Operator =  "$" + (condition \ "operator").as[String] -> (condition \ "value").as[Double]
  }
}
