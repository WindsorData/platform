import libt._

package object model {
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
    'disclosureFiscalYearDate -> TDate,
    'def14a -> TDate,
    'tenK -> TDate,
    'otherDocs -> TCol(TModel('type -> TString, 'date -> TDate)),

    'grantTypes -> TGrantTypes,
    'companyDB -> TModel(
      'usageAndSVTData -> TUsageAndSVTData,
      'bsInputs -> TBlackScholesInputs,
      'dilution -> TDilution),
    
    'executives -> TCol(TExecutive),
    'bod -> TCol(TBod),
    'guidelines -> TCol(TExecGuidelines),
    'stBonusPlan -> TCol(TExecSTBonusPlan))
}








