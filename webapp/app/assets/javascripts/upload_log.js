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
});