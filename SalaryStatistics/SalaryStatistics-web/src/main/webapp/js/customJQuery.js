$(document).ready(function() {
    alert("ready");
    
    $(".dropdown li a").click(function(){
      $("#first-select").html($(this).text() + " <span class=\"caret\"></span>");
   });
});