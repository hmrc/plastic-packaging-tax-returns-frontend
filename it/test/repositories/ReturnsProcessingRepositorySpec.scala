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

package repositories

import config.FrontendAppConfig
import models.returns.{ProcessingEntry, ProcessingStatus}
import org.mongodb.scala.model.Filters
import org.mockito.MockitoSugar.when
import org.scalatest.{Assertion, OptionValues}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsError, JsString, Json}
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import models.returns.ProcessingStatus.format

import java.time.temporal.ChronoUnit

class ReturnsProcessingRepositorySpec
  extends AnyFreeSpec
    with Matchers
    with DefaultPlayMongoRepositorySupport[ProcessingEntry]
    with ScalaFutures
    with IntegrationPatience
    with OptionValues
    with MockitoSugar {

  private val instant          = Instant.now

  private val processingEntry = ProcessingEntry("1234", ProcessingStatus.Complete, Some("Completed successfully"), instant)

  private val mockAppConfig = mock[FrontendAppConfig]
  when(mockAppConfig.cacheTtl) thenReturn 1

  protected override val repository = new ReturnsProcessingRepository(
    mongoComponent = mongoComponent,
    appConfig = mockAppConfig
  )

  ".set" - {

    "must set the last updated time on the supplied processing entry to `now`, and save it" in {
      val setResult     = repository.set(processingEntry).futureValue
      val updatedRecord = find(Filters.equal("_id", processingEntry.id)).futureValue.headOption.value

      verifyProcessingEntryResult(updatedRecord, processingEntry)
    }
  }

  ".get" - {
    "when there is no record for this id" - {

      "must return None" in {

        repository.get("non-existent-id").futureValue must not be defined
      }
    }

    "when there is a record for this id" - {

      "must return the record" in {

        insert(processingEntry).futureValue

        val result = repository.get(processingEntry.id).futureValue.value

        verifyProcessingEntryResult(result, processingEntry)
      }
    }
  }

  def verifyProcessingEntryResult(actual: ProcessingEntry, expected: ProcessingEntry): Assertion = {
    actual.id mustEqual expected.id
    actual.status mustEqual expected.status
    actual.message mustEqual expected.message
    actual.lastUpdated.truncatedTo(ChronoUnit.MILLIS) mustEqual expected.lastUpdated.truncatedTo(ChronoUnit.MILLIS)
  }

  "ProcessingStatus" - {

    "must serialize and deserialize correctly" in {
      val statuses = Seq(
        ProcessingStatus.Processing,
        ProcessingStatus.AlreadySubmitted,
        ProcessingStatus.Complete,
        ProcessingStatus.Failed
      )

      statuses.foreach { status =>
        val json = Json.toJson(status)(ProcessingStatus.format.writes)
        json.as[ProcessingStatus] mustEqual status
      }
    }

    "must fail to deserialize invalid status" in {
      val invalidJson = JsString("InvalidStatus")
      invalidJson.validate[ProcessingStatus] mustEqual JsError("Invalid ProcessingStatus")
    }
  }

  "ProcessingEntry" - {

    "must serialize and deserialize correctly" in {
      val entry = ProcessingEntry(
        id = "1234",
        status = ProcessingStatus.Complete,
        message = Some("Completed successfully"),
        lastUpdated = Instant.now().truncatedTo(ChronoUnit.MILLIS)
      )

      val json = Json.toJson(entry)
      val result = json.as[ProcessingEntry]
      result.copy(lastUpdated = result.lastUpdated.truncatedTo(ChronoUnit.MILLIS)) mustEqual entry
    }

    "must fail to deserialize invalid entry" in {
      val invalidJson = Json.obj(
        "_id" -> "1234",
        "status" -> "InvalidStatus",
        "message" -> "Completed successfully",
        "lastUpdated" -> Instant.now().toString
      )

      invalidJson.validate[ProcessingEntry] mustBe a[JsError]
    }
  }
}