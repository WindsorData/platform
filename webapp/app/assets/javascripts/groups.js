$(function() {
  $("#tickers_tokens").tokenInput("/groups/tickers.json", {
    crossDomain: false,
    hintText: "Type in a ticker",
    //prePopulate: $("#tickers_tokens").data("pre"),
    theme: "facebook"
  });
});