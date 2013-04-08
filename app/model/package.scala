import libt._
package object model {
  import persistence._

  val TCompanyFiscalYear = TModel(
    'ticker -> TString,
    'name -> TString,
    'disclosureFiscalYear -> TInt,
    'originalCurrency -> TString,
    'currencyConversionDate -> TDate,

    'executives -> TCol(
      TModel(
        'name -> TString,
        'title -> TString,
        'shortTitle -> TString,
        'functionalMatches ->
          TModel(
            'primary -> TEnum("CEO (Chief Executive Officer)",
              "CFO (Chief Financial Officer)",
              "COO (Chief Operating Officer)",
              "Sales",
              "Bus Dev (Business Development)",
              "CAO (Chief Admin Officer)",
              "GC (General Counsel-Legal)",
              "CAO (Chief Accounting Officer)",
              "CIO (Chief Investment-Asset Officer)",
              "CTO (Chief Technology Officer)",
              "Manufacturing",
              "Engineering",
              "Marketing",
              "CSO (Chief Science Officer)",
              "CSO (Chief Strategic Officer)",
              "CIO (Chief Information Officer)",
              "Product",
              "CRO (Chief Risk Officer)",
              "Treasurer/Secretary",
              "Executive Chairman",
              "Other"),
            'secondary -> TEnum(
              "Sales",
              "Bus Dev (Business Development)",
              "CAO (Chief Admin Officer)",
              "GC (General Counsel-Legal)",
              "CAO (Chief Accounting Officer)",
              "CIO (Chief Investment-Asset Officer)",
              "CTO (Chief Technology Officer)",
              "Manufacturing",
              "Engineering",
              "Marketing",
              "CSO (Chief Science Officer)",
              "CSO (Chief Strategic Officer)",
              "CIO (Chief Information Officer)",
              "Product",
              "CRO (Chief Risk Officer)",
              "Treasurer/Secretary",
              "GM (General Manager)",
              "Other"),
            'level -> TEnum(
              "President",
              "EVP (Executive Vice President)",
              "SVP (Senior Vice President)",
              "VP (Vice President)",
              "GM (General Manager)",
              "Group President"),
            'scope -> TEnum(
              "WW/Global/International",
              "US",
              "North America",
              "Europe",
              "Asia",
              "Americas"),
            'bod -> TEnum(
              "Chairman",
              "Vice Chairman",
              "Director")),
        'founder -> TString,

        'cashCompensations -> TModel(
          'baseSalary -> TNumber,
          'actualBonus -> TNumber,
          'targetBonus -> TNumber,
          'thresholdBonus -> TNumber,
          'maxBonus -> TNumber,
          'new8KData -> TModel(
            'baseSalary -> TNumber,
            'targetBonus -> TNumber)),

        'equityCompanyValue -> TModel(
          'optionsValue -> TNumber,
          'options -> TNumber,
          'exPrice -> TNumber,
          'bsPercentage -> TNumber,
          'timeVestRsValue -> TNumber,
          'shares -> TNumber,
          'price -> TNumber,
          'perfRSValue -> TNumber,
          'shares2 -> TNumber,
          'price2 -> TNumber,
          'perfCash -> TNumber),

        'carriedInterest -> TModel(
          'ownedShares -> TNumber,
          'vestedOptions -> TNumber,
          'unvestedOptions -> TNumber,
          'tineVest -> TNumber,
          'perfVest -> TNumber))))
}



//  def validFunctionalMatch =
//    functionalMatches.toSet[Input[String]].flatMap { _.value }.subsetOf(Executive.functionalMatchValues)



  





