$(document).ready(function() {
  
  addExecutiveForm();
  
  function addExecutiveForm(){
    $('.basic-group.hidden').clone().removeClass('hidden').appendTo('#forms-container');
  }
  $("#js-new-form").click(function() {
    addExecutiveForm();
  });
});