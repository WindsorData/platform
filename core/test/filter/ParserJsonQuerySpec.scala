package filter

import play.api.libs.json._
import org.scalatest.FunSuite
import persistence.query._
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith

@RunWith(classOf[JUnitRunner])
class ParserJsonQuerySpec extends FunSuite {

  def stringMultilineToJson(json: String): JsValue = Json.parse(json.stripMargin)

  test("can parse a list of operations") {
    val json = """[{"operator": "gt", "value": 10}, {"operator": "lt", "value": 22}]"""
    val jsonValue = stringMultilineToJson(json).as[List[JsValue]]
    val conditions = ParserJsonQuery.operatorsFromJson(jsonValue)

    assert(conditions.size === 2)
    assert(conditions(0) === "$gt" -> 10)
    assert(conditions(1) === "$lt" -> 22)
  }

  test("can parse a complete query") {
    val json =
      """ {
          "executives": [
              {
                "executivesFilters": [
                          {"key": "firstName", "value": "den"},
                          {"key": "lastName", "value": "ritch"},
                          {"key": "salary", "operators":[ {"operator":"gt","value":5}, {"operator":"lt","value":10} ]}
                      ]
              }
            ],
            "advanced": [
                {"key": "foo", "value": "bar"},
                {"key": "chi", "operators": [{"operator": "gt", "value": 4}]}
            ]
        }
      """
    val query = ParserJsonQuery.query(stringMultilineToJson(json))
    assert(query.executives.size === 1)
    assert(query.executives(0).size === 3)
    assert(query.advanced.size === 2)

    assert(query.executives.head(1).asInstanceOf[EqualCondition] === EqualCondition("lastName", "ritch"))
    assert(query.executives.head.last.asInstanceOf[ConditionWithOperators] === ConditionWithOperators("salary", Seq("$gt" -> 5, "$lt" -> 10)))
    assert(query.advanced(0).asInstanceOf[EqualCondition] === EqualCondition("foo", "bar"))
    assert(query.advanced(1).asInstanceOf[ConditionWithOperators] === ConditionWithOperators("chi", Seq("$gt" -> 4)))
  }

  test("can parse a list of executivesFilters") {
    val json = """[ {"executivesFilters": []}, {"executivesFilters": []}, {"executivesFilters": []}]"""
    val filters = ParserJsonQuery.filtersFromJson(stringMultilineToJson(json).as[List[JsValue]])
    assert(filters.size === 3)
  }

  test("can parse a equalCondition ") {
    val json = """ {"key": "foo.bar", "value": 15} """
    val condition = ParserJsonQuery.conditionFromJson(stringMultilineToJson(json))
    assert(condition === EqualCondition("foo.bar", 15.0))
  }

  test("can parse a condition with operators") {
    val json = """ {"key": "foo.bar", "operators": [{"operator": "lt", "value": 3}]} """
    val complexCondition = ParserJsonQuery.conditionFromJson(stringMultilineToJson(json))
    assert(complexCondition === ConditionWithOperators("foo.bar", Seq("$lt" -> 3)))
  }


}
