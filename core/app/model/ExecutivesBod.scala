package model

import libt._

object ExecutivesBod {

  val TMeetingsFees = 
    TModel(
       'regular -> TNumber,
       'special -> TNumber,
       'telephonic -> TNumber,
       'chair -> TNumber)
       
  val TCashRetainer = TModel(
      'member -> TNumber,
      'chair -> TNumber)
    
  val TAnnual = TModel(
      'member -> TNumber,
      'member$ -> TNumber,
      'chair -> TNumber,
      'chair$ -> TNumber,
      'vestYears -> TNumber,
      'vesting -> TString)
  
  val TInitial = TModel(
      'annualEligible -> TString,
      'member -> TNumber,
      'member$ -> TNumber,
      'chair -> TNumber,
      'chair$ -> TNumber,
      'vestYears -> TNumber,
      'vesting  -> TString)
               
  val TBod = TModel(
      'directorData -> TModel(
          'group -> TString,
          'ceo -> TModel(
              'exists -> TXBool,
              'chairman -> TXBool),
          'numberOfDirectors -> TModel(
              'employee -> TNumber,
              'nonEmployee -> TNumber)),
       'meetings -> TModel(
           'numberOfMeetings -> TModel(
    		   'regular -> TNumber,
    		   'special -> TNumber,
    		   'telephonic -> TNumber),
            'meetingsFees -> TMeetingsFees,
            'meetingsFeesPriorValues -> TMeetingsFees),
       'annualRetainers -> TModel(
           'cashRetainer -> TCashRetainer),
       'annualRetainersPriorValues -> TModel(
           'cashRetainer -> TCashRetainer),
       'stockOptions -> TModel(
           'annual -> TAnnual,
           'initial -> TInitial),
       'stockOptionsPriorValues -> TModel(
           'annual -> TAnnual,
           'initial -> TInitial),
       'fullValues -> TModel(
           'annual -> TAnnual,
           'initial -> TInitial),
       'fullValuesPriorValues -> TModel(
           'annual -> TAnnual,
           'initial -> TInitial),
       'other -> TModel(
           'cashDeferrals -> TModel(
               'toCash -> TXBool,
               'toStock -> TXBool)),
       'ownershipGuidelines -> TModel(
           'achieve -> TModel(
               'timeTo -> TString,
               'years -> TNumber,
               'multiple -> TNumber,
               'value -> TNumber,
               'shares -> TNumber,
               'lesserOf -> TXBool),
            'retention -> TModel(
                'ratio -> TNumber,
                'period -> TString)))
}