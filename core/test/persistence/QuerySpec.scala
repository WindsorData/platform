package persistence

import org.scalatest.FunSuite
import com.mongodb.casbah.commons.MongoDBObject
import org.joda.time.DateTime
import persistence.query._

class QuerySpec extends FunSuite {

  def conditionLastYear = s""""disclosureFiscalYear.value" : ${new DateTime().minusYears(1).getYear}"""

  test("can create query with only a condition") {
    val query = QueryExecutives(Seq(Seq(EqualCondition("firstName.value", "Robert H."))), Seq())
    val mongoQuery = query.exampleExecutives.head.toString
    assert(s"""{ "executives" : { "$$elemMatch" : { "firstName.value" : "Robert H."}} , ${conditionLastYear}}""" === mongoQuery)
  }

  test("can create a query with only a condition with operators") {
    val query = QueryExecutives(Seq(Seq(ConditionWithOperators("age.value", Seq("$gte" -> 10, "$lte" -> 11)))), Seq())
    val mongoQuery = query.exampleExecutives.head.toString
    assert(s"""{ "executives" : { "$$elemMatch" : { "age.value" : { "$$gte" : 10 , "$$lte" : 11}}} , ${conditionLastYear}}""" === mongoQuery)
  }

  test("can create a query with multiple conditions") {
    val query = QueryExecutives(Seq(Seq(
        EqualCondition("firstName.value", "Robert H."),
        ConditionWithOperators("year.value", Seq("$gte" -> 2010, "$lte" -> 2011))
    )), Seq())
    val mongoQuery = query.exampleExecutives.head.toString
    assert(s"""{ "executives" : { "$$elemMatch" : { "firstName.value" : "Robert H." , "year.value" : { "$$gte" : 2010 , "$$lte" : 2011}}} , ${conditionLastYear}}""" === mongoQuery)
  }

  test("can create a query with or conditions") {
    val query = QueryExecutives(Seq(
        Seq(EqualCondition("firstName.value", "Robert H.")),
        Seq(ConditionWithOperators("year.value", Seq("$gte" -> 2010)))
    ), Seq())
    val mongoQuery = query.toString
    assert(s"""{ "$$or" : [ { "executives" : { "$$elemMatch" : { "firstName.value" : "Robert H."}} , ${conditionLastYear}} , { "executives" : { "$$elemMatch" : { "year.value" : { "$$gte" : 2010}}} , ${conditionLastYear}}]}""" == mongoQuery)
  }

  test("can create a query with extra condition") {
    val query = QueryExecutives(
      Seq(Seq(EqualCondition("firstName.value", "Robert H."))),
      Seq(ConditionWithOperators("year.value", Seq("$gte" -> 2010)))
    )

    val mongoQuery = query.exampleExecutives.head.toString
    assert(s"""{ "executives" : { "$$elemMatch" : { "firstName.value" : "Robert H." , "year.value" : { "$$gte" : 2010}}} , ${conditionLastYear}}""" === mongoQuery)
  }

}
