package model

import libt.TModel
import libt.TString

object CompanyIndex {

  val TCompanyIndex = TModel(
    'ticker -> TString,
    'name -> TString)
}








