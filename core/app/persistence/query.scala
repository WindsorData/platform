package persistence

import com.mongodb.casbah.Imports._
import persistence._
import org.joda.time.DateTime
import libt.Path

/**
 * Both generic and domain specific queries 
 * against a Windsor DB.
 */
//TODO Split generic and specific queries
object query {

  type Filter = Seq[Condition]
  type Operator = (String, Any)

  case class QueryExecutives(year: Int, executives: Seq[Filter], advanced: Filter) {
    def exampleExecutives = {
      executives match {
        case Nil => Seq(filterLastYear.asMongoQuery)
        case _ => executives.map {it => ElemMatch("executives", it ++ advanced).asMongoQuery ++ filterLastYear.asMongoQuery}
      }
    }
    def query = MongoDBObject("$or" -> exampleExecutives)

    def apply(db: Persistence) = db.find(query)
    override def toString = query.toString

    def filterLastYear = EqualCondition(Path('disclosureFiscalYear , 'value).joinWithDots, year)
  }

  trait Condition {
    def asMongoQuery : DBO
  }

  case class EqualCondition(property: String, value: Any) extends Condition {
    def asMongoQuery = MongoDBObject(property -> value)
  }

  case class ElemMatch(property: String, conditions: Seq[Condition]){
    def asMongoQuery = MongoDBObject(property -> MongoDBObject("$elemMatch" -> conditions.map(_.asMongoQuery).reduce(_ ++ _)))
  }

  case class ConditionWithOperators(property: String, operators: Seq[Operator]) extends Condition {
    def asMongoQuery = MongoDBObject(property -> MongoDBObject(operators.toList))
  }

}