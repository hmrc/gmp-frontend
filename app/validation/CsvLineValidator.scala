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

package validation

import play.api.i18n.Messages

object CsvLineValidator {

  def validateLine(line: String) = line.split(",").zipWithIndex.map {
      case (value, 0) => (0, validateScon(value))
      case (value, 1) => (1, validateNino(value))
      case (value, key) => (key, None)
    }.toMap.filter {
      _._2.isDefined
    }.map { m =>
      (m._1, m._2.get)
    } match {
      case map if map.nonEmpty => Some(map)
      case _ => None
    }

  private def validateScon(scon: String): Option[String] = {
    scon match {
      case "" => Some(Messages("gmp.error.mandatory", Messages("gmp.scon")))
      case x if !SconValidate.isValid(x) => Some(Messages("gmp.error.scon.invalid"))
      case _ => None
    }
  }

  private def validateNino(nino: String) = {
    nino match {
      case "" => Some(Messages("gmp.error.mandatory", Messages("gmp.nino")))
      case x if !NinoValidate.isValid(x) => Some(Messages("gmp.error.nino.invalid"))
      case _ => None
    }
  }
}
