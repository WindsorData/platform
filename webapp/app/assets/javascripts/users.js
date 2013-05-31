$(function() {
  $('#user_role').val("client");

  $('#user_role').change(function(){
    if($('#user_role').val() != "client") {
      hideCompanyCombo();
    }else {
      showCompanyCombo();
    }    
  });

  function showCompanyCombo(){
    $('#user_company').removeClass('hidden');
  }
  function hideCompanyCombo(){
    $('#user_company').addClass('hidden');
  }

});
