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

package controllers.amends

import base.SpecBase
import cacheables.AmendSelectedPeriodKey
import connectors.CacheConnector
import forms.amends.CancelAmendFormProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.amends.CancelAmendView

import scala.concurrent.Future

class CancelAmendControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  val formProvider = new CancelAmendFormProvider()
  val form = formProvider()

  lazy val cancelAmendRoute = controllers.amends.routes.CancelAmendController.onPageLoad.url

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(cacheConnector)
  }

  "CancelAmend Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(inject.bind[CacheConnector].toInstance(cacheConnector))
        .build()

      running(application) {
        val request = FakeRequest(GET, cancelAmendRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CancelAmendView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, taxReturnOb)(request, messages(application)).toString
      }
    }

    "must redirect to SubmittedReturns page when yes (will have no amended period key)" in {

      when(cacheConnector.saveUserAnswerFunc(any())(any())) thenReturn ((_, _) => Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(inject.bind[CacheConnector].toInstance(cacheConnector))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, cancelAmendRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SubmittedReturnsController.onPageLoad().url
      }
    }
    "must redirect back to amend heart page when no" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(inject.bind[CacheConnector].toInstance(cacheConnector))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, cancelAmendRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.CheckYourAnswersController.onPageLoad().url

      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(inject.bind[CacheConnector].toInstance(cacheConnector))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, cancelAmendRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[CancelAmendView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, taxReturnOb)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, cancelAmendRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad.url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, cancelAmendRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad.url
      }
    }
  }
}