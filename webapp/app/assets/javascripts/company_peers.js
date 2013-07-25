$(document).ready(function() {
  $("#company_peer_ticker").tokenInput("/company_peers.json", {
    crossDomain: false,
    hintText: "Type in a company peer ticker",
    theme: "facebook",
    propertyToSearch: 'ticker',
    preventDuplicates: true,
    tokenValue: 'ticker',
    tokenLimit: 1 // remove to select more than one
  });
});
