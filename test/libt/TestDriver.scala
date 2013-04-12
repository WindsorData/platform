package libt

import org.junit.runner.RunWith
import org.scalatest.FunSpec
import libt._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TestDriver extends FunSpec {

  describe("tmodel") {
    describe("can describe") {
      it("empty models") {
        TModel()
      }

      it("models with just attributes") {
        TModel(
          'foo -> TString,
          'bar -> TNumber)
      }

      it("models with nested models") {
        TModel(
          'foo -> TModel(
            'bar -> TString))
      }

      it("models with collections ") {
        TModel(
          'foo -> TCol(TString),
          'bar -> TCol(
            TModel(
              'bar -> TString,
              'baz -> TString)))
      }
    }

    describe("can instantiate") {
      it("empty models") {
        Model()
      }

      it("models with attributes") {
        Model('foo -> Value("bar"))
      }

      it("models with collections") {
        Model('foo -> Col(
          Value("bar"),
          Value("baz")))
      }

      it("models with nested models") {
        Model('foo ->
          Model('baz ->
            Value("bar")))
      }
    }
  }

  describe("value") {
    it("can map") {
      assert(Value(4).map(_ + 1) === Value(5))
    }
    it("can perform orDefault") {
      assert(Value(4).orDefault(5) === Value(4))
      assert(Value().orDefault(5) === Value(5))
    }
  }
}