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
import cacheables.{AmendSelectedPeriodKey, ReturnDisplayApiCacheable}
import connectors.CacheConnector
import forms.AmendDirectExportPlasticPackagingFormProvider
import models.returns.{IdDetails, ReturnDisplayApi, ReturnDisplayChargeDetails, ReturnDisplayDetails}
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.AmendDirectExportPlasticPackagingPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.AmendDirectExportPlasticPackagingView

import scala.concurrent.Future

class AmendDirectExportPlasticPackagingControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new AmendDirectExportPlasticPackagingFormProvider()
  val form         = formProvider()

  def onwardRoute = Call("GET", "/foo")

  val validAnswer = 0

  val charge: ReturnDisplayChargeDetails = ReturnDisplayChargeDetails(
    periodFrom = "2022-04-01",
    periodTo = "2022-06-30",
    periodKey = "22AC",
    chargeReference = Some("pan"),
    receiptDate = "2022-06-31",
    returnType = "TYPE"
  )

  val retDisApi: ReturnDisplayApi = ReturnDisplayApi(
    "",
    IdDetails("", ""),
    Some(charge),
    ReturnDisplayDetails(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
  )

  lazy val amendDirectExportPlasticPackagingRoute =
    routes.AmendDirectExportPlasticPackagingController.onPageLoad(NormalMode).url

  "AmendDirectExportPlasticPackaging Controller" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(ReturnDisplayApiCacheable, retDisApi).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, amendDirectExportPlasticPackagingRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AmendDirectExportPlasticPackagingView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, retDisApi)(request,
                                                                 messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(AmendDirectExportPlasticPackagingPage, validAnswer).get
        .set(ReturnDisplayApiCacheable, retDisApi).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, amendDirectExportPlasticPackagingRoute)

        val view = application.injector.instanceOf[AmendDirectExportPlasticPackagingView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), NormalMode, retDisApi)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockCacheConnector = mock[CacheConnector]

      val userAnswers = UserAnswers(userAnswersId)
        .set(AmendDirectExportPlasticPackagingPage, validAnswer).get
        .set(ReturnDisplayApiCacheable, retDisApi).success.value

      when(mockCacheConnector.set(any(), any())(any())) thenReturn Future.successful(mockResponse)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
                     bind[CacheConnector].toInstance(mockCacheConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, amendDirectExportPlasticPackagingRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(ReturnDisplayApiCacheable, retDisApi).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, amendDirectExportPlasticPackagingRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[AmendDirectExportPlasticPackagingView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, retDisApi)(request,
                                                                      messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, amendDirectExportPlasticPackagingRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, amendDirectExportPlasticPackagingRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
