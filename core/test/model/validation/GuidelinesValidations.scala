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
	  it("should validate digits on number of shares and multiple of salary") {
	    def model(numberOfShares: BigDecimal, multOfSalary: BigDecimal) =
	      createModel(
	          Model(
	              'numberOfShares -> Value(numberOfShares),
	              'multipleOfSalary -> Value(multOfSalary)))
	              
	    assert(guidelines.guidelinesDigitValidation(model(1,1)).isDoubtful)
	    assert(!guidelines.guidelinesDigitValidation(model(100,1)).isDoubtful)
	    assert(guidelines.guidelinesDigitValidation(model(1,100)).isDoubtful)
	    assert(guidelines.guidelinesDigitValidation(model(100,100)).isDoubtful)
	  }
	}
  
  	describe("st bonus plan validations") {
  	  it("should validate the existance of corporate") {
  	    def model(use: Boolean) = 
  	      createModel(
  	          Model(
  	              'scope -> Model(
  	                  'corporate -> Model(
  	                      'use -> Value(use)))))
  	    assert(!guidelines.scopeValidation(model(true)).isDoubtful)
  	    assert(guidelines.scopeValidation(model(false)).isDoubtful)
  	  }
  	  
  	  it("should validate metrics") {
  	    def model(select: Seq[Element], typein: Seq[Element]) = 
  	      createModel(
  	          Model(
  	              'metrics -> Model(
  	                  'select -> Col(select: _*),
  	                  'typeIn -> Col(typein: _*))))
  	                
  	    assert(guidelines.metricsValidation(model(Seq(), Seq())).isDoubtful)
  	    assert(!guidelines.metricsValidation(model(Seq(), Seq(Model('a -> Value())))).isDoubtful)
  	    assert(!guidelines.metricsValidation(model(Seq(Model('b -> Value(1))), Seq(Model('a -> Value())))).isDoubtful)
  	  }
  	}
}