package output.calculation

import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner
import libt.reduction._
import libt._

@RunWith(classOf[JUnitRunner])
class CalculationSpec extends FunSpec {

  describe("calculation usage") {
    it("should sum a collection of features inside a Model") {
      val model =
        Model(
          'foo -> Col(
            Model('bar -> Value(1: BigDecimal)),
            Model('bar -> Value(2: BigDecimal))))

      assert(Sum(Path('foo, *, 'bar)).reduce(model) === 3)
    }

    it("should average a collection of features inside a Model") {
      val model =
        Model(
          'foo -> Col(
            Model('bar -> Value(2: BigDecimal)),
            Model('bar -> Value(3: BigDecimal)),
            Model('bar -> Value(4: BigDecimal))))

      assert(Average(Path('foo, *, 'bar)).reduce(model).toInt === 3)
    }

    it("should reduce a custom calculation") {
      val model =
        Model(
          'foo -> Model('val1 -> Value(2: BigDecimal),
            'val2 -> Value(3: BigDecimal),
            'val3 -> Value(4: BigDecimal),
            'val4 -> Value(10: BigDecimal)))

      def customReduction(model: Model) = {
        implicit def element2BigDecimal(elem: Element): BigDecimal =
          elem.asValue[BigDecimal].value.get
          
        model('val1) - model('val2) + model('val3) + model('val4)
      }

      val reductor =
        CustomReduction(
          Path('foo),
          customReduction)

      assert(reductor.reduce(model) === 13)
    }
  }
}