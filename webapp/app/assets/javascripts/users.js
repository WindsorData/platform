$(function() {
  hide_or_show_company_fields();

  $('#user_role').change(function(){
    hide_or_show_company_fields();    
  });

  function hide_or_show_company_fields(){
    if($('#user_role').val() != "client") {
      $('#company_fields').addClass('hidden');
    }else {
      $('#company_fields').removeClass('hidden');
    }
  }

});
