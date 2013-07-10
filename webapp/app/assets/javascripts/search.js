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
    var text = $("#advanced-search-button").text();
    $("#advanced-search-button").text(text == "Show Advanced search" ? "Hide Advanced Search" : "Show Advanced search");
  });

  function replaceElementNameWithCounter(){
    var elements = $('.cloned [name^="[role_form]"]');

    $.each(elements, function(){
      var replaced = $(this).attr('name').replace("[role_form]", "[role_form_" + form_counter + "]");
      $(this).attr('name', replaced);
    });
  }

  $('input:checkbox').click(function() {
    var el = $(this).closest('div').children("input[type='text']");
    el.attr("disabled", !this.checked);
  });

});