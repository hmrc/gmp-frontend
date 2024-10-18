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

package models

import play.api.libs.json.{Json, OFormat}

import play.api.libs.json._
import play.api.libs.functional.syntax._
import java.time.Instant

case class SingleCalculationSessionCache(
                                          id: String,
                                          memberDetails: MemberDetails,
                                          scon: String,
                                          scenario: String,
                                          revaluationDate: Option[GmpDate],
                                          rate: Option[String],
                                          leaving: Leaving,
                                          equalise: Option[Int],
                                          lastModified: Instant = Instant.now()
                                        )

object SingleCalculationSessionCache {
  object MongoFormats {
    import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats.Implicits._
    def reads: Reads[SingleCalculationSessionCache] = (
      (__ \ "id").read[String] and
        (__ \ "memberDetails").read[MemberDetails] and
        (__ \ "scon").read[String] and
        (__ \ "scenario").read[String] and
        (__ \ "revaluationDate").readNullable[GmpDate] and
        (__ \ "rate").readNullable[String] and
        (__ \ "leaving").read[Leaving] and
        (__ \ "equalise").readNullable[Int] and
        (__ \ "lastModified").read[Instant]
      )(SingleCalculationSessionCache.apply _)

    def writes: OWrites[SingleCalculationSessionCache] = new OWrites[SingleCalculationSessionCache] {
      override def writes(datedCacheMap: SingleCalculationSessionCache): JsObject = Json.obj(
        "id" -> datedCacheMap.id,
        "memberDetails" -> datedCacheMap.memberDetails,
        "scon" -> datedCacheMap.scon,
        "scenario" -> datedCacheMap.scenario,
        "revaluationDate" -> datedCacheMap.revaluationDate,
        "rate" -> datedCacheMap.rate,
        "leaving" -> datedCacheMap.leaving,
        "equalise" -> datedCacheMap.equalise,
        "lastModified" -> datedCacheMap.lastModified
      )
    }

    implicit val formats: OFormat[SingleCalculationSessionCache] = OFormat(reads, writes)
  }
}