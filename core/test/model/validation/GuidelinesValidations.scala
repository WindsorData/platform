package model.validation

import org.scalatest.FunSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import libt._
import model.mapping.guidelines

@RunWith(classOf[JUnitRunner])
class GuidelinesValidations extends FunSpec{

  def createModel(model: Model) =
    Model(
      'disclosureFiscalYear -> Value(2013),
      'guidelines -> Col(
        Model(
          'firstName -> Value("foo"),
          'lastName -> Value("bar")) ++ model))
            
	describe("guidelines validations") {
	  it("should validate ") {
	    def model(numberOfShares: BigDecimal, multOfSalary: BigDecimal) =
	      createModel(
	          Model(
	              'numberOfShares -> Value(numberOfShares),
	              'multipleOfSalary -> Value(multOfSalary)))
	              
	    assert(guidelines.guidelinesThreeDigitValidation(model(1,1)).isDoubtful)
	    assert(guidelines.guidelinesThreeDigitValidation(model(100,1)).isDoubtful)
	    assert(guidelines.guidelinesThreeDigitValidation(model(1,100)).isDoubtful)
	    assert(!guidelines.guidelinesThreeDigitValidation(model(100,100)).isDoubtful)
	  }
	}
}