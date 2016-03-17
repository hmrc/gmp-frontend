$(document).ready(function() {

  if($('#revaluate-yes').is(':checked') && !$('#errors').length){
    ("#revaluate-yes").removeAttr("checked");
  }

  if($('#revaluate-no').is(':checked') && !$('#errors').length){
    ("#revaluate-no").removeAttr("checked");
  }

  if ($('#errors').length && (
                              ($('#errors').text().indexOf('Enter a revaluation date') > -1) ||
                              ($('#errors').text().indexOf('Enter a day between 1 and 31') > -1) ||
                              ($('#errors').text().indexOf('Enter a month between 1 and 12') > -1) ||
                              ($('#errors').text().indexOf('Enter the year in full (4 numbers)') > -1)
                              )){
      $("#revaluate-yes").click();
  }

  $('#revaluate-no').click(function(){
    $('#revaluationDate_day').val('');
    $('#revaluationDate_month').val('');
    $('#revaluationDate_year').val('');
  });


});



