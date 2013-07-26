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

  $("#toggleFormLink").click(function(){ 
    $("#single_ticker_form").toggleClass("hidden");
    $("#ticker_list_form").toggleClass("hidden");
    var text = $("#toggleFormLink").text();
    text == "Switch to Tickers List" ?  $("#toggleFormLink").text("Switch to Single Ticker") : $("#toggleFormLink").text("Switch to Tickers List");
  });

  $("input[name=secondary_type]").click(function(e){
    $("#normalized").toggleClass("hidden");
    $("#unnormalized").toggleClass("hidden");
  });

});
