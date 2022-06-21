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

package controllers.returns

import base.SpecBase
import base.utils.NonExportedPlasticTestHelper
import connectors.CacheConnector
import controllers.{routes => appRoutes}
import forms.returns.NonExportedRecycledPlasticPackagingWeightFormProvider
import models.NormalMode
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.returns.NonExportedRecycledPlasticPackagingWeightPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.returns.NonExportedRecycledPlasticPackagingWeightView

import scala.concurrent.Future

class NonExportedRecycledPlasticPackagingWeightControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new NonExportedRecycledPlasticPackagingWeightFormProvider()
  val form = formProvider()

  def onwardRoute = Call("GET", "/foo")

  val validAnswer: Long = 0L
  val manufacturedAmount = 200L
  val importedAmount = 100L
  val exportedAmount = 50L
  val nonExportedAmount = manufacturedAmount + importedAmount - exportedAmount
  lazy val nonExportedAnswer = NonExportedPlasticTestHelper.createUserAnswer(exportedAmount, manufacturedAmount, importedAmount)

  lazy val recycledPlasticPackagingWeightRoute =
    controllers.returns.routes.NonExportedRecycledPlasticPackagingWeightController.onPageLoad(NormalMode).url

  "RecycledPlasticPackagingWeight Controller" - {

    "must return OK and the correct view for a GET" in {

      val ans = nonExportedAnswer
        .set(NonExportedRecycledPlasticPackagingWeightPage, validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(ans)).build()

      running(application) {
        val request = FakeRequest(GET, recycledPlasticPackagingWeightRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[NonExportedRecycledPlasticPackagingWeightView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), NormalMode, nonExportedAmount)(request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val ans = nonExportedAnswer
        .set(NonExportedRecycledPlasticPackagingWeightPage, validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(ans)).build()

      running(application) {
        val request = FakeRequest(GET, recycledPlasticPackagingWeightRoute)

        val view = application.injector.instanceOf[NonExportedRecycledPlasticPackagingWeightView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), NormalMode, nonExportedAmount)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect GET to home page when exported amount not found" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers.set(NonExportedRecycledPlasticPackagingWeightPage, validAnswer).success.value)).build()

      running(application) {
        val request = FakeRequest(GET, recycledPlasticPackagingWeightRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual appRoutes.IndexController.onPageLoad.url
      }
    }
    "must redirect to the next page when valid data is submitted" in {

      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any(), any())(any())) thenReturn Future.successful(mockResponse)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[CacheConnector].toInstance(mockCacheConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, recycledPlasticPackagingWeightRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(nonExportedAnswer)).build()

      running(application) {
        val request =
          FakeRequest(POST, recycledPlasticPackagingWeightRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[NonExportedRecycledPlasticPackagingWeightView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, nonExportedAmount)(request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, recycledPlasticPackagingWeightRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad.url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, recycledPlasticPackagingWeightRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad.url
      }
    }
  }
}
