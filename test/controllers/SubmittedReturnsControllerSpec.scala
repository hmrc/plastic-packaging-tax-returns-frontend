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

import base.{MockObligationsConnector, SpecBase}
import connectors.ObligationsConnector
import models.returns.TaxReturnObligation
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.SubmittedReturnsView

import java.time.LocalDate
import scala.concurrent.Future

class SubmittedReturnsControllerSpec extends SpecBase with MockObligationsConnector {

  "SubmittedReturns Controller" - {

    "must return OK and the correct view for a GET with no obligations" in {

      val ob: Seq[TaxReturnObligation] =
        Seq.empty

      setUpMocks(ob)

      val application = applicationBuilder(userAnswers = None).overrides(
        bind[ObligationsConnector].toInstance(mockObligationsConnector)
      ).build()

      running(application) {
        val request = FakeRequest(GET, routes.SubmittedReturnsController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SubmittedReturnsView]

        val ob: Seq[TaxReturnObligation] = {
          Seq.empty
        }

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(ob)(request, messages(application)).toString

      }
    }

    "must return OK and the correct view for a GET with obligations" in {

      val ob: Seq[TaxReturnObligation] =
        Seq(
          TaxReturnObligation(
            LocalDate.now(),
            LocalDate.now(),
            LocalDate.now().plusWeeks(4),
            "PK1"
          ),
          TaxReturnObligation(
            LocalDate.now(),
            LocalDate.now().plusWeeks(4),
            LocalDate.now().plusWeeks(8),
            "PK2"
          )
        )

      setUpMocks(ob)

      val application = applicationBuilder(userAnswers = None).overrides(
        bind[ObligationsConnector].toInstance(mockObligationsConnector)
      ).build()

      running(application) {
        val request = FakeRequest(GET, routes.SubmittedReturnsController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SubmittedReturnsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(ob)(request, messages(application)).toString

      }
    }
  }

  private def setUpMocks(obligation: Seq[TaxReturnObligation]) = {
    reset(mockObligationsConnector)

    when(mockObligationsConnector.getFulfilled(any[String])(any())).thenReturn(Future.successful(obligation))

  }
}
