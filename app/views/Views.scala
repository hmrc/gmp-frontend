/*
 * Copyright 2020 HM Revenue & Customs
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

package views

import javax.inject.Inject
import models.BulkReference
import views.html._

class Views @Inject()(
  val bulkReference: bulk_reference,
  val bulkRequestReceived: bulk_request_received,
  val bulkResults: bulk_results,
  val bulkResultsNotFound: bulk_results_not_found,
  val bulkWrongUser: bulk_wrong_user,
  val contributionsEarnings: contributions_earnings,
  val dashboard: views.html.dashboard,
  val dateOfLeaving: dateofleaving,
  val equalise: views.html.equalise,
  val failure: views.html.failure,
  val globalError: global_error,
  val globalPageNotFound: global_page_not_found,
  val incorrectlyEncoded: views.html.incorrectlyEncoded,
  val inflationProof: inflation_proof,
  val memberDetails: member_details,
  val moreBulkResults: more_bulk_results,
  val pensionDetails: pension_details,
  val results: views.html.results,
  val revaluation: views.html.revaluation,
  val revaluationRate: revaluation_rate,
  val scenario: views.html.scenario,
  val thankYou: thank_you,
  val unauthorised: views.html.unauthorised,
  val uploadResult: upload_result,
  val upscanCsvFileUpload: upscan_csv_file_upload
) {

}
