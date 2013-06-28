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

}
