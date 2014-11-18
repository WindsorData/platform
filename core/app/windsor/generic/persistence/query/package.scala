package windsor.generic.persistence

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
/**
 * Package object with generic querying classes
 */
package object query {

  type Filter = Seq[Condition]
  type Operator = (String, Any)

  trait Condition {
    def asMongoQuery: DBO
  }

  case class EqualCondition(property: String, value: Any) extends Condition {
    def asMongoQuery = MongoDBObject(property -> value)
  }

  case class ElemMatch(property: String, conditions: Seq[Condition]) {
    def asMongoQuery = MongoDBObject(property -> MongoDBObject("$elemMatch" -> conditions.map(_.asMongoQuery).reduce(_ ++ _)))
  }

  case class ConditionWithOperators(property: String, operators: Seq[Operator]) extends Condition {
    def asMongoQuery = MongoDBObject(property -> MongoDBObject(operators.toList))
  }
}