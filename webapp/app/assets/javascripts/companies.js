$(document).ready(function() {
  $("#ticker").tokenInput("/groups/tickers.json", { // remove /groups
    crossDomain: false,
    hintText: "Type in a ticker",
    theme: "facebook",
    propertyToSearch: "ticker",
    tokenValue: 'cusip',
    tokenLimit: 1
  });
});