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

package controllers.amends

import base.SpecBase
import connectors.DownstreamServiceError
import models.returns.{ProcessingEntry, ProcessingStatus}
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar.{mock, reset, when}
import org.scalatest.BeforeAndAfterEach
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.ReturnsProcessingRepository
import views.html.amends.AmendProcessingView

import scala.concurrent.Future

class AmendProcessingControllerSpec extends SpecBase with BeforeAndAfterEach {

  private val mockRepository = mock[ReturnsProcessingRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockRepository)
  }

  "AmendProcessing Controller" - {

    "must return OK and the correct view when status is Processing" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[ReturnsProcessingRepository].toInstance(mockRepository))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.amends.routes.AmendProcessingController.onPageLoad().url)
        when(mockRepository.get(any)).thenReturn(
          Future.successful(Some(ProcessingEntry("123", ProcessingStatus.Processing)))
        )

        val result = route(application, request).value

        val view = application.injector.instanceOf[AmendProcessingView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }

    "must redirect to ReturnConfirmationController when status is Complete" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[ReturnsProcessingRepository].toInstance(mockRepository))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.amends.routes.AmendProcessingController.onPageLoad().url)

        when(mockRepository.get(any)).thenReturn(
          Future.successful(Some(ProcessingEntry("123", ProcessingStatus.Complete)))
        )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.amends.routes.AmendConfirmationController.onPageLoad().url
      }
    }

    "must throw DownstreamServiceError when status is Failed" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[ReturnsProcessingRepository].toInstance(mockRepository))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.amends.routes.AmendProcessingController.onPageLoad().url)

        when(mockRepository.get(any)).thenReturn(
          Future.successful(Some(ProcessingEntry("123", ProcessingStatus.Failed)))
        )

        val exception = intercept[DownstreamServiceError] {
          await(route(application, request).value)
        }

        exception.getMessage must include("Failed to amend return")
      }
    }

    "must return correct status for onPageLoadStatus" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[ReturnsProcessingRepository].toInstance(mockRepository))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.amends.routes.AmendProcessingController.onPageLoadStatus().url)

        when(mockRepository.get(any)).thenReturn(
          Future.successful(Some(ProcessingEntry("123", ProcessingStatus.Complete)))
        )
        var result = route(application, request).value
        status(result) mustEqual OK
        contentAsJson(result) mustEqual Json.obj("status" -> "complete")

        when(mockRepository.get(any)).thenReturn(
          Future.successful(Some(ProcessingEntry("123", ProcessingStatus.Processing)))
        )
        result = route(application, request).value
        status(result) mustEqual OK
        contentAsJson(result) mustEqual Json.obj("status" -> "processing")

        when(mockRepository.get(any)).thenReturn(
          Future.successful(Some(ProcessingEntry("123", ProcessingStatus.Failed)))
        )
        result = route(application, request).value
        status(result) mustEqual OK
        contentAsJson(result) mustEqual Json.obj("status" -> "failed")

        when(mockRepository.get(any)).thenReturn(Future.successful(None))
        result = route(application, request).value
        status(result) mustEqual OK
        contentAsJson(result) mustEqual Json.obj("status" -> "not-found")
      }
    }
  }
}
