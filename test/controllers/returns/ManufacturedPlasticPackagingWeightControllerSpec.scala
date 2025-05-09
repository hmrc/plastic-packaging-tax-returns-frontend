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

package controllers.returns

import base.SpecBase
import connectors.CacheConnector
import forms.returns.ManufacturedPlasticPackagingWeightFormProvider
import models.Mode.NormalMode
import navigation.ReturnsJourneyNavigator
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.when
import org.scalatestplus.mockito.MockitoSugar
import pages.returns.ManufacturedPlasticPackagingWeightPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.returns.ManufacturedPlasticPackagingWeightView

import scala.concurrent.Future

class ManufacturedPlasticPackagingWeightControllerSpec extends SpecBase with MockitoSugar {

  val formProvider       = new ManufacturedPlasticPackagingWeightFormProvider()
  val mockCacheConnector = mock[CacheConnector]

  def onwardRoute = Call("GET", "/foo")

  val validAnswer: Long = 1

  lazy val ManufacturedPlasticPackagingWeightRoute =
    controllers.returns.routes.ManufacturedPlasticPackagingWeightController.onPageLoad(NormalMode).url

  val navigator = mock[ReturnsJourneyNavigator]
  when(navigator.manufacturedPlasticPackagingWeightPage(any)).thenReturn(onwardRoute)
  when(mockSessionRepo.get[Boolean](any, any)(any)).thenReturn(Future.successful(Some(false)))

  "ManufacturedPlasticPackagingWeight Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, ManufacturedPlasticPackagingWeightRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ManufacturedPlasticPackagingWeightView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode, taxReturnOb)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val ans = userAnswers.set(ManufacturedPlasticPackagingWeightPage, validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(ans)).build()

      running(application) {
        val request = FakeRequest(GET, ManufacturedPlasticPackagingWeightRoute)

        val view = application.injector.instanceOf[ManufacturedPlasticPackagingWeightView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill(validAnswer), NormalMode, taxReturnOb)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockCacheConnector.set(any(), any())(any())) thenReturn Future.successful(mockResponse)

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[ReturnsJourneyNavigator].toInstance(navigator),
          bind[CacheConnector].toInstance(mockCacheConnector)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, ManufacturedPlasticPackagingWeightRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, ManufacturedPlasticPackagingWeightRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = formProvider().bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[ManufacturedPlasticPackagingWeightView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, taxReturnOb)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, ManufacturedPlasticPackagingWeightRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad.url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, ManufacturedPlasticPackagingWeightRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad.url
      }
    }

  }
}
