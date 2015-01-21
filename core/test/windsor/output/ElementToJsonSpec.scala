package windsor.output

import org.scalatest.FlatSpec
import play.api.libs.json._
import libt.json._
import libt._

class ElementToJsonSpec extends FlatSpec {

  behavior of "JsonElementConverter"

    it should "answer null json for empty Value" in {
      assert(Value().asJson === JsNull)
    }

    it should "answer json value for non-empty Value" in {
      assert(Value(2.12).asJson === JsNumber(2.12))
      assert(Value("foo").asJson === JsString("foo"))
      assert(Value(true).asJson === JsBoolean(true))
    }

    it should "answer empty json object for empty Model" in {
      assert(Model().asJson === Json.parse("{}"))
    }

    it should "answer json object for a non-empty model" in {
      assert(Model('foo -> Value("bar")).asJson === Json.parse("{\"foo\": \"bar\"}"))
    }

    it should "answer empty json array for an empty col" in {
      assert(Col().asJson === JsArray(Seq()))
    }

    it should "answer json array for a non-empty col" in {
      assert(
        Col(Model('foo -> Value("bar")), Model('number -> Value(0))).asJson ===
        Json.parse("""[{"foo" : "bar"}, {"number": 0}]"""))
    }

    it should "answer json object for a model with all posible elements" in {
      assert(
        Model(
        'foo -> Value("bar"),
        'model -> Model(
          'bar -> Value("foo")),
        'col -> Col(Model(), Model('x -> Value(3)))).asJson ===
          Json.parse("""{"foo": "bar", "model": {"bar":"foo"}, "col": [{},{"x":3}]}"""))
    }
}
