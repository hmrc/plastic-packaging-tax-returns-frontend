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

package controllers.amends

import base.SpecBase
import cacheables.{AmendObligationCacheable, ReturnDisplayApiCacheable}
import connectors.CacheConnector
import forms.amends.AmendHumanMedicinePlasticPackagingFormProvider
import models.UserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.when
import org.scalatestplus.mockito.MockitoSugar
import pages.amends.AmendHumanMedicinePlasticPackagingPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.amends.AmendHumanMedicinePlasticPackagingView

import scala.concurrent.Future

class AmendHumanMedicinePlasticPackagingControllerSpec extends SpecBase with MockitoSugar {

  override def userAnswers: UserAnswers = UserAnswers(userAnswersId)
    .set(ReturnDisplayApiCacheable, retDisApi).get
    .set(AmendObligationCacheable, taxReturnOb).get

  val formProvider = new AmendHumanMedicinePlasticPackagingFormProvider()
  when(mockSessionRepo.get[Boolean](any, any)(any)).thenReturn(Future.successful(Some(false)))

  def onwardRoute = Call("GET", "/foo")

  val validAnswer: Long = 0

  lazy val amendHumanMedicinePlasticPackagingRoute =
    routes.AmendHumanMedicinePlasticPackagingController.onPageLoad().url

  "AmendHumanMedicinePlasticPackaging Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, amendHumanMedicinePlasticPackagingRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AmendHumanMedicinePlasticPackagingView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider())(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val ans = userAnswers.set(AmendHumanMedicinePlasticPackagingPage, validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(ans)).build()

      running(application) {
        val request = FakeRequest(GET, amendHumanMedicinePlasticPackagingRoute)

        val view = application.injector.instanceOf[AmendHumanMedicinePlasticPackagingView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill(validAnswer))(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any(), any())(any())) thenReturn Future.successful(mockResponse)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[CacheConnector].toInstance(mockCacheConnector))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, amendHumanMedicinePlasticPackagingRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.CheckYourAnswersController.onPageLoad().url
      }
    }

    "must redirect when previous tax return is not in user answers" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, amendHumanMedicinePlasticPackagingRoute)

        val result = route(application, request).value

        redirectLocation(result) mustBe Some(routes.SubmittedReturnsController.onPageLoad().url)
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, amendHumanMedicinePlasticPackagingRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = formProvider().bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[AmendHumanMedicinePlasticPackagingView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, amendHumanMedicinePlasticPackagingRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad.url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, amendHumanMedicinePlasticPackagingRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad.url
      }
    }
  }
}
