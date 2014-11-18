package persistence


import com.mongodb.casbah.Imports._

import libt.Path

import windsor.generic.persistence.query._
import windsor.generic.persistence._

/**
 * Package object with specific Windsor queries.
 */
package object query {

  case class QueryExecutives(year: Int, executives: Seq[Filter], advanced: Filter) {
    def exampleExecutives = {
      executives match {
        case Nil => Seq(filterLastYear.asMongoQuery)
        case _ => executives.map {it => ElemMatch("executives", it ++ advanced).asMongoQuery ++ filterLastYear.asMongoQuery}
      }
    }
    def query = MongoDBObject("$or" -> exampleExecutives)

    def apply(db: Database) = db.find(query)
    override def toString = query.toString

    def filterLastYear = EqualCondition(Path('disclosureFiscalYear , 'value).joinWithDots, year)
  }
}