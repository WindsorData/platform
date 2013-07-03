package persistence

import com.mongodb.casbah.Imports._
import persistence._

object query {

  type Filter = Seq[Condition]
  type Operator = (String, Any)

  case class QueryExecutives(executives: Seq[Filter], advanced: Filter) {
    def exampleExecutives = executives.map {it => (it ++ advanced).map(_.asMongoQuery).reduce(_ ++ _)}
    def query = MongoDBObject("$or" -> exampleExecutives)

    def apply()(implicit db: MongoDB) = findByExample(db, query)
    override def toString = query.toString
  }

  trait Condition {
    def asMongoQuery : DBO
  }

  case class EqualCondition(property: String, value: Any) extends Condition {
    def asMongoQuery = MongoDBObject(property -> value)
  }

  case class ConditionWithOperators(property: String, operators: Seq[Operator]) extends Condition {
    def asMongoQuery = MongoDBObject(property -> MongoDBObject(operators.toList))
  }

}