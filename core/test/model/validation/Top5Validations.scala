package model.validation

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSpec
import libt._
import model.mapping.top5
import org.junit.Assert

@RunWith(classOf[JUnitRunner])
class Top5Validations extends FunSpec {
  describe("top 5 validations") {

    def createModel(model: Model) =
      Model(
        'disclosureFiscalYear -> Value(2013),
        'executives -> Col(
          Model(
            'firstName -> Value("foo"),
            'lastName -> Value("bar")) ++ model))

    it("should validate BOD with CEO primary functional match") {
      def model(primary: String) =
        Model('functionalMatches ->
              Model('primary -> Value(primary),
                'bod -> Value()))
                
      assert(top5.bodValidation(createModel(model("CEO (Chief Executive Officer)"))).isDoubtful)
      assert(!top5.bodValidation(createModel(model("something"))).isDoubtful)
    }

    it("should validate when base salary is 1 and the executive is a founder or not") {
      def model(salary: BigDecimal) =
        Model(
          'cashCompensations -> Model(
            'baseSalary -> Value(salary)),
          'founder -> Value())

      assert(top5.founderValidation(createModel(model(1))).isDoubtful)
      assert(!top5.founderValidation(createModel(model(10))).isDoubtful)
    }

    it("should validate current base salary and target bonus with next fiscal year data") {
      def model(nextBaseSalary: BigDecimal, nextTargetBonus: BigDecimal) = 
        Model(
            'cashCompensations -> Model(
              'baseSalary -> Value(200: BigDecimal),
              'targetBonus -> Value(10: BigDecimal),
              'nextFiscalYearData -> Model(
                'baseSalary -> Value(nextBaseSalary),
                'targetBonus -> Value(nextTargetBonus))))
        
      assert(top5.nextFiscalYearDataValidation(createModel(model(100,1))).isDoubtful)
      assert(!top5.nextFiscalYearDataValidation(createModel(model(300,100))).isDoubtful)
    }

    it("should validate perf cash") {
      def model(targetValue: Value[BigDecimal], payout: Value[BigDecimal]) =
        Model(
            'performanceCash -> Model(
              'targetValue -> targetValue,
              'payout -> payout))
              
      assert(top5.perfCashValidation(createModel(model(Value(1),Value(1)))).isInvalid)
      assert(top5.perfCashValidation(createModel(model(Value(),Value(1)))).isValid)
      assert(top5.perfCashValidation(createModel(model(Value(1),Value()))).isValid)
      assert(top5.perfCashValidation(createModel(model(Value(),Value()))).isValid)
    }
    
    it("should validate owned shares values with its beneficial ownership") {
      def model(beneficial: BigDecimal) =
        Model(
          'carriedInterest -> Model(
            'ownedShares -> Model(
              'beneficialOwnership -> Value(beneficial),
              'options -> Value(10: BigDecimal),
              'unvestedRestrictedStock -> Value(10: BigDecimal),
              'disclaimBeneficialOwnership -> Value(10: BigDecimal),
              'heldByTrust -> Value(10: BigDecimal))))
        
      assert(top5.ownedSharesValidation(createModel(model(5))).isDoubtful)
      assert(!top5.ownedSharesValidation(createModel(model(20))).isDoubtful)
    }
    
    it("should validate if salary has more than 3 digits or not") {
      def model(salary: BigDecimal) = 
         Model(
          'cashCompensations -> Model(
            'baseSalary -> Value(salary)))

      assert(top5.salaryValidation(createModel(model(10))).isDoubtful)
      assert(!top5.salaryValidation(createModel(model(100))).isDoubtful)
      assert(!top5.salaryValidation(createModel(model(9999))).isDoubtful)
    }
    
  }
}