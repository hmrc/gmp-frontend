/*
 * Copyright 2017 HM Revenue & Customs
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

package forms

import forms.ExitQuestionnaireForm._
import models.ExitQuestionnaire
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.libs.json.Json

class ExitQuestionnaireFormSpec extends PlaySpec with OneAppPerSuite {
  "Exit Questionnaire Form" must {
    "return no errors with no optional input" in {

      val exitQuestionnaireFormResults = exitQuestionnaireForm.bind(Json.toJson(ExitQuestionnaire(None,None, None, None, None, None)))

      assert(exitQuestionnaireFormResults.errors.size == 0)

    }

    "return no errors with valid input" in {

      val exitQuestionnaireFormResults = exitQuestionnaireForm.bind(Json.toJson(ExitQuestionnaire(Some(ExitQuestionnaire.VERY_EASY), Some(ExitQuestionnaire.VERY_SATISFIED), Some("These are the comments"), Some("Full Name"), Some("email@address.com"), Some("0123456789"))))

      assert(exitQuestionnaireFormResults.errors.size == 0)

    }

    "return 1 errors with input exceeding maxlength" in {

      val exitQuestionnaireFormResults = exitQuestionnaireForm.bind(Json.toJson(ExitQuestionnaire(Some(ExitQuestionnaire.VERY_EASY), Some(ExitQuestionnaire.VERY_SATISFIED), Some("I am writing something. Yes, I plan to make it the most boring thing ever written. I go to the store. A car is parked. Many cars are parked or moving. Some are blue. Some are tan. They have windows. In the store, there are items for sale. These include such things as soap, detergent, magazines, and lettuce. You can enhance your life with these products. Soap can be used for bathing, be it in a bathtub or in a shower. Apply the soap to your body and rinse. Detergent is used to wash clothes. Place your dirty clothes into a washing machine and add some detergent as directed on the box. Select the appropriate settings on your washing machine and you should be ready to begin. Magazines are stapled reading material made with glossy paper, and they cover a wide variety of topics, ranging from news and politics to business and stock market information. Some magazines are concerned with more recreational topics, like sports card collecting or different kinds of hairstyles. Lettuce is a vegetable. It is usually green and leafy, and is the main ingredient of salads. You may have an appliance at home that can quickly shred lettuce for use in salads. Lettuce is also used as an optional item for hamburgers and deli sandwiches. Some people even eat lettuce by itself. I have not done this. So you can purchase many types of things at stores."), Some("Full Name"), Some("email@address.com"), Some("0123456789"))))

      assert(exitQuestionnaireFormResults.errors.size == 1)

    }
  }
}
