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



