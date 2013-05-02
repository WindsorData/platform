package libt

import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner
import libt.spreadsheet._

@RunWith(classOf[JUnitRunner])
class TitlePathSpec extends FunSpec {

  describe("Features titles"){
    it("should return a title on a simple feature"){
      Feature(Path('foo)).title === Seq("Foo")
    }
    
    it("should return a title on a feature with multiple route paths"){
      Feature(Path('this, 'is, 'foo, 'bar)).title === Seq("This", "Is", "Foo", "Bar")
    }
    
    it("should return a title on a feature with multiple route/index paths"){
      Feature(Path('this, 1, 'foo, 2)).title === Seq("This", "1", "Foo", "2")
    }
    
    it("should return a well formed title for a camel case symbols"){
      Feature(Path('fooBar, 'needsToBe, 'aWellFormedTitle)).title === 
        Seq("Foo Bar", "Needs To Be", "A Well Formed Title")
    }
    
    it("should return no title on Gap"){
      Gap.title == None
    }
  }
}