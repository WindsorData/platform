import libt.Path
import persistence.query._
import play.api.libs.json.JsValue

package object parser {

  object QueryParser {

    private val parsers = Stream(EqualParser, OperatorParser)

    def query(query: JsValue) : QueryExecutives =
      QueryExecutives(executivesFilters(query), advancedFilters(query))
    def executivesFilters(json: JsValue) : Seq[Filter] =
      basicsFromJson(json).map(filtersFromJson(_).map(_.map(parseCondition))).getOrElse(Seq())
    def advancedFilters(json: JsValue) : Filter =
      (json \ "advanced").asOpt[Seq[JsValue]].map(_.map(parseCondition)).getOrElse(Seq())

    private[parser] def parseCondition(jsonCondition: JsValue) : Condition =
      parsers.flatMap(_(jsonCondition)).toList.head

    private[parser] def filtersFromJson(json: Seq[JsValue]) =
      json.map(_.\("executivesFilters").as[Seq[JsValue]])

    private[parser] def basicsFromJson(json: JsValue) : Option[Seq[JsValue]] =
      (json \ "executives").asOpt[Seq[JsValue]]
  }

  trait ConditionParser {
    private val translations = Map(
      "role" -> Path('executives, 'functionalMatches, 'primary),
      "pay_rank" -> Path('executives, 'calculated, 'ttdcPayRank),
      "founder" -> Path('executives, 'founder),
      "trans_period" -> Path('executives, 'transitionPeriod)
    ).mapValues(path => (path ++ Path('value)).joinWithDots)

    /***Parses a json condition*/
    def apply(json: JsValue) : Option[Condition]
    /**Extracts a search key from the given condition, translating it if necessary */
    protected def parseAndTranslateKey(json:JsValue) =
      translations.get((json \ "key").as[String]).getOrElse((json \ "key").as[String])
  }

  object EqualParser extends ConditionParser {
    def apply(json: JsValue) = {
      val value: JsValue = json \ "value"
      value.asOpt[Double].orElse(value.asOpt[String]).map(EqualCondition(parseAndTranslateKey(json), _))
    }
  }

  object OperatorParser extends ConditionParser {
    def apply(json: JsValue) =
      (json \ "operators").asOpt[Seq[JsValue]].map(it => ConditionWithOperators(parseAndTranslateKey(json), operators(it)))

    def operators(operators: Seq[JsValue]) : Seq[Operator] = operators.map(operator)
    def operator(condition : JsValue) : Operator =
      "$" + (condition \ "operator").as[String] -> (condition \ "value").as[Double]
  }
}
