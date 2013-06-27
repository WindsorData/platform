$(function() {
  var form_counter = 1;
  
  addExecutiveForm();
  replaceElementNameWithCounter();
  
  function addExecutiveForm(){
    $('.basic-group.hidden').clone().removeClass('hidden').addClass("cloned").appendTo('#forms-container');
    replaceElementNameWithCounter();
    form_counter ++;
  }

  $("#js-new-form").click(function() {
    addExecutiveForm();
  });

  $("#advanced-search-button").click(function() {
    $(".advanced-search").toggleClass('hidden');
  });

  function replaceElementNameWithCounter(){
    var elements = $('.cloned [name^="[role_form]"]');

    $.each(elements, function(){
      var replaced = $(this).attr('name').replace("[role_form]", "[role_form_" + form_counter + "]");
      $(this).attr('name', replaced);
    });
  }

});