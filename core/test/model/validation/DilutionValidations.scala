package model.validation

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSpec
import libt._
import model.mapping.dilution

@RunWith(classOf[JUnitRunner])
class DilutionValidations extends FunSpec {

  describe("usage and svt data validations") {
    it("should validate average shares") {
      def model(year1: BigDecimal, year2: BigDecimal, year3: BigDecimal) = 
        Model('usageAndSVTData -> Model(
            'avgSharesOutstanding -> 
            	Model(
            	    'year1 -> Value(year1),
            	    'year2 -> Value(year2),
            	    'year3 -> Value(year3))))
      assert(dilution.averageSharesValidation(model(100,10,1)).isValid)
      assert(dilution.averageSharesValidation(model(3000000,2000000,1000000)).isDoubtful)
    }
    
    it("should validate options and full values") {
      def model(y1: BigDecimal, y2: BigDecimal, y3: BigDecimal) =
        Model(
            'usageAndSVTData -> Model(
            'optionsSARs -> Model(
                'granted -> Model(
                    'year1 -> Value(y1),
                    'year2 -> Value(y2),
                    'year3 -> Value(y3)),
                'cancelled -> Model(
                    'year1 -> Value(10000: BigDecimal),
                    'year2 -> Value(10000: BigDecimal),
                    'year3 -> Value(10000: BigDecimal))),
            'fullValue -> Model(
            		'sharesGranted -> Model(
            		    'year1 -> Value(10000: BigDecimal),
            		    'year2 -> Value(10000: BigDecimal),
            		    'year3 -> Value(10000: BigDecimal)),
            		'sharesCancelled -> Model(
            		    'year1 -> Value(10000: BigDecimal),
            		    'year2 -> Value(10000: BigDecimal),
            		    'year3 -> Value(10000: BigDecimal)))))
            		    
       assert(dilution.optionsAndFullValueValidation(model(1, 1, 1)).isDoubtful)
       assert(dilution.optionsAndFullValueValidation(model(10000, 100, 10)).isDoubtful)
       assert(!dilution.optionsAndFullValueValidation(model(10000, 10000, 10000)).isDoubtful)
    }
  }

  describe("dilution validations") {
    def model(option: BigDecimal, fullValue: BigDecimal, total: BigDecimal) =
      Model(
        'dilution -> Model(
          'awardsOutstandings -> Model(
            'option -> Value(option),
            'fullValue -> Value(fullValue),
            'total -> Value(total))))
            
    it("should validate Awards Outstanding: Total") {
      assert(dilution.totalValidation(model(2, 2, 2)).isInvalid)
      assert(dilution.totalValidation(model(1, 2, 3)).isValid)
    }

    it("should validate if option and fullvalues are 0") {
      assert(dilution.optionAndFullValuesValidation(model(0, 2, 2)).isDoubtful)
      assert(dilution.optionAndFullValuesValidation(model(0, 0, 2)).isDoubtful)
      assert(dilution.optionAndFullValuesValidation(model(2, 0, 2)).isDoubtful)
    }
  }
}