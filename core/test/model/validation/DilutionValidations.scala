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
        Model(
        'usageAndSVTData -> 
            Model('avgSharesOutstanding -> 
            	Model(
            	    'year1 -> Value(year1),
            	    'year2 -> Value(year2),
            	    'year3 -> Value(year3))))
      assert(dilution.averageSharesValidation(model(100,10,1)).isDoubtful)
      assert(!dilution.averageSharesValidation(model(3000000,2000000,1000000)).isDoubtful)
    }
  }
}