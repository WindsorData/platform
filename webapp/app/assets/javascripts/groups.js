$(document).ready(function() {
  $("#tickers_tokens").tokenInput("/groups/tickers.json", {
    crossDomain: false,
    hintText: "Type in a ticker",
    prePopulate: $("#tickers_tokens").data("pre"),
    theme: "facebook",
    preventDuplicates: true,
    propertyToSearch: "ticker"
  });

  $("#toggleTickerFormLink").click(function(){ 
    $("#single_ticker_input").toggleClass("hidden");
    $("#multiple_ticker_input").toggleClass("hidden");
    var text = $("#toggleTickerFormLink").text();
    text == "Click to paste tickers list" ?  $("#toggleTickerFormLink").text("Click to sigle ticker autocomplete") : $("#toggleTickerFormLink").text("Click to paste tickers list");
  });
});
