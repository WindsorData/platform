package model

import libt._

object PeerCompanies {

  val TPeers = TModel(
    'companyName -> TString,
    'ticker -> TString,
    'src_doc -> TString,
    'filingDate -> TDate,
    'group -> TString,
    'fiscalYear	-> TInt,
    'comments	-> TString,
    'link -> TString,
    'groupDesc -> TString,
    'changes -> TString,
    'subGroup	-> TString,
    'peerCoName	-> TString,
    'peerTicker -> TString,
    'value -> TAny)

  val peerId = Seq(Path('peerTicker), Path('ticker), Path('filingDate), Path('fiscalYear))
}
