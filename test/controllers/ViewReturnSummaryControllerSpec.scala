/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers

import base.SpecBase
import connectors.{CacheConnector, TaxReturnsConnector}
import models.returns.{IdDetails, ReturnDisplayApi, ReturnDisplayChargeDetails, ReturnDisplayDetails}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.checkAnswers.ViewReturnSummaryViewModel
import views.html.ViewReturnSummaryView

import scala.concurrent.Future

class ViewReturnSummaryControllerSpec extends SpecBase with MockitoSugar {

  private val mockConnector = mock[TaxReturnsConnector]

  val returnDisplayDetails = ReturnDisplayDetails(1, 1, 1, 1, 1, 1, 1, 1, 1, 1)
  val submittedReturn = ReturnDisplayApi("", IdDetails("", ""), Some(charge), returnDisplayDetails)

  "onPageLoad" - {
    "must return OK and the correct view" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          inject.bind[TaxReturnsConnector].toInstance(mockConnector),
          inject.bind[CacheConnector].toInstance(cacheConnector)
        )
        .build()

      running(application) {
        val viewModel = ViewReturnSummaryViewModel(submittedReturn)

        when(mockConnector.get(any(), any())(any())).thenReturn(
          Future.successful(Right(submittedReturn))
        )

        when(cacheConnector.set(any(), any())(any())).thenReturn(Future.successful(mockResponse))

        val request = FakeRequest(routes.ViewReturnSummaryController.onPageLoad("00XX"))

        val result = route(application, request).value

        val view = application.injector.instanceOf[ViewReturnSummaryView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view("April to June 2022", viewModel)(
          request,
          messages(application)
        ).toString

        verify(mockConnector).get(any(), ArgumentMatchers.eq("00XX"))(any())
      }
    }

    "must throw an error" - {
      "when the period key is malformed" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .build()

        running(application) {
          val request = FakeRequest(routes.ViewReturnSummaryController.onPageLoad("MALFORMED"))

          val ex = intercept[Exception](await(route(application, request).value))
          ex.getMessage mustBe "Period key 'MALFORMED' is not allowed."
        }
      }
    }
  }
}
