$(function() {
  $("#date_since")
    .datepicker({
      onClose: function(selectedDate) {
        $("#date_to").datepicker("option", "minDate", selectedDate);
      }
    });

  $("#date_to")
  .datepicker({
    onClose: function(selectedDate) {
      $("#date_since").datepicker("option", "maxDate", selectedDate);
    }
  });

  $("#search-button").click(function(){
    $("#log-container").html('<p id="loading-gif" style="text-align: center;"><img src="/assets/ajax-loader.gif" /></p>');
  });

  $('#upload-log-ticker').select2({
    allowClear: true,
    formatNoMatches: function(term){
      return "No results";
    }
  });
});