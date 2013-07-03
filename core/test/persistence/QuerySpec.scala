package persistence

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import persistence.query._
import com.mongodb.casbah.commons.MongoDBObject

@RunWith(classOf[JUnitRunner])
class QuerySpec extends FunSuite {

  test("can create query with only a condition") {
    val query = Query(Seq(Seq(EqualCondition("executives.firstName.value", "Robert H."))))
    val mongoQuery = query.orConditions.head.toString
    assert("""{ "executives.firstName.value" : "Robert H."}""" == mongoQuery)
  }

  test("can create a query with only a condition with operators") {
    val query = Query(Seq(Seq(ConditionWithOperators("disclosureFiscalYear.value", Seq("$gte" -> 2010, "$lte" -> 2011)))))
    val mongoQuery = query.orConditions.head.toString

    assert("""{ "disclosureFiscalYear.value" : { "$gte" : 2010 , "$lte" : 2011}}""" === mongoQuery)
  }

  test("can create a query with multiple conditions") {
    val query = Query(Seq(Seq(
        EqualCondition("executives.firstName.value", "Robert H."),
        ConditionWithOperators("disclosureFiscalYear.value", Seq("$gte" -> 2010, "$lte" -> 2011))
    )))
    val mongoQuery = query.orConditions.head.toString

    assert("""{ "executives.firstName.value" : "Robert H." , "disclosureFiscalYear.value" : { "$gte" : 2010 , "$lte" : 2011}}""" === mongoQuery)
  }

  test("can create a query with or conditions") {
    val query = Query(Seq(
        Seq(EqualCondition("executives.firstName.value", "Robert H.")),
        Seq(ConditionWithOperators("disclosureFiscalYear.value", Seq("$gte" -> 2010)))
    ))
    val mongoQuery = query.query.toString

    assert("""{ "$or" : [ { "executives.firstName.value" : "Robert H."} , { "disclosureFiscalYear.value" : { "$gte" : 2010}}]}""" == mongoQuery)
  }

}
