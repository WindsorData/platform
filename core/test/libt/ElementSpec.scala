package libt

import org.scalatest.FunSpec

class ElementSpec extends FunSpec {

  describe("applySeq") {
    it("should return an empty seq for a path with first part missing") {
      assert(Model().applySeq('a) === Seq())
    }
    it("should return an empty seq for a path with second part missing") {
      assert(Model('a -> Model()).applySeq(Path('a, 'b)) === Seq())
    }
    it("should return an empty seq for a path with first and second part missing") {
      assert(Model().applySeq(Path('a, 'b)) === Seq())
    }
    it("should return a singleton seq for a path to a sinle value") {
      assert(Model('a -> Value(1)).applySeq('a) === Seq(Value(1)))
    }
    it("should return a singleton seq for a path to a single model") {
      assert(Model('a -> Model('b -> Value(1))).applySeq('a) === Seq(Model('b -> Value(1))))
    }
    it("should return spread elements under *") {
      assert(Model('a -> Col(Value(1), Value(2))).applySeq(Path('a, *)) === Seq(Value(1), Value(2)))
    }
  }

  describe("merge") {
    it("should be equivalent to ++ for models when there are no collisions") {
      val aModel = Model('a -> Value("a"))
      val otherModel = Model('b -> Value("b"))
      assert(aModel.merge(otherModel) === aModel ++ otherModel)
    }

    it("should resolve collisions in models by merging entries") {
      val aModel = Model('a -> Model('b -> Value("b")))
      val otherModel = Model('a -> Model('c -> Value("c")))
      assert(aModel.merge(otherModel) ===
        Model(
          'a -> Model(
            'b -> Value("b"),
            'c -> Value("c"))))
    }

    it("should be equivalent to zip with merge for cols of same size") {
      val aModel = Model('a -> Value("a"))
      val otherModel = Model('b -> Value("b"))
      assert(Col(aModel).merge(Col(otherModel)) === Col(aModel merge otherModel))
    }

    it("should not discard extra values when cols are of different sizes") {
      val aModel = Model('a -> Value("a"))
      val otherModel = Model('b -> Value("b"))
      assert(Col(aModel).merge(Col()) === Col(aModel))
      assert(Col(aModel).merge(Col(otherModel, otherModel)) === Col(aModel merge otherModel, otherModel))
    }

    it("should keep the value to merge for values") {
      val aValue = Value("a")
      val otherValue = Value("b")
      assert(aValue.merge(otherValue) === Value("b"))
    }

    it("should fail for incompatible types") {
      intercept[IllegalArgumentException] {
        Model().merge(Value("a"))
      }
    }
  }

}