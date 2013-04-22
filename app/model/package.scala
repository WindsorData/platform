import libt._
import libt.spreadsheet._
import input._
import scala.collection.immutable.Stream
import org.apache.poi.ss.usermodel.Row
import org.joda.time.DateTime
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Workbook

package object model {
  import persistence._

  val grantTypes = TEnum("Annual", "Retention", "Hire", "Promotion", "Special", "Acquisition", "Other")

  val validPrimaryValues = TEnum("CEO (Chief Executive Officer)",
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

  val validSecondaryValues = TEnum(
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

  val validLevelValues = TEnum(
    "President",
    "EVP (Executive Vice President)",
    "SVP (Senior Vice President)",
    "VP (Vice President)",
    "GM (General Manager)",
    "Group President")

  val validScopeValues = TEnum(
    "WW/Global/International",
    "US",
    "North America",
    "Europe",
    "Asia",
    "Americas")

  val validBodValues = TEnum("Chairman",
    "Vice Chairman",
    "Director")

  val TExecutive = TModel(
    'firstName -> TString,
    'lastName -> TString,
    'title -> TString,
    'functionalMatches ->
      TModel(
        'primary -> validPrimaryValues,
        'secondary -> validSecondaryValues,
        'level -> validLevelValues,
        'scope -> validScopeValues,
        'bod -> validBodValues),
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
        'type -> grantTypes)),

    'timeVestRS -> TCol(
      TModel(
        'grantDate -> TDate,
        'number -> TNumber,
        'price -> TNumber,
        'value -> TNumber,
        'type -> grantTypes)),

    'performanceVestRS -> TCol(
      TModel(
        'grantDate -> TDate,
        'targetNumber -> TNumber,
        'grantDatePrice -> TDate,
        'targetValue -> TNumber,
        'type -> grantTypes)),

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








