package libt.reduction

import org.junit.runner.RunWith
import org.scalatest.FunSpec
import libt.reduction._
import libt._
import scala.math.BigDecimal.int2bigDecimal
import org.scalatest.junit.JUnitRunner

class CalculationSpec extends FunSpec {

  val modelWithCol =
    Model(
      'foo -> Col(
        Model('bar -> Value(2: BigDecimal)),
        Model('bar -> Value(3: BigDecimal)),
        Model('bar -> Value(4: BigDecimal))))

  val modelWithValues = 
    Model('val1 -> Value(2: BigDecimal),
        'val2 -> Value(3: BigDecimal),
        'val3 -> Value(4: BigDecimal),
        'val4 -> Value(10: BigDecimal))
        
  val modelWithModel = Model('foo -> modelWithValues)

  describe("calculation usage") {
    it("should sum a collection of features inside a Model") {
      assert(Sum(Path('foo, *, 'bar)).reduce(modelWithCol) === 9)
    }

    it("should average a collection of features inside a Model") {
      assert(Average(Path('foo, *, 'bar)).reduce(modelWithCol).toInt === 3)
    }

    it("should substract all feature inside a Model") {
      assert(SubstractAll(Path('foo), Path('val3), Path('val2), Path('val1)).reduce(modelWithModel) === -1)
    }
  }
}
