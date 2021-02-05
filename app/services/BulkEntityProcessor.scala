/*
 * Copyright 2021 HM Revenue & Customs
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

import play.api.libs.iteratee.{Enumerator, Iteratee}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait BulkEntityProcessing[E] extends EntityCharProcessing[E] {
  def list(source: => Enumerator[Array[Byte]], deliminator: Char, convertToEntity: String => E): Future[List[E]]
  def listf(sourcef: => Future[Enumerator[Array[Byte]]], deliminator: Char, convertToEntity: String => E): Future[List[E]]
  def list(source: => Iterator[Char], deliminator: Char, convertToEntity: String => E): List[E]

  def usingIteratorOfChars(source: => Iterator[Char], deliminator: Char, convertToEntity: String => E): Iterator[E]
  def usingEnumOfByteArrays(source: => Enumerator[Array[Byte]], deliminator: Char, convertToEntity: String => E): Future[Iterator[E]]
  def usingFutureEnumOfByteArrays(source: => Future[Enumerator[Array[Byte]]], deliminator: Char, convertToEntity: String => E): Future[Iterator[E]]
}

class BulkEntityProcessor[E] extends BulkEntityProcessing[E] {

  private def byteArrayToChar=
    Iteratee.getChunks[Array[Byte]]
      .map(chunk => chunk)
      .map(byteArrays => byteArrays.toIterator.map(byteArray => byteArray.map(_.toChar)))

  private def caterForMissingTrailingDeliminator(convertToEntity: String => E)(processingResult: EntityContainer[E]): List[E] =
    if(processingResult.partialEntityData.nonEmpty) {
      processingResult.constructedEntities :+ convertToEntity(processingResult.partialEntityData)
    }
    else {
      processingResult.constructedEntities
    }

  def list(source: => Enumerator[Array[Byte]], deliminator: Char, convertToEntity: String => E): Future[List[E]] = {
    val streamDataToEntities = processEntityData[E](deliminator, convertToEntity) _
    val tidyProcessedData = caterForMissingTrailingDeliminator(convertToEntity) _
    source run byteArrayToChar map {
      charArrayIterator =>
        val processedInputData = charArrayIterator.foldLeft(EntityContainer("", List[E]()))((entityRepo, streamCharacterArray) => {
          streamCharacterArray.foldLeft(entityRepo)((entityContainer, streamCharacter) => {
            streamDataToEntities(entityContainer, streamCharacter)
          })
        })
        tidyProcessedData(processedInputData)
    }
  }

  def listf(sourcef: => Future[Enumerator[Array[Byte]]], deliminator: Char, convertToEntity: String => E): Future[List[E]] = {
    sourcef flatMap {
      source =>
        list(source, deliminator, convertToEntity)
    }
  }

  def list(source: => Iterator[Char], deliminator: Char, convertToEntity: String => E): List[E] = {
    val streamDataToEntities = processEntityData[E](deliminator, convertToEntity) _
    val tidyProcessedData = caterForMissingTrailingDeliminator(convertToEntity) _
    val processedInputData = source.foldLeft(EntityContainer[E]("", List[E]()))((entityContainer, streamCharacter) => {
      streamDataToEntities(entityContainer, streamCharacter)
    })
    tidyProcessedData(processedInputData)
  }

  def usingIteratorOfChars(source: => Iterator[Char], deliminator: Char, convertToEntity: String => E): Iterator[E] = {
    new CharsIterator(source, deliminator, convertToEntity)
  }

  def usingEnumOfByteArrays(source: => Enumerator[Array[Byte]], deliminator: Char, convertToEntity: String => E): Future[Iterator[E]] = {
    val byteArrayToChar = Iteratee.getChunks[Array[Byte]].map(chunk => chunk).map(byteArrays => byteArrays.toIterator.map(byteArray => byteArray.map(_.toChar)))

    source run byteArrayToChar map {
      charArrayIterator =>
        new CharArraysIterator[E](charArrayIterator, deliminator, convertToEntity)
    }
  }

  def usingFutureEnumOfByteArrays(source: => Future[Enumerator[Array[Byte]]], deliminator: Char, convertToEntity: String => E): Future[Iterator[E]] = {
    source.flatMap {
      usingEnumOfByteArrays(_, deliminator, convertToEntity)
    }
  }
}

private class CharArraysIterator[T](source: Iterator[Array[Char]], deliminator: Char, convertToEntity: String => T)
  extends Iterator[T] with EntityCharProcessing[T] with QueueSupport {
  implicit def convert(entityRawData: String): T = convertToEntity(entityRawData)

  override val characterQueue = new scala.collection.mutable.Queue[Char]()
  private val entityQueue = scala.collection.mutable.Queue[T]()

  override def hasNext: Boolean = source.hasNext || characterQueue.nonEmpty || entityQueue.nonEmpty

  override def next(): T = {
    val streamDataToEntities = processEntityData[T](deliminator, convertToEntity) _

    def processNextArray = source.next().foldLeft(EntityContainer(emptyCharacterQueue(), List[T]()))((entityContainer, streamCharacter) => {
      streamDataToEntities(entityContainer, streamCharacter)
    })

    // Process next Chunk only if there's nothing in the entityQueue to serve and unprocessed data is available
    if (source.hasNext && entityQueue.isEmpty) {
      val latestEntityDataFetched = processNextArray
      latestEntityDataFetched.constructedEntities.foreach(entityQueue.enqueue(_))
      latestEntityDataFetched.partialEntityData.toCharArray.foreach(characterQueue.enqueue(_))
    }

    if (entityQueue.nonEmpty) return entityQueue.dequeueFirst(_ => true).getOrElse(throw new RuntimeException("The Iterator is out of sync! The hasNext returned true but the next function cannot find anything to return!"))
    if (entityQueue.isEmpty && !source.hasNext && characterQueue.nonEmpty) return emptyCharacterQueue()

    next()
  }
}

private class CharsIterator[T](source: Iterator[Char], deliminator: Char, converter: String => T) extends Iterator[T] with QueueSupport {
  override val characterQueue = new scala.collection.mutable.Queue[Char]()

  implicit def convert(entityRawData: String): T = converter(entityRawData)

  override def hasNext: Boolean = {
    source.hasNext || characterQueue.nonEmpty
  }

  override def next(): T = {
    val nextCharacter = source.next()

    if (nextCharacter != deliminator) {
      characterQueue.enqueue(nextCharacter)
      if (source.hasNext) next() else emptyCharacterQueue()
    }
    else {
      emptyCharacterQueue()
    }
  }
}

trait QueueSupport {
  val characterQueue: scala.collection.mutable.Queue[Char]

  def emptyCharacterQueue(): String = {
    characterQueue.dequeueAll(_ => true).foldLeft("")((partialEntityString, character) => {
      partialEntityString + character.toString
    })
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
