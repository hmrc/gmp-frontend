/*
* Copyright 2016 HM Revenue & Customs
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

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



