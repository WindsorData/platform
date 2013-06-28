package persistence

import com.mongodb.casbah.Imports._

object query {

  type Filter = Seq[Condition]
  type Operator = (String, Any)

  case class QueryExecutives(executives: Seq[Filter], advanced: Filter) {
    def exampleExecutives = executives.map {it => (it ++ advanced).map(_.condition).reduce(_ ++ _)}
    def query = MongoDBObject("$or" -> exampleExecutives)
  }

  trait Condition {
    def condition : DBO
  }

  case class EqualCondition(property: String, value: Any) extends Condition {
    def condition = MongoDBObject(property -> value)
  }

  case class ConditionWithOperators(property: String, operators: Seq[Operator]) extends Condition {
    def condition = MongoDBObject(property -> MongoDBObject(operators.toList))
  }

}