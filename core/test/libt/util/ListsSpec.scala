package libt.util
import org.scalatest.FunSuite
import libt.util.Lists._

class IndexedTraversablesSpec extends FunSuite {
  test("can traverse with index") {
    var result = List[Int]()
    Seq("foo", "bar", "baz").foreachWithIndex { (x, index) => 
      result ++= Seq(index)
    }
    assert(result === Seq(0, 1, 2))
  }

}