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
import connectors.CacheConnector
import controllers.{routes => appRoutes}
import forms.ExportedRecycledPlasticPackagingWeightFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ExportedRecycledPlasticPackagingWeightPage
import pages.returns.ExportedPlasticPackagingWeightPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.InsufficientEnrolments
import views.html.returns.ExportedRecycledPlasticPackagingWeightView

import scala.concurrent.Future

class ExportedRecycledPlasticPackagingWeightControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new ExportedRecycledPlasticPackagingWeightFormProvider()
  val form = formProvider()

  def onwardRoute = Call("GET", "/foo")

  val validAnswer = 200L
  val exportedAmount = 8L

  lazy val exportedRecycledPlasticPackagingWeightRoute = routes.ExportedRecycledPlasticPackagingWeightController.onPageLoad(NormalMode).url

  val userAnswersWithExportAmount = userAnswers.set(ExportedPlasticPackagingWeightPage, value = exportedAmount).success.value

  "ExportedRecycledPlasticPackagingWeight Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithExportAmount)).build()

      running(application) {
        val view = application.injector.instanceOf[ExportedRecycledPlasticPackagingWeightView]

        val request = FakeRequest(GET, exportedRecycledPlasticPackagingWeightRoute)
        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, exportedAmount)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val ans = userAnswersWithExportAmount
        .set(ExportedRecycledPlasticPackagingWeightPage, validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(ans)).build()

      running(application) {
        val request = FakeRequest(GET, exportedRecycledPlasticPackagingWeightRoute)

        val view = application.injector.instanceOf[ExportedRecycledPlasticPackagingWeightView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), NormalMode, exportedAmount)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any(), any())(any())) thenReturn Future.successful(mockResponse)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithExportAmount))
          .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[CacheConnector].toInstance(mockCacheConnector)
          )
          .build()


      running(application) {
        val request =
          FakeRequest(POST, exportedRecycledPlasticPackagingWeightRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithExportAmount)).build()

      running(application) {
        val request =
          FakeRequest(POST, exportedRecycledPlasticPackagingWeightRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[ExportedRecycledPlasticPackagingWeightView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, exportedAmount)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, exportedRecycledPlasticPackagingWeightRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual appRoutes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, exportedRecycledPlasticPackagingWeightRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual appRoutes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "raise and error" - {
      "when not authorised" in {
        val application = applicationBuilderFailedAuth(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, exportedRecycledPlasticPackagingWeightRoute)

          val result = route(application, request).value

          intercept[InsufficientEnrolments](status(result))
        }
      }

      "when exported amount not found" in {

        val userAnswers = UserAnswers(userAnswersId).set(ExportedRecycledPlasticPackagingWeightPage, validAnswer).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, exportedRecycledPlasticPackagingWeightRoute)

          val result = route(application, request).value

          intercept[IllegalStateException](status(result))
        }
      }
    }
  }
}
