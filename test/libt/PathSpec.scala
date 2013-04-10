package libt

import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PathSpec extends FunSpec {

  describe("schema traversing") {
    it("should traverse values") {
      assert(TString(Path()) === TString)
    }

    it("should traverse models") {
      assert(TModel('foo -> TString)(Path('foo)) === TString)
    }

    it("should traverse collections") {
      assert(TCol(TString)(Path(0)) === TString)
    }

    it("should traverse elements recursively") {
      val schema = TModel('foo -> TModel('bar -> TString))
      assert(schema(Path('foo, 'bar)) === TString)
    }
  }

}