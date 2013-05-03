import libt._
import libt.spreadsheet._
import scala.collection.immutable.Stream
import org.apache.poi.ss.usermodel.Row
import org.joda.time.DateTime
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Workbook

package object model {
  import persistence._

  val TGrantTypes = TEnum("Annual", "Retention", "Hire", "Promotion", "Special", "Acquisition", "Other")

  val TPrimaryValues = TEnum("CEO (Chief Executive Officer)",
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
    "Other")

  val TSecondaryValues = TEnum(
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
    "Other")

  val TLevelValues = TEnum(
    "President",
    "EVP (Executive Vice President)",
    "SVP (Senior Vice President)",
    "VP (Vice President)",
    "GM (General Manager)",
    "Group President")

  val TScopeValues = TEnum(
    "WW/Global/International",
    "US",
    "North America",
    "Europe",
    "Asia",
    "Americas")
    
  val TBodValues = TEnum("Chairman",
    "Vice Chairman",
    "Director")

  val TExecutive = TModel(
    'firstName -> TString,
    'lastName -> TString,
    'title -> TString,
    'functionalMatches ->
      TModel(
        'primary -> TPrimaryValues,
        'secondary -> TSecondaryValues,
        'level -> TLevelValues,
        'scope -> TScopeValues,
        'bod -> TBodValues),
    'founder -> TString,
    'transitionPeriod -> TString,

    'cashCompensations -> TModel(
      'baseSalary -> TNumber,
      'actualBonus -> TNumber,
      'retentionBonus -> TNumber,
      'signOnBonus -> TNumber,
      'targetBonus -> TNumber,
      'thresholdBonus -> TNumber,
      'maxBonus -> TNumber,
      'nextFiscalYearData -> TModel(
        'baseSalary -> TNumber,
        'targetBonus -> TNumber)),

    'optionGrants -> TCol(
      TModel(
        'grantDate -> TDate,
        'expireDate -> TDate,
        'number -> TNumber,
        'price -> TNumber,
        'value -> TNumber,
        'perf -> TXBool,
        'type -> TGrantTypes)),

    'timeVestRS -> TCol(
      TModel(
        'grantDate -> TDate,
        'number -> TNumber,
        'price -> TNumber,
        'value -> TNumber,
        'type -> TGrantTypes)),

    'performanceVestRS -> TCol(
      TModel(
        'grantDate -> TDate,
        'targetNumber -> TNumber,
        'grantDatePrice -> TDate,
        'targetValue -> TNumber,
        'type -> TGrantTypes)),

    'performanceCash -> TCol(
      TModel(
        'grantDate -> TDate,
        'targetValue -> TNumber,
        'payout -> TNumber)),

    'carriedInterest -> TModel(
      'ownedShares -> TModel(
        'beneficialOwnership -> TNumber,
        'options -> TNumber,
        'unvestedRestrictedStock -> TNumber,
        'disclaimBeneficialOwnership -> TNumber,
        'heldByTrust -> TNumber,
        'other -> TString),
      'outstandingEquityAwards -> TModel(
        'vestedOptions -> TNumber,
        'unvestedOptions -> TNumber,
        'timeVestRS -> TNumber,
        'perfVestRS -> TNumber)))

  val TCompanyFiscalYear = TModel(
    'ticker -> TString,
    'name -> TString,
    'disclosureFiscalYear -> TInt,

    'executives -> TCol(TExecutive))

 
}








