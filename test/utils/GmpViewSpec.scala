/*
 * Copyright 2024 HM Revenue & Customs
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

package utils

import config.ApplicationConfig
import controllers.FakeGmpContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import org.scalatestplus.play.PlaySpec
import play.api.i18n.{Messages, MessagesApi, MessagesImpl}
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.twirl.api.Html

trait GmpViewSpec extends PlaySpec with JSoupMatchers with GuiceOneServerPerSuite {

  implicit lazy val messagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents].langs.availables.head
  implicit val messagesApi =  app.injector.instanceOf[MessagesApi]
  implicit lazy val messages: Messages = MessagesImpl(messagesControllerComponents, messagesApi)

  implicit val applicationConfig=app.injector.instanceOf[ApplicationConfig]

  override def haveBackLink = new CssSelector("a[id='back-link'], a[id='js-back-link']")

  private val backLink = new CssSelector("a[class=govuk-back-link]")

  implicit val request = FakeRequest()
  implicit val context = FakeGmpContext

  def view: Html
  def doc: Document = Jsoup.parse(view.toString())
  def doc(view: Html): Document = Jsoup.parse(view.toString())

  def pageWithTitle(titleText: String): Unit = {
    "have a static title" in {
      doc.title must include(titleText)
    }
  }

  def pageWithTableCaption(captionText: String): Unit = {
    s"have a table caption with text: $captionText" in {
      doc must haveTableCaptionWithText(captionText)
    }
  }

  def pageWithHeader(headerText: String): Unit = {
    "have a static h1 header" in {
      doc must haveHeadingWithText(headerText)
    }
  }

  def pageWitStrong(text: String): Unit = {
    "have a static strong" in {
      doc must haveHeadingWithText(text)
    }
  }

  def pageWithH2Header(headerText: String): Unit = {
    s"have a static h2 header with text: $headerText" in {
      doc must haveH2HeadingWithText(headerText)
    }
  }

  def pageWithButtonForm(submitUrl: String, buttonText: String): Unit = {
    "have a form with a submit button or input labelled as buttonText" in {
      doc must haveSubmitButton(buttonText)
    }
    "have a form with the correct submit url" in {
      doc must haveFormWithSubmitUrl(submitUrl)
    }
  }

  def pageWithBackLink(): Unit = {
    "have a back link" in {
      doc must haveBackLink
    }
  }

  def pageWithNewBackLink(): Unit = {
    "have a back link" in {
      doc must backLink
    }
  }

  def pageWithJsBackLink(): Unit = {
    "have a back link that uses JS for navigation" in {
      val backButton = doc.select("a.govuk-back-link[href=\"#\"]")
      backButton.size mustBe 1
    }
  }


}

