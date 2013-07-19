package libt.persistence

import scala.math.BigDecimal.int2bigDecimal
import org.junit.runner.RunWith
import org.scalatest.BeforeAndAfter
import org.scalatest.FunSpec
import com.mongodb.casbah.commons.MongoDBList
import com.mongodb.casbah.commons.MongoDBObject
import org.scalatest.junit.JUnitRunner
import com.mongodb.DBObject
import libt._

class TestDriver extends FunSpec {

  describe("tmodel Persistence") {

    it("can marshall values") {
      val schema = TString
      assert(schema.marshall(
        Value(
          Some("value"),
          Some("calc"),
          Some("comment"),
          Some("note"),
          Some("link")))
        ===
        MongoDBObject(
          "value" -> "value",
          "calc" -> "calc",
          "comment" -> "comment",
          "note" -> "note",
          "link" -> "link"))
    }

    it("can unmarshall values") {
      val schema = TString
      assert(schema.unmarshall(MongoDBObject("value" -> "foo")) === Value("foo"))
    }
    
    it("can unmarshall BigDecimal") {
      val schema = TNumber
      assert(schema.unmarshall(MongoDBObject("value" -> 1.0)) === Value(1.0))
    }
    
    it("can unmarshall Numeric Enums") {
      val schema = TNumberEnum(1.0,2.0,3.0)
      assert(schema.unmarshall(MongoDBObject("value" -> 1.0)) === Value(1.0))
    }

    it("can marshall empty models") {
      val schema = TModel()
      assert(schema.marshall(Model()) === MongoDBObject())
    }

    it("can unmarshall empty models") {
      val schema = TModel()
      assert(schema.unmarshall(MongoDBObject()) === Model())
    }

    it("compacts nones in values when marshalling") {
      val schema = TString
      assert(schema.marshall(Value("foo")) === MongoDBObject("value" -> "foo"))
    }

    ignore("compacts nones in models when marshalling") {
      val schema = TModel('foo -> TString)
      val model = Model('foo -> Value())
      assert(schema.marshall(model) === Model())
    }

    it("can convert simple models forth and back") {
      val schema = TModel(
        'foo -> TString,
        'bar -> TString,
        'baz -> TString)
      assertIdempotent(schema, Model(
        'foo -> Value("hello"),
        'bar -> Value("bar"),
        'baz -> Value()))
    }

    it("ignore attributes not contained in the schema") {
      val schema = TModel('foo -> TNumber)
      assert(schema.marshall(Model('bar -> Value(1))) === MongoDBObject())
    }

    it("can deal with decimals") {
      val schema = TModel('foo -> TNumber)
      assertIdempotent(schema, Model('foo -> Value(4: BigDecimal)))
    }

    it("can convert simple models with metadata forth and back") {
      val schema = TModel(
        'foo -> TString,
        'bar -> TString)
      assertIdempotent(schema, Model(
        'foo -> Value("hello"),
        'bar -> Value(Some("bar"), Some("baz"), Some("foo"))))
    }

    it("can marshall collections of values") {
      val schema = TCol(TString)
      assert(schema.marshall(
        Col(
          Value("foo"),
          Value("baz"))).toString
        ===
        MongoDBList(
          MongoDBObject("value" -> "foo"),
          MongoDBObject("value" -> "baz")).toString)
    }

    it("can convert collections of values") {
      val schema = TModel(
        'foo -> TString,
        'bar -> TCol(TString))
      assertIdempotent(schema, Model(
        'foo -> Value("hello"),
        'bar -> Col(Value("foo"), Value("baz"))))
    }

    it("can convert collections of models") {
      val schema = TModel(
        'foo -> TString,
        'bar -> TCol(
          TModel(
            'baz -> TNumber)))
      assertIdempotent(schema, Model(
        'foo -> Value("hello"),
        'bar -> Col(
          Model(
            'baz -> Value(3: BigDecimal)))))
    }
  }

  def assertIdempotent(schema: TElement, element: Element) =
    assert(schema.unmarshall(schema.marshall(element)) === element)

}