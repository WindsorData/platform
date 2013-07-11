package model.validation

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSpec
import libt._
import model.mapping.top5
import java.util.Date
import java.util.Calendar
import org.joda.time.DateTime

class Top5ValidationsSpec extends FunSpec {
  def createModel(model: Model, year: Int = 2013, firstName: String = "foo", lastName: String = "bar") =
    Model(
      'disclosureFiscalYear -> Value(year),
      'executives -> Col(
        Model(
          'firstName -> Value(firstName),
          'lastName -> Value(lastName)) ++ model))
  
  describe("top 5 workbook validations") {
    def model(first: String, last: String, year: Int, primary: Value[String], transition: Value[String]) =
      createModel(
        Model(
          'functionalMatches -> Model(
            'primary -> primary),
          'transitionPeriod -> transition), year, first, last)
	it("should validation executive's transition period between different fiscal years") {
	  assert(
	      top5.transitionPeriodValidation(
	      Seq(model("Example","A",2012, Value("foo"), Value("No")), 
	          model("Example","A",2011, Value("bar"), Value("Yes")), 
	          model("Example","A",2010, Value("bar"), Value("no")))).forall(_.isValid))
	  assert(
	      top5.transitionPeriodValidation(
	      Seq(model("Example","B",2012, Value("foo"), Value("No")), 
	          model("Example","B",2011, Value("foo"), Value("No")), 
	          model("Example","B",2010, Value("bar"), Value("Yes")))).forall(_.isValid))
	  assert(
	      top5.transitionPeriodValidation(
	      Seq(model("Example","C",2012, Value("foo"), Value("No")), 
	          model("Example","C",2011, Value("bar"), Value("No")), 
	          model("Example","C",2010, Value("bar"), Value("No")))).exists(_.isInvalid))
	      
	  assert(
	      top5.transitionPeriodValidation(
	      Seq(model("Example","D",2012, Value("foo"), Value("No")), 
	          model("Example","D",2011, Value("foo"), Value("No")), 
	          model("Example","D",2010, Value("bar"), Value("No")))).exists(_.isInvalid))	          
	}

    it("should validate executives with the same first name and last name between different fiscal years") {
      assert(
        top5.transitionPeriodValidation(
          Seq(model("Example", "A", 2012, Value("foo"), Value("No")),
            model("Example", "B", 2011, Value("bar"), Value("No")),
            model("Example", "B", 2010, Value("bar"), Value("No")))).forall(_.isValid))
    }
    
    it("should validate options grant dates with grant types max term") {
     def optionGrant(grantDate: Value[Date], expireDate: Value[Date]) =
      createModel(
        model = Model(
            'optionGrants -> Col(
                Model(
                    'grantDate -> grantDate,
                    'expireDate -> expireDate))))
     def grantType(maxTerm: Value[BigDecimal]) =
      Model(
          'disclosureFiscalYear -> Value(2013),
          'grantTypes -> Model(
            'stockOptions -> Model(
                'maxTerm -> maxTerm)))
                
     assert(top5.optionGrantsVsGrantType(
         Seq(grantType(Value()), optionGrant(Value(),Value()))).forall(_.isValid))
     assert(top5.optionGrantsVsGrantType(
         Seq(grantType(Value(1: BigDecimal)), 
             optionGrant(
                 Value(new DateTime().plusYears(1).toDate),
                 Value(new DateTime().toDate)))).forall(_.isValid))
     assert(top5.optionGrantsVsGrantType(
         Seq(grantType(Value(1: BigDecimal)), 
             optionGrant(
                 Value(new DateTime().plusYears(2).toDate),
                 Value(new DateTime().toDate)))).exists(_.isDoubtful))
    }
    
    it("should validate timeVestRs in carried interest") {
      def model(grantDate: Value[Int], timeVest: Value[Int], year: Int) =
        createModel(
            year = year,
            model = Model(
            'timeVestRS -> Col(
                Model(
                'grantDate -> grantDate)),
            'carriedInterest -> Model(
                'outstandingEquityAwards -> Model(
                    'timeVestRS -> timeVest))))
                   
       assert(top5.execDbTimeVestValidation(
           Seq(model(Value(1), Value(1), 2013),
               model(Value(), Value(), 2012)))
               .forall(_.isValid))
       assert(top5.execDbTimeVestValidation(
           Seq(model(Value(1), Value(), 2013),
               model(Value(), Value(), 2012)))
               .exists(_.isDoubtful))
    }
  }
  
  describe("top 5 sheet validations") {

    it("should validate BOD with CEO primary functional match") {
      def model(primary: String) =
        createModel(
        Model('functionalMatches ->
              Model('primary -> Value(primary),
                'bod -> Value())))
                
      assert(top5.bodValidation(model("CEO (Chief Executive Officer)")).isDoubtful)
      assert(!top5.bodValidation(model("something")).isDoubtful)
    }

    it("should validate when base salary is 1 and the executive is a founder or not") {
      def model(salary: BigDecimal) =
        createModel(
        Model(
          'cashCompensations -> Model(
            'baseSalary -> Value(salary)),
          'founder -> Value()))

      assert(top5.founderValidation(model(1)).isDoubtful)
      assert(!top5.founderValidation(model(10)).isDoubtful)
    }

    it("should validate current base salary and target bonus with next fiscal year data") {
      def model(nextBaseSalary: BigDecimal, nextTargetBonus: BigDecimal) = 
        createModel(
        Model(
            'cashCompensations -> Model(
              'baseSalary -> Value(200: BigDecimal),
              'targetBonus -> Value(10: BigDecimal),
              'nextFiscalYearData -> Model(
                'baseSalary -> Value(nextBaseSalary),
                'targetBonus -> Value(nextTargetBonus)))))
        
      assert(top5.nextFiscalYearDataValidation(model(100,1)).isDoubtful)
      assert(!top5.nextFiscalYearDataValidation(model(300,100)).isDoubtful)
    }

    it("should validate perf cash") {
      def model(targetValue: Value[BigDecimal], payout: Value[BigDecimal]) =
        createModel(
        Model(
            'performanceCash -> Model(
              'targetValue -> targetValue,
              'payout -> payout)))
              
      assert(top5.perfCashValidation(model(Value(1),Value(1))).isInvalid)
      assert(top5.perfCashValidation(model(Value(),Value(1))).isValid)
      assert(top5.perfCashValidation(model(Value(1),Value())).isValid)
      assert(top5.perfCashValidation(model(Value(),Value())).isValid)
    }
    
    it("should validate owned shares values with its beneficial ownership") {
      def model(beneficial: BigDecimal) =
        createModel(
        Model(
          'carriedInterest -> Model(
            'ownedShares -> Model(
              'beneficialOwnership -> Value(beneficial),
              'options -> Value(10: BigDecimal),
              'unvestedRestrictedStock -> Value(10: BigDecimal),
              'disclaimBeneficialOwnership -> Value(10: BigDecimal),
              'heldByTrust -> Value(10: BigDecimal)))))
        
      assert(top5.ownedSharesValidation(model(5)).isDoubtful)
      assert(!top5.ownedSharesValidation(model(20)).isDoubtful)
    }
    
    it("should validate if transition period is not empty") {
      def model(transition: Value[String]) =
        createModel(
        Model(
          'transitionPeriod -> transition))
        
      assert(top5.nonEmptyTransitionPeriods(model(Value())).isInvalid)
      assert(top5.nonEmptyTransitionPeriods(model(Value("No"))).isValid)
      assert(top5.nonEmptyTransitionPeriods(model(Value("Yes"))).isValid)
    }
    
    it("should validate if salary has more than 3 digits or not") {
      def model(salary: BigDecimal) = 
        createModel(
         Model(
          'cashCompensations -> Model(
            'baseSalary -> Value(salary))))

      assert(top5.salaryValidation(model(10)).isDoubtful)
      assert(!top5.salaryValidation(model(100)).isDoubtful)
      assert(!top5.salaryValidation(model(9999)).isDoubtful)
    }
    
    it("should validate on time vest rs, if price * number == value") {
      def model(price: Value[BigDecimal], number: Value[BigDecimal], value: Value[BigDecimal]) =
        createModel(Model(
            'timeVestRS -> Col(
                Model(
                    'number -> number,
                    'price -> price,
                    'value -> value))))
      assert(top5.timeVestRsValueValidation(model(Value(2),Value(2000),Value(4))).isValid)
      assert(top5.timeVestRsValueValidation(model(Value(),Value(),Value())).isValid)
      assert(top5.timeVestRsValueValidation(model(Value(),Value(1),Value(2))).isValid)
      assert(top5.timeVestRsValueValidation(model(Value(2.24),Value(3000.76),Value(6.722))).isValid)
      assert(top5.timeVestRsValueValidation(model(Value(2),Value(3000),Value(4))).isInvalid)
    }
    
    it("should validate Options Exercisable") {
      def model(options: Value[BigDecimal], vested: Value[BigDecimal], unvested: Value[BigDecimal]) =
        createModel(Model(
            'carriedInterest -> Model(
                'ownedShares -> Model(
                    'options -> options),
                'outstandingEquityAwards -> Model(
                    'vestedOptions -> vested,
                    'unvestedOptions -> unvested))))
      assert(top5.optionsExercisableValidation(model(Value(0), Value(), Value())).isValid)
      assert(top5.optionsExercisableValidation(model(Value(0), Value(), Value(0))).isValid)
      assert(top5.optionsExercisableValidation(model(Value(0), Value(0), Value())).isValid)
      assert(top5.optionsExercisableValidation(model(Value(0), Value(0), Value(0))).isValid)
      assert(top5.optionsExercisableValidation(model(Value(0), Value(1), Value(2))).isInvalid)
      assert(top5.optionsExercisableValidation(model(Value(0), Value(), Value(2))).isInvalid)
      assert(top5.optionsExercisableValidation(model(Value(0), Value(0), Value(2))).isInvalid)
    }
  }
}
