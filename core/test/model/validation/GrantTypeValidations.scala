package model.validation

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSpec
import libt._
import model.mapping.top5

@RunWith(classOf[JUnitRunner])
class GrantTypeValidations extends FunSpec {
  
  describe("grant types validations") {

    def createModel(cValue: Value[BigDecimal], minPayout: Value[BigDecimal], use: Value[Boolean] = Value(true)) =
      Model(
        'grantTypes -> Model(
          'stockOptions -> Model(
            'use -> use,
            'a -> Value(1),
            'b -> Value(2),
            'c -> cValue),
          'performanceEquityVesting -> Model(
            'minPayout -> minPayout)))
            
    it("should be invalid when stock option - use is true and stock option is incomplete") {
      assert(top5.grantTypeValidation(createModel(Value(), Value(1))).isInvalid)
      assert(top5.grantTypeValidation(createModel(Value(), Value(0))).isInvalid)
    }
    
    it("should be doubtful when minPayout is not 0") {
      assert(top5.grantTypeValidation(createModel(Value(3), Value(1))).isDoubtful)
    }
   
    it("should be valid when stock option - use is false or if stock option is complete") {
      assert(top5.grantTypeValidation(createModel(Value(3), Value(1), Value(false))).isValidOrDoubtful)
      assert(top5.grantTypeValidation(createModel(Value(3), Value(0))).isValidOrDoubtful)
    }
  }
}