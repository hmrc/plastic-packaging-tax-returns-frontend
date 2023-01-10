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
import connectors.CacheConnector
import forms.amends.AmendExportedWeightFormProvider
import models.Mode.NormalMode
import models.UserAnswers
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.amends.AmendExportedWeightPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import views.html.amends.AmendExportedWeightView

import scala.concurrent.Future

// todo: replace test with proper real test, that do not start the application
class AmendExportedWeightControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new AmendExportedWeightFormProvider()
  val form = formProvider()

  def onwardRoute = Call("GET", "/foo")

  val validAnswer: Long = 0L

  lazy val amendExportedWeightRoute = routes.AmendExportedWeightController.onPageLoad(NormalMode).url

  "AmendExportedWeightControllerportedWeight Controller" - {

    "must return OK and the correct view for a GET" ignore  {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, amendExportedWeightRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AmendExportedWeightView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" ignore  {

      val userAnswers = UserAnswers(userAnswersId).set(AmendExportedWeightPage, validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, amendExportedWeightRoute)

        val view = application.injector.instanceOf[AmendExportedWeightView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" ignore  {

      val mockcacheConnector = mock[CacheConnector]

      when(mockcacheConnector.set(any, any)(any)) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[CacheConnector].toInstance(mockcacheConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, amendExportedWeightRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" ignore  {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, amendExportedWeightRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[AmendExportedWeightView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, amendExportedWeightRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad.url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, amendExportedWeightRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad.url
      }
    }
  }
}
