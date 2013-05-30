package model
import libt._

object Commons {

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

  val TFunctionalMatch =
    TModel(
      'primary -> TPrimaryValues,
      'secondary -> TSecondaryValues,
      'level -> TLevelValues,
      'scope -> TScopeValues,
      'bod -> TBodValues)
}