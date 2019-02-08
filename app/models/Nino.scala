/*
 * Copyright 2019 HM Revenue & Customs
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

package models

import uk.gov.hmrc.domain.{TaxIdentifier, SimpleName}
import validation.NinoValidate

case class Nino(nino: String) extends TaxIdentifier with SimpleName {
  require(NinoValidate.isValid(nino), s"$nino is not a valid nino.")

  def value = nino

  val name = "nino"

  def formatted = value.grouped(2).mkString(" ")
}

//object Nino extends (String => Nino) {
//
//
//}
