package libt

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSpec

@RunWith(classOf[JUnitRunner])
class ModelSpec extends FunSpec {

  describe("subModel") {
    it("should return an empty model for an empty PK") {
      assert(
        Model('f -> Value("f"), 'g -> Value("g")).subModel(PK()) === Model())
    }

    it("should return a one element model for a single element, valid pk") {
      assert(
        Model('f -> Value("f"), 'g -> Value("g")).subModel(PK(Path('f))) === Model('f -> Value("f")))
    }

    it("should use the last part of the path as key when using non trivial paths") {
      assert(
        Model('f -> Model('g -> Value("g"))).subModel(PK(Path('f, 'g))) === Model('g -> Value("g")))
    }
  }

}