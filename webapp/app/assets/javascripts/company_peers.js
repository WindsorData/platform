$(document).ready(function() {
  $("#company_peers_tokens").tokenInput("/company_peers.json", {
    crossDomain: false,
    hintText: "Type in a company ticker",
    theme: "facebook",
    propertyToSearch: 'ticker',
    preventDuplicates: true
  });
});
