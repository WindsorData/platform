package util
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.fixture.FlatSpec
import org.scalatest.path.FunSpec
import org.scalatest.FunSuite
import IndexedTraversables._

@RunWith(classOf[JUnitRunner])
class IndexedTraversablesSpec extends FunSuite {
  test("can traverse with index") {
    var result = List[Int]()
    Seq("foo", "bar", "baz").foreachWithIndex { (x, index) => 
      result ++= Seq(index)
    }
    assert(result === Seq(0, 1, 2))
  }

}