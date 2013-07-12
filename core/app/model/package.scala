import libt._

package object model {
  import persistence._
  import model.ExecutivesGuidelines._
  import model.ExecutivesTop5._
  import model.ExecutivesSTBonusPlan._
  import model.ExecutivesSVTBSDilution._
  import model.ExecutivesBod._
  
   val TCompanyFiscalYear = TModel(
    'cusip -> TAny,
    'ticker -> TString,
    'name -> TString,
    'disclosureFiscalYear -> TInt,
    'def14a -> TInt,
    'tenK -> TInt,

    'grantTypes -> TGrantTypes,
    'usageAndSVTData -> TUsageAndSVTData,
    'bsInputs -> TBlackScholesInputs,
    'dilution -> TDilution,
    
    'executives -> TCol(TExecutive),
    'bod -> TCol(TBod),
    'guidelines -> TCol(TExecGuidelines),
    'stBonusPlan -> TCol(TExecSTBonusPlan))
}








