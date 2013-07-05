package libt

import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

class PathSpec extends FunSpec {

  describe("schema traversing") {
    it("should traverse values") {
      assert(TString(Path()) === TString)
    }

    it("should traverse models") {
      assert(TModel('foo -> TString)(Path('foo)) === TString)
    }

    it("should traverse collection of values") {
      assert(TCol(TString)(Path(0)) === TString)
    }
    
    it("should traverse collection of models") {
      assert(TCol(TModel('a -> TString, 'b -> TBool))(Path(0, 'a)) === TString)
      assert(TCol(TModel('a -> TString, 'b -> TBool))(Path(100, 'b)) === TBool)
    }
    
    it("should traverse models with a single element collection"){
      assert(TModel('aModel -> TCol(TString))(Path('aModel, 0)) === TString)
    }
    
    it("should traverse models with a collection of models"){
      assert(TModel('aModel -> TCol(TModel('a -> TString)))(Path('aModel, 0, 'a)) === TString)
    }

    it("should traverse elements recursively") {
      val schema = TModel('foo -> TModel('bar -> TString))
      assert(schema(Path('foo, 'bar)) === TString)
    }
  }

}