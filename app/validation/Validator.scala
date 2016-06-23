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

import java.text.{ParseException, SimpleDateFormat}

import scala.annotation.tailrec

trait Validator {
  def isValid(value: String): Boolean
}

object SconValidate extends Validator with Modulus19CheckDigit {

  private val validFormat = "^[sS][012468]\\d{6}[abcdefhjklmnpqrtwxyABCDEFHJKLMNPQRTWXY]$"
  private val REFERENCE_NUMBER_START = 1
  private val REFERENCE_NUMBER_END = 8
  private val CHECK_DIGIT_START = 8
  private val CHECK_DIGIT_END = 9

  override def isValid(value: String): Boolean = {

    if (value.matches(validFormat)) {
      isCheckCorrect(value.substring(REFERENCE_NUMBER_START, REFERENCE_NUMBER_END), value.substring(CHECK_DIGIT_START, CHECK_DIGIT_END))
    } else {
      false
    }
  }

}

object NinoValidate extends Validator {

  private val validNinoFormat = "(AA|AB|AE|AH|AK|AL|AM|AP|AR|AS|AT|AW|AX|AY|AZ|BA|BB|BE|BH|BK|BL|BM|BT|CA|CB|CE|CH|CK|CL|CR|EA|EB|EE|EH|EK|EL|EM|EP|ER|ES|ET|EW|EX|EY|EZ|GY|HA|HB|HE|HH|HK|HL|HM|HP|HR|HS|HT|HW|HX|HY|HZ|JA|JB|JC|JE|JG|JH|JJ|JK|JL|JM|JN|JP|JR|JS|JT|JW|JX|JY|JZ|KA|KB|KE|KH|KK|KL|KM|KP|KR|KS|KT|KW|KX|KY|KZ|LA|LB|LE|LH|LK|LL|LM|LP|LR|LS|LT|LW|LX|LY|LZ|MA|MW|MX|NA|NB|NE|NH|NL|NM|NP|NR|NS|NW|NX|NY|NZ|OA|OB|OE|OH|OK|OL|OM|OP|OR|OS|OX|PA|PB|PC|PE|PG|PH|PJ|PK|PL|PM|PN|PP|PR|PS|PT|PW|PX|PY|RA|RB|RE|RH|RK|RM|RP|RR|RS|RT|RW|RX|RY|RZ|SA|SB|SC|SE|SG|SH|SJ|SK|SL|SM|SN|SP|SR|SS|ST|SW|SX|SY|SZ|TA|TB|TE|TH|TK|TL|TM|TN|TP|TR|TS|TT|TW|TX|TY|TZ|WA|WB|WE|WK|WL|WM|WP|YA|YB|YE|YH|YK|YL|YM|YP|YR|YS|YT|YW|YX|YY|YZ|ZA|ZB|ZE|ZH|ZK|ZL|ZM|ZP|ZR|ZS|ZT|ZW|ZX|ZY)[0-9]{6}[A-D]"

  override def isValid(nino: String) = nino != null && nino.replaceAll("\\s", "").toUpperCase.matches(validNinoFormat)
}

object NameValidate extends Validator {
  private val pattern = """[a-zA-Z\- '’]+"""

  override def isValid(name: String) = name != null && name.matches(pattern)
}

object SMValidate extends Validator {
  private val pattern = "(?i)^\\s*sm\\s*$"

  override def isValid(sm: String) = sm matches pattern

  def matches(sm: String) = isValid(sm)
}

object DateValidate extends Validator {

  private val validFormat = "^(0?[1-9]|[12][0-9]|3[01])[/](0?[1-9]|1[012])[/](19|20)\\d\\d$"
  private val dateFormat = "dd/MM/yyyy"
  private val gmpStart = {
    new SimpleDateFormat(dateFormat).parse("05/04/1978")
  }

  private val gmpEnd = {
    new SimpleDateFormat(dateFormat).parse("04/04/2046")
  }

  override def isValid(value: String): Boolean = {
    if (value.matches(validFormat)) {
      val format = new SimpleDateFormat(dateFormat)
      format.setLenient(false)
      try {
        format.parse(value)
        true
      } catch {
        case e: ParseException => false
      }
    } else {
      false
    }
  }

  def isOnOrAfterGMPStart(value: String): Boolean = {
    if (value.matches(validFormat)) {
      val format = new SimpleDateFormat(dateFormat)
      val date = format.parse(value)
      date.compareTo(gmpStart) >= 0
    } else true
  }

  def isOnOrBeforeGMPEnd(value: String): Boolean = {
    if (value.matches(validFormat)) {
      val format = new SimpleDateFormat(dateFormat)
      val date = format.parse(value)
      date.compareTo(gmpEnd) <= 0
    } else true
  }


}

sealed trait Modulus19CheckDigit {

  protected val MOD = 19
  protected val FIXED_VALUE = 51
  protected val CHECK_LETTERS = "ABCDEFHJKLMNPQRTWXY"
  protected val MULTIPLY = 8

  protected def isCheckCorrect(value: String, checkLetter: String): Boolean = {

    @tailrec
    def total(chars: List[Char], sum: Int, multiply: Int): Int = {
      chars match {
        case char :: tail => total(tail, sum + (char.getNumericValue * multiply), multiply - 1)
        case Nil => sum
      }
    }

    val modulus = (total(value.toList, 0, MULTIPLY) + FIXED_VALUE) % MOD
    val foundCheckLetter = CHECK_LETTERS.substring(modulus, modulus + 1)

    foundCheckLetter.equals(checkLetter.toUpperCase)

  }

}
