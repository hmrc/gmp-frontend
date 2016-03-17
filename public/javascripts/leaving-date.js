$(document).ready(function() {

  if(!$('#errors').length){
    ("#leaving-yes-after").removeAttr("checked");
    ("#leaving-yes-before").removeAttr("checked");
    ("#leaving-no").removeAttr("checked");
  }

  if ($('#errors').length && (
                              ($('#errors').text().indexOf('Enter a leaving date') > -1) ||
                              ($('#errors').text().indexOf('Enter a valid date') > -1) ||
                              ($('#errors').text().indexOf('Enter a day between 1 and 31') > -1) ||
                              ($('#errors').text().indexOf('Enter a month between 1 and 12') > -1) ||
                              ($('#errors').text().indexOf('Enter a date using numbers only') > -1) ||
                              ($('#errors').text().indexOf('Enter the year in full (4 numbers)') > -1)
                           )){
      $("#leaving-yes-after").click();
  }

  $('#leaving-no').click(function(){
    $('#leavingDate_day').val('');
    $('#leavingDate_month').val('');
    $('#leavingDate_year').val('');
  });

   $('#leaving-yes-before').click(function(){
      $('#leavingDate_day').val('');
      $('#leavingDate_month').val('');
      $('#leavingDate_year').val('');
    });


});



