$(document).ready(function() {
  $(".company-peer-ticker").tokenInput("/company_peers.json", {
    crossDomain: false,
    hintText: "Type in a company peer ticker",
    theme: "facebook",
    propertyToSearch: 'ticker',
    preventDuplicates: true,
    tokenValue: 'ticker',
    tokenLimit: 1 // remove to select more than one
  });  

  $("input[name=secondary_type]").click(function(e){
    $("#normalized").toggleClass("hidden");
    $("#unnormalized").toggleClass("hidden");
  });
  
  $("#topAmount").change(function(){
    filterTopPeersType("#normalized div.accordion section");
    filterTopPeersType("#unnormalized div.accordion section");    
  });

  function filterTopPeersType(list){
    $(list).each(function(){
      $(this).removeClass("hidden");
    });

    var topAmount = $("#topAmount option:selected").val();
    if(!isNaN(topAmount)){
      $(list).each(function(index){ 
        if(index >= topAmount){ 
          $(this).addClass("hidden")
        } 
      });
    }
  }
});
