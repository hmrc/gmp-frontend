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

package utils

import config.ApplicationConfig
import controllers.FakeGmpContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.Play
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl, MessagesProvider}
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.twirl.api.Html

trait GmpViewSpec extends PlaySpec with JSoupMatchers with OneServerPerSuite {

  val messagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents].langs.availables.head
  val messagesApi =  app.injector.instanceOf[MessagesApi]
  implicit lazy val messages: Messages = MessagesImpl(messagesControllerComponents, messagesApi)

  implicit val applicationConfig=app.injector.instanceOf[ApplicationConfig]

  override def haveBackLink = new CssSelector("a[id=back-link]")

  implicit val request = FakeRequest()
  implicit val context = FakeGmpContext
 // implicit val messages: Messages = play.api.i18n.Messages.Implicits.applicationMessages

  def view: Html
  def doc: Document = Jsoup.parse(view.toString())
  def doc(view: Html): Document = Jsoup.parse(view.toString())

  def pageWithTitle(titleText: String): Unit = {
    "have a static title" in {
      doc.title must include(titleText)
    }
  }

  def pageWithHeader(headerText: String): Unit = {
    "have a static h1 header" in {
      doc must haveHeadingWithText(headerText)
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

  def pageWithBackLink: Unit = {
    "have a back link" in {
      doc must haveBackLink
    }
  }

}

