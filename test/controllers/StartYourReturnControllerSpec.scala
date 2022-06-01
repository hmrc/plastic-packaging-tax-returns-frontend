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
import cacheables.ObligationCacheable
import connectors.{CacheConnector, ObligationsConnector}
import forms.StartYourReturnFormProvider
import models.obligations.PPTObligations
import models.returns.TaxReturnObligation
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.Mockito.{atLeastOnce, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.StartYourReturnPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.StartYourReturnView
import controllers.returns.{routes => routes}

import java.time.LocalDate
import scala.concurrent.Future

class StartYourReturnControllerSpec extends SpecBase with MockitoSugar with MockObligationsConnector  {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new StartYourReturnFormProvider()
  val form = formProvider()

  lazy val startYourReturnRoute = controllers.returns.routes.StartYourReturnController.onPageLoad(NormalMode).url

  val obligation: TaxReturnObligation = TaxReturnObligation(
    fromDate = LocalDate.parse("2022-04-01"),
    toDate = LocalDate.parse("2022-06-30"),
    dueDate = LocalDate.parse("2022-09-30"),
    periodKey = "22AC"
  )

  val pptObligation: PPTObligations = PPTObligations(Some(obligation), Some(obligation), 1, true, true)

  val mockCacheConnector = mock[CacheConnector]

  "StartYourReturn Controller" - {

    "must return OK and the correct view for a GET" in {

      mockGetObligations(pptObligation)

      val pptId = "123"
      val userAnswers = UserAnswers(pptId).set(ObligationCacheable, obligation).get

      val application = applicationBuilder(userAnswers = Some(userAnswers)).overrides(
        bind[ObligationsConnector].toInstance(mockObligationsConnector),
          bind[CacheConnector].toInstance(mockCacheConnector)
      ).build()

      running(application) {
        val request = FakeRequest(GET, startYourReturnRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[StartYourReturnView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, obligation)(request, messages(application)).toString

        verify(mockCacheConnector, atLeastOnce).set(refEq(pptId), refEq(userAnswers))(any())
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      mockGetObligations(pptObligation)

      val userAnswers = UserAnswers(userAnswersId).set(StartYourReturnPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).overrides(
        bind[ObligationsConnector].toInstance(mockObligationsConnector)
      ).build()

      running(application) {
        val request = FakeRequest(GET, startYourReturnRoute)

        val view = application.injector.instanceOf[StartYourReturnView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode, obligation)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      mockGetObligations(pptObligation)

      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any(), any())(any())) thenReturn Future.successful(mockResponse)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[CacheConnector].toInstance(mockCacheConnector),
            bind[ObligationsConnector].toInstance(mockObligationsConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, startYourReturnRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      mockGetObligations(pptObligation)

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(
        bind[ObligationsConnector].toInstance(mockObligationsConnector)
      ).build()

      running(application) {
        val request =
          FakeRequest(POST, startYourReturnRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[StartYourReturnView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, obligation)(request, messages(application)).toString

      }
    }
  }
}
