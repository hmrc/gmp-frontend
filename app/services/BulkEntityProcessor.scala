/*
 * Copyright 2023 HM Revenue & Customs
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

package services

trait BulkEntityProcessing[E] extends EntityCharProcessing[E] {
  def list(source: => Iterator[Char], deliminator: Char, convertToEntity: String => E): List[E]
}

class BulkEntityProcessor[E] extends BulkEntityProcessing[E] {
  private def caterForMissingTrailingDeliminator(convertToEntity: String => E)(processingResult: EntityContainer[E]): List[E] =
    if(processingResult.partialEntityData.nonEmpty) {
      processingResult.constructedEntities :+ convertToEntity(processingResult.partialEntityData)
    }
    else {
      processingResult.constructedEntities
    }

  def list(source: => Iterator[Char], deliminator: Char, convertToEntity: String => E): List[E] = {
    val streamDataToEntities = processEntityData[E](deliminator, convertToEntity) _
    val tidyProcessedData = caterForMissingTrailingDeliminator(convertToEntity) _
    val processedInputData = source.foldLeft(EntityContainer[E]("", List[E]()))((entityContainer, streamCharacter) => {
      streamDataToEntities(entityContainer, streamCharacter)
    })
    tidyProcessedData(processedInputData)
  }
}
case class EntityContainer[T](partialEntityData: String, constructedEntities: List[T])

trait EntityCharProcessing[T] {
  def processEntityData[E](deliminator: Char, convertToEntity: String => E)(entityContainer: EntityContainer[E], input: Char): EntityContainer[E] = {
    val entities: List[E] = if (input != deliminator) {
      entityContainer.constructedEntities
    } else {
      val entityDataChars = entityContainer.partialEntityData.toString
      entityContainer.constructedEntities :+ convertToEntity(entityDataChars)
    }
    val entityCharacters: String = if (input != deliminator) {
      entityContainer.partialEntityData.concat(input.toString)
    } else {
      ""
    }
    EntityContainer(entityCharacters, entities)
  }
}
