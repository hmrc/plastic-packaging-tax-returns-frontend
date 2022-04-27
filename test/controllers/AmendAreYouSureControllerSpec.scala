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
import cacheables.{AmendSelectedPeriodKey, ObligationCacheable}
import connectors.CacheConnector
import controllers.helpers.TaxReturnHelper
import forms.AmendAreYouSureFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.Mockito.{atLeastOnce, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.AmendAreYouSurePage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.AmendAreYouSureView
import models.returns.{IdDetails, ReturnDisplayApi, ReturnDisplayChargeDetails, ReturnDisplayDetails, TaxReturnObligation}
import play.api.data.Form
import play.twirl.api.HtmlFormat

import java.time.LocalDate
import scala.concurrent.Future

class AmendAreYouSureControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider        = new AmendAreYouSureFormProvider()
  val form: Form[Boolean] = formProvider()

  val obligation: TaxReturnObligation = TaxReturnObligation(
    fromDate =
      LocalDate.parse("2022-04-01"),
    toDate = LocalDate.parse("2022-06-30"),
    dueDate = LocalDate.parse("2022-09-30"),
    periodKey = "22AC"
  )

  lazy val amendAreYouSureRoute: String =
    routes.AmendAreYouSureController.onPageLoad(NormalMode).url

  val mockService: TaxReturnHelper = mock[TaxReturnHelper]

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

  when(mockService.fetchTaxReturn(any(), any())(any())).thenReturn(Future.successful(retDisApi))
  when(mockService.getObligation(any(), any())(any())).thenReturn(
    Future.successful(Seq(obligation))
  )

  val mockCacheConnector = mock[CacheConnector]

  "AmendAreYouSure Controller" - {

    "must return OK and the correct view for a GET" in {
      val mockView = mock[AmendAreYouSureView]
      when(mockView.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)

      val userAnswers = UserAnswers(userAnswersId)
        .set(AmendSelectedPeriodKey, "TEST")
        .get.set(ObligationCacheable, obligation).get

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[TaxReturnHelper].toInstance(mockService),
          bind[AmendAreYouSureView].toInstance(mockView),
          bind[CacheConnector].toInstance(mockCacheConnector)
        ).build()

      running(application) {
        val request = FakeRequest(GET, amendAreYouSureRoute)

        val result = route(application, request).value

        status(result) mustEqual OK

        verify(mockView).apply(any(), any(), refEq(retDisApi))(any(), any())
        verify(mockCacheConnector, atLeastOnce).set(refEq(userAnswersId), refEq(userAnswers))(any())

      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(AmendAreYouSurePage, true).get
        .set(AmendSelectedPeriodKey, "TEST").get

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[TaxReturnHelper].toInstance(mockService))
        .build()

      running(application) {
        val request = FakeRequest(GET, amendAreYouSureRoute)

        val view = application.injector.instanceOf[AmendAreYouSureView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode, retDisApi)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect when periodKey is not in user answers" in {
      val userAnswers = UserAnswers(userAnswersId)

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[TaxReturnHelper].toInstance(mockService)).build()

      running(application) {
        val request = FakeRequest(GET, amendAreYouSureRoute)

        val result = route(application, request).value

        redirectLocation(result) mustBe Some("/go-and-select-a-year")
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any(), any())(any())) thenReturn Future.successful(mockResponse)

      val userAnswers = UserAnswers(userAnswersId)
        .set(AmendSelectedPeriodKey, "TEST").get

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[TaxReturnHelper].toInstance(mockService),
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[CacheConnector].toInstance(mockCacheConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, amendAreYouSureRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(AmendSelectedPeriodKey, "TEST").get

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[TaxReturnHelper].toInstance(mockService))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, amendAreYouSureRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[AmendAreYouSureView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, retDisApi)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, amendAreYouSureRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, amendAreYouSureRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
