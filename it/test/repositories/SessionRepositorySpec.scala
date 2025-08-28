/*
 * Copyright 2025 HM Revenue & Customs
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

package test.repositories

import config.FrontendAppConfig
import org.mockito.MockitoSugar.when
import org.mongodb.scala.model.Filters
import org.scalatest.{Assertion, OptionValues}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsObject, JsPath, JsString}
import repositories.{Entry, SessionRepository}
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant, ZoneId}
import scala.concurrent.ExecutionContext.Implicits.global

class SessionRepositorySpec
    extends AnyFreeSpec
    with Matchers
    with DefaultPlayMongoRepositorySupport[Entry]
    with ScalaFutures
    with IntegrationPatience
    with OptionValues
    with MockitoSugar {

  private val instant          = Instant.now
  private val stubClock: Clock = Clock.fixed(instant, ZoneId.systemDefault)

  val entryValue: JsObject = JsObject.apply(Seq("key" -> JsString("value")))
  private val entry        = Entry("1234", JsObject.apply(Seq("path" -> entryValue)), Instant.ofEpochSecond(1))

  private val mockAppConfig = mock[FrontendAppConfig]
  when(mockAppConfig.cacheTtl) thenReturn 1

  protected override val repository = new SessionRepository(
    mongoComponent = mongoComponent,
    appConfig = mockAppConfig,
    clock = stubClock
  )

  ".set" - {

    "must set the last updated time on the supplied user answers to `now`, and save them" in {

      val expectedResult = entry copy (lastUpdated = instant.truncatedTo(ChronoUnit.MILLIS))

      val setResult     = repository.set(entry.id, JsPath \ "path", entryValue).futureValue
      val updatedRecord = find(Filters.equal("_id", entry.id)).futureValue.headOption.value

      setResult mustEqual true
      verifyUserAnswerResult(updatedRecord, expectedResult)
    }
  }

  ".get" - {
    "when there is no record for this id" - {

      "must return None" in {

        repository.get[String](entry.id, JsPath).futureValue must not be defined
      }
    }
  }

  ".keepAlive" - {

    "when there is a record for this id" - {

      "must update its lastUpdated to `now` and return true" in {

        insert(entry).futureValue

        val result = repository.keepAlive(entry.id).futureValue

        val expectedUpdatedAnswers = entry copy (lastUpdated = instant)

        result mustEqual true
        val updatedAnswers = find(Filters.equal("_id", entry.id)).futureValue.headOption.value

        verifyUserAnswerResult(updatedAnswers, expectedUpdatedAnswers)
      }
    }

    "when there is no record for this id" - {

      "must return true" in {

        repository.keepAlive("id that does not exist").futureValue mustEqual true
      }
    }
  }

  def verifyUserAnswerResult(actual: Entry, expected: Entry): Assertion = {
    actual.id mustEqual expected.id
    actual.data mustEqual expected.data
  }

}
