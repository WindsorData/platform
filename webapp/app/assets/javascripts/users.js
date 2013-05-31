$(function() {
  $('#user_role').val("client");

  $('#user_role').change(function(){
    if($('#user_role').val() != "client") {
      hideCompanyFields();
    }else {
      showCompanyFields();
    }    
  });

  function showCompanyFields(){
    $('#company_fields').removeClass('hidden');
  }
  function hideCompanyFields(){
    $('#company_fields').addClass('hidden');
  }

});
