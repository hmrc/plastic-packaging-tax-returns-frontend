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

import audit.returns.ReturnStarted
import base.SpecBase
import cacheables.ObligationCacheable
import connectors.CacheConnector
import controllers.helpers.TaxReturnHelper
import forms.returns.StartYourReturnFormProvider
import models.returns.TaxReturnObligation
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito
import org.mockito.Mockito.{atLeastOnce, times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.returns.StartYourReturnPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import views.html.returns.StartYourReturnView

import java.time.LocalDate
import scala.concurrent.Future

class StartYourReturnControllerSpec extends SpecBase with MockitoSugar  {

  val mockTaxReturnHelper: TaxReturnHelper = mock[TaxReturnHelper]
  def onwardRoute = Call("GET", "/foo")
  val formProvider = new StartYourReturnFormProvider()
  val mockAuditConnector   = mock[AuditConnector]

  lazy val startYourReturnRoute = controllers.returns.routes.StartYourReturnController.onPageLoad(NormalMode).url

  val obligation: TaxReturnObligation = TaxReturnObligation(
    fromDate = LocalDate.parse("2022-04-01"),
    toDate = LocalDate.parse("2022-06-30"),
    dueDate = LocalDate.parse("2022-09-30"),
    periodKey = "22AC"
  )
  val isFirst = true

  val mockCacheConnector = mock[CacheConnector]

  "StartYourReturn Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockTaxReturnHelper.nextOpenObligationAndIfFirst(any())(any())).thenReturn(Future.successful(Some((obligation,isFirst))))
      when(mockCacheConnector.set(any(), any())(any())).thenReturn(Future.successful(HttpResponse.apply(200, "")))

      val pptId = "123"
      val userAnswers = UserAnswers(pptId).set(ObligationCacheable, obligation).get

      val application = applicationBuilder(userAnswers = Some(userAnswers)).overrides(
        bind[TaxReturnHelper].toInstance(mockTaxReturnHelper),
          bind[CacheConnector].toInstance(mockCacheConnector)
      ).build()

      running(application) {
        val request = FakeRequest(GET, startYourReturnRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[StartYourReturnView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode, obligation, isFirst)(request, messages(application)).toString

        verify(mockCacheConnector, atLeastOnce).set(refEq(pptId), refEq(userAnswers))(any())
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      when(mockTaxReturnHelper.nextOpenObligationAndIfFirst(any())(any())).thenReturn(Future.successful(Some((obligation,isFirst))))
      when(mockCacheConnector.set(any(), any())(any())).thenReturn(Future.successful(HttpResponse.apply(200, "")))

      val userAnswers = UserAnswers(userAnswersId).set(StartYourReturnPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).overrides(
        bind[TaxReturnHelper].toInstance(mockTaxReturnHelper),
        bind[CacheConnector].toInstance(mockCacheConnector)
      ).build()

      running(application) {
        val request = FakeRequest(GET, startYourReturnRoute)

        val view = application.injector.instanceOf[StartYourReturnView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill(true), NormalMode, obligation, isFirst)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any(), any())(any())) thenReturn Future.successful(mockResponse)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[CacheConnector].toInstance(mockCacheConnector),
            bind[TaxReturnHelper].toInstance(mockTaxReturnHelper),
            bind[AuditConnector].toInstance(mockAuditConnector)
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

    "must audit started event when user answers yes" in {

      Mockito.reset(mockAuditConnector)

      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any(), any())(any())) thenReturn Future.successful(mockResponse)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[CacheConnector].toInstance(mockCacheConnector),
            bind[TaxReturnHelper].toInstance(mockTaxReturnHelper),
            bind[AuditConnector].toInstance(mockAuditConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, startYourReturnRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        verify(mockAuditConnector, times(1)).
          sendExplicitAudit(eqTo(ReturnStarted.eventType), any[ReturnStarted])(any(), any(), any())

      }
    }

    "must not audit started event when user answers no" in {

      Mockito.reset(mockAuditConnector)

      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any(), any())(any())) thenReturn Future.successful(mockResponse)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[CacheConnector].toInstance(mockCacheConnector),
            bind[TaxReturnHelper].toInstance(mockTaxReturnHelper),
            bind[AuditConnector].toInstance(mockAuditConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, startYourReturnRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        verify(mockAuditConnector, times(0)).
          sendExplicitAudit(eqTo(ReturnStarted.eventType), any[ReturnStarted])(any(), any(), any())

      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockTaxReturnHelper.nextOpenObligationAndIfFirst(any())(any())).thenReturn(Future.successful(Some((obligation,isFirst))))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(
        bind[TaxReturnHelper].toInstance(mockTaxReturnHelper)
      ).build()

      running(application) {
        val request =
          FakeRequest(POST, startYourReturnRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = formProvider().bind(Map("value" -> ""))

        val view = application.injector.instanceOf[StartYourReturnView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, obligation, isFirst)(request, messages(application)).toString

      }
    }

    "redirect to account home when no obligation to start a return" in {

      when(mockTaxReturnHelper.nextOpenObligationAndIfFirst(any())(any())).thenReturn(Future.successful(None))
      when(mockCacheConnector.set(any(), any())(any())).thenReturn(Future.successful(HttpResponse.apply(200, "")))


      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(
        bind[TaxReturnHelper].toInstance(mockTaxReturnHelper)
      ).build()

      running(application) {
        val request =
          FakeRequest(GET, startYourReturnRoute)

        val result = route(application, request).value

        redirectLocation(result) mustBe Some(controllers.routes.IndexController.onPageLoad.url)
      }
    }
  }
}
