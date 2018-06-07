/*
 * Copyright 2018 HM Revenue & Customs
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

import controllers.FakeGmpContext
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.scalatest.matchers.{MatchResult, Matcher}
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.twirl.api.Html

import scala.collection.JavaConversions._

trait GmpViewSpec extends PlaySpec with OneServerPerSuite{

  implicit val request = FakeRequest()
  implicit val context = FakeGmpContext()
  implicit val messages: Messages = play.api.i18n.Messages.Implicits.applicationMessages


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

  def haveHeadingWithText (expectedText: String) = new TagWithTextMatcher(expectedText, "h1")
  def haveParagraphWithText (expectedText: String) = new TagWithTextMatcher(expectedText, "p")

  class TagWithTextMatcher(expectedContent: String, tag: String) extends Matcher[Document] {
    def apply(left: Document): MatchResult = {
      val elements: List[String] =
        left.getElementsByTag(tag).toList
          .map(_.text)

      lazy val elementContents = elements.mkString("\t", "\n\t", "")

      MatchResult(
        elements.contains(expectedContent),
        s"[$expectedContent] not found in '$tag' elements:[\n$elementContents]",
        s"'$tag' element found with text [$expectedContent]"
      )
    }
  }

  class ElementWithAttributeValueMatcher(expectedContent: String, attribute: String) extends Matcher[Element] {
    def apply(left: Element): MatchResult = {
      val attribVal = left.attr(attribute)
      val attributes = left.attributes().asList().mkString("\t", "\n\t", "")

      MatchResult(
        attribVal == expectedContent,
        s"""[${attribute}="${expectedContent}"] is not a member of the element's attributes:[\n${attributes}]""",
        s"""[${attribute}="${expectedContent}"] is a member of the element's attributes:[\n${attributes}]""")
    }

  }

}

