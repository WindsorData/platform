package libt

import org.scalatest.FunSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import libt._

@RunWith(classOf[JUnitRunner])
class MultiPathSpec extends FunSpec {
  
  describe("multipath model traversing") {
    it("should traverse Values on Cols") {
      assert(Col(Value(1), Value(2)).applySeq(Path(*)) === Seq(Value(1) , Value(2)))
    }
    
    it("should traverse Models on Cols") {
      val model = 
        Col(
          Model('foo -> Value(1)), 
          Model('foo -> Value(2)))
          
      assert(model.applySeq(Path(*, 'foo)) === Seq(Value(1) , Value(2)))
    }
    
    it("should traverse Models on a Model with some Col") {
      val model = 
        Model(
          'foo -> Col(
              Model('bar -> Value(1)),
              Model('bar -> Value(2))))
              
      assert(model.applySeq(Path('foo, *, 'bar)) === Seq(Value(1) , Value(2)))
    }
  }

}