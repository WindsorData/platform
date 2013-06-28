package filter

import play.api.libs.json._
import org.scalatest.FunSuite
import persistence.{ConditionWithOperators, EqualCondition, Operator}
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
    assert(conditions(0) === ("$gt" -> 10))
    assert(conditions(1) === ("$lt" -> 22))
  }

  test("can parse a complete query") {
    val json =
      """ {
          "basics": [
              {
                "filters": [
                          {"key": "firstName", "value": "den"},
                          {"key": "lastName", "value": "ritch"},
                          {"key": "salary", "operators":[ {"operator":"gt","value":5}, {"operator":"lt","value":10} ]}
                      ]
              }
            ]
        }
      """
    val query = ParserJsonQuery.query(stringMultilineToJson(json))
    assert(query.basics.size === 1)
    assert(query.basics(0).size === 3)

    val equalCondition = query.basics.head(1).asInstanceOf[EqualCondition]
    val complexCondition = query.basics.head.last.asInstanceOf[ConditionWithOperators]
    assert(equalCondition === EqualCondition("lastName", "ritch"))
    assert(complexCondition === ConditionWithOperators("salary", Seq(("$gt" -> 5), ("$lt" -> 10))))
  }

  test("can parse a list of filters") {
    val json = """[ {"filters": []}, {"filters": []}, {"filters": []}]"""
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
    assert(complexCondition === ConditionWithOperators("foo.bar", Seq(("$lt" -> 3))))
  }


}
