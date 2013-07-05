package libt

import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner
import libt.spreadsheet._

class TitlePathSpec extends FunSpec {

  describe("Features titles"){
    it("should return a title on a simple feature"){
      assert(Path('foo).titles === Seq("Foo"))
    }
    
    it("should return a title on a feature with multiple route paths"){
      assert(Path('this, 'is, 'foo, 'bar).titles === Seq("This", "Is", "Foo", "Bar"))
    }
    
    it("should return a title on a feature with multiple route/index paths"){
      assert(Path('this, 1, 'foo, 2).titles === Seq("This", "1", "Foo", "2"))
    }
    
    it("should return a well formed title for a camel case symbols"){
      assert(Path('fooBar, 'needsToBe, 'aWellFormedTitle).titles === 
        Seq("Foo Bar", "Needs To Be", "A Well Formed Title"))
    }
  }
}