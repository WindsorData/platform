package persistence

import com.mongodb.casbah.Imports._
import persistence._
import org.joda.time.DateTime
import libt.Path

object query {

  type Filter = Seq[Condition]
  type Operator = (String, Any)

  case class QueryExecutives(executives: Seq[Filter], advanced: Filter) {
    def exampleExecutives = executives.map {it => (it ++ advanced :+ filterLastYear).map(_.asMongoQuery).reduce(_ ++ _)}
    def query = MongoDBObject("$or" -> exampleExecutives)

    def apply()(implicit db: MongoDB) = findByExample(db, query)
    override def toString = query.toString

    def filterLastYear = EqualCondition(Path('disclosureFiscalYear , 'value).joinWithDots, new DateTime().minusYears(1).getYear)
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