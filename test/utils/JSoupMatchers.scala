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

import org.jsoup.nodes.{Attributes, Document, Element}
import org.jsoup.select.Elements
import org.scalatest.matchers.{MatchResult, Matcher}

import scala.collection.JavaConversions._

trait JSoupMatchers {

  def haveHeadingWithText (expectedText: String) = new TagWithTextMatcher(expectedText, "h1")
  def haveParagraphWithText (expectedText: String) = new TagWithTextMatcher(expectedText, "p")
  def haveSubmitButton(expectedText: String) = new CssSelectorWithTextMatcher(expectedText,"button[type=submit]")
  def haveFormWithSubmitUrl(url: String) = new CssSelectorWithAttributeValueMatcher("action", url, "form[method=POST]")
  def haveBackLink = new CssSelector("a[id=back-link]")
  def haveInputLabelWithText (id:String, expectedText: String) = new CssSelectorWithTextMatcher(expectedText, s"label[for=$id]")
  def haveH2HeadingWithText (expectedText: String) = new TagWithTextMatcher(expectedText, "h2")
  def haveBulletPointWithText (expectedText: String) = new CssSelectorWithTextMatcher(expectedText, "ul>li")

  class CssSelectorWithTextMatcher(expectedContent: String, selector: String) extends Matcher[Document] {
    def apply(left: Document): MatchResult = {
      val elements: List[String] =
        left.select(selector)
          .toList
          .map(_.text)

      lazy val elementContents = elements.mkString("\t", "\n\t", "")
      MatchResult(
        elements.contains(expectedContent),
        s"[$expectedContent] not found in elements with '$selector' selector:[\n$elementContents]",
        s"[$expectedContent] element found with '$selector' selector and text [$expectedContent]"
      )
    }
  }

  class CssSelectorWithAttributeValueMatcher(attributeName: String, attributeValue: String, selector: String) extends Matcher[Document] {
    def apply(left: Document): MatchResult = {
      val attributes: List[Attributes] =
        left.select(selector)
          .toList
          .map(_.attributes())

      lazy val attributeContents = attributes.mkString("\t", "\n\t", "")

      MatchResult(
        attributes.map(_.get(attributeName)).contains(attributeValue),
        s"[$attributeName=$attributeValue] not found in elements with '$selector' selector:[\n$attributeContents]",
        s"[$attributeName=$attributeValue] element found with '$selector' selector"
      )
    }
  }

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

  class CssSelector(selector: String) extends Matcher[Document] {
    def apply(left: Document): MatchResult = {
      val elements: Elements =
        left.select(selector)

      MatchResult(
        elements.size >= 1,
        s"No element found with '$selector' selector",
        s"${elements.size} elements found with '$selector' selector"
      )
    }
  }

}
