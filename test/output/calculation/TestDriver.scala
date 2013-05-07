package output.calculation

import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner
import libt.reduction._
import libt._

@RunWith(classOf[JUnitRunner])
class CalculationSpec extends FunSpec {

  val model =
    Model(
      'foo -> Col(
        Model('bar -> Value(2: BigDecimal)),
        Model('bar -> Value(3: BigDecimal)),
        Model('bar -> Value(4: BigDecimal))))

  describe("calculation usage") {
    it("should sum a collection of features inside a Model") {
      assert(Sum(Path('foo, *, 'bar)).reduce(model) === 9)
    }

    it("should average a collection of features inside a Model") {
      assert(Average(Path('foo, *, 'bar)).reduce(model).toInt === 3)
    }

    it("should substract all feature inside a Model") {
      val model =
        Model(
          'foo -> Model('val1 -> Value(2: BigDecimal),
        		  		'val2 -> Value(3: BigDecimal),
        		  		'val3 -> Value(4: BigDecimal),
        		  		'val4 -> Value(10: BigDecimal)))

      assert(SubstractAll(Path('foo), Path('val3), Path('val2), Path('val1)).reduce(model) === -1)
    }
  }
}