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
import connectors.{ServiceError, TaxReturnsConnector}
import controllers.helpers.TaxLiability
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.mockito.stubbing.OngoingStubbing
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import viewmodels.govuk.SummaryListFluency
import views.html.ReturnsCheckYourAnswersView

import scala.concurrent.Future

class ReturnsCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  def setupMock: OngoingStubbing[Future[Either[ServiceError, Option[String]]]] = {

    reset(mockSessionRepo, mockTaxReturnConnector)

    when(mockSessionRepo.set(any())).thenReturn(Future.successful(true))

    when(mockTaxReturnConnector.submit(any())(any())).thenReturn(Future.successful(Right(Some("12345"))))

  }

  "Returns Check Your Answers Controller" - {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, controllers.returns.routes.ReturnsCheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[ReturnsCheckYourAnswersView]
        val list = SummaryListViewModel(Seq.empty)
        val liability = TaxLiability()
        val mode = NormalMode

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(mode, list, liability, taxReturnOb)(request, messages(application)).toString

      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {

        val request = FakeRequest(GET, controllers.returns.routes.ReturnsCheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url

      }
    }

    "must cache payment ref and redirect for a POST" in {

      setupMock

      val application = applicationBuilder(userAnswers = Some(userAnswers)).overrides(
        bind[SessionRepository].toInstance(mockSessionRepo),
        bind[TaxReturnsConnector].toInstance(mockTaxReturnConnector)
      ).build()

      running(application) {

        val request = FakeRequest(POST, controllers.returns.routes.ReturnsCheckYourAnswersController.onSubmit().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.returns.routes.ReturnConfirmationController.onPageLoad().url

        verify(mockSessionRepo).set(any())

      }
    }
  }
}
