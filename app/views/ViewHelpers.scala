/*
 * Copyright 2022 HM Revenue & Customs
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

import uk.gov.hmrc.govukfrontend.views.html.components.{GovukButton, GovukDateInput, GovukDetails, GovukErrorSummary, GovukFileUpload, GovukInput, GovukRadios, GovukWarningText}
import uk.gov.hmrc.hmrcfrontend.views.html.components.HmrcHeader
import uk.gov.hmrc.hmrcfrontend.views.html.helpers.{HmrcHead, HmrcTrackingConsentSnippet}
import uk.gov.hmrc.play.views.html.helpers.FormWithCSRF

import javax.inject.Inject

class ViewHelpers @Inject()(
                                val hmrcTrackingConsentSnippet: HmrcTrackingConsentSnippet,
                                val hmrcHeader:                 HmrcHeader,
                                val hmrcHead:                  HmrcHead,
                                val govukButton:                GovukButton,
                                val govukErrorSummary:          GovukErrorSummary,
                                val govUkRadios:                GovukRadios,
                                val govukDateInput:             GovukDateInput,
                                val govukInput:                 GovukInput,
                                val govukFileUpload:            GovukFileUpload,
                                val govUkDetails:               GovukDetails,
                                val govukWarningText:           GovukWarningText,
                                val form:                       FormWithCSRF

                              )
