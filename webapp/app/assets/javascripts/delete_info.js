$(function() {
  $("#filling_date")
    .datepicker({
      onClose: function(selectedDate) {
        $("#filling_date").datepicker("option", "fillingdate", selectedDate);
      }
    });
})