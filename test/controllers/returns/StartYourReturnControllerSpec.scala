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
import config.{Features, FrontendAppConfig}
import connectors.CacheConnector
import controllers.helpers.TaxReturnHelper
import forms.returns.StartYourReturnFormProvider
import models.Mode.NormalMode
import models.UserAnswers
import models.returns.TaxReturnObligation
import navigation.ReturnsJourneyNavigator
import org.mockito.ArgumentMatchers.refEq
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito._
import org.mockito.MockitoSugar.mock
import org.mockito.{ArgumentMatchers, Mockito}
import org.scalatest.BeforeAndAfterEach
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

class StartYourReturnControllerSpec extends SpecBase with BeforeAndAfterEach {

  private val mockTaxReturnHelper: TaxReturnHelper = mock[TaxReturnHelper]
  private val formProvider: StartYourReturnFormProvider = new StartYourReturnFormProvider()
  private val mockAuditConnector: AuditConnector = mock[AuditConnector]
  private val mockCacheConnector = mock[CacheConnector]
  private val navigator = mock[ReturnsJourneyNavigator]
  private val isFirst = true
  private lazy val startYourReturnRoute = controllers.returns.routes.StartYourReturnController.onPageLoad(NormalMode).url

  private val obligation: TaxReturnObligation = TaxReturnObligation(
    fromDate = LocalDate.parse("2022-04-01"),
    toDate = LocalDate.parse("2022-06-30"),
    dueDate = LocalDate.parse("2022-09-30"),
    periodKey = "22AC"
  )

  private def any[T] = ArgumentMatchers.any[T]()
  
  override protected def beforeEach(): Unit = {
    super.beforeEach
    reset(mockTaxReturnHelper, mockAuditConnector, mockCacheConnector, navigator)
    when(mockCacheConnector.saveUserAnswerFunc(any)(any)) thenReturn ((_, _) => Future.successful(true))
  }

  "StartYourReturn Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockTaxReturnHelper.nextOpenObligationAndIfFirst(any)(any)).thenReturn(Future.successful(Some((obligation, isFirst))))
      when(mockCacheConnector.set(any, any)(any)).thenReturn(Future.successful(HttpResponse.apply(200, "")))

      val pptId = "123"
      val userAnswers = UserAnswers(pptId).set(ObligationCacheable, obligation).get

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .configure("bootstrap.filters.csrf.enabled" -> false)
        .overrides(
          bind[TaxReturnHelper].toInstance(mockTaxReturnHelper),
          bind[CacheConnector].toInstance(mockCacheConnector))
        .build()

      running(application) {
        val request = FakeRequest(GET, startYourReturnRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[StartYourReturnView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode, obligation, isFirst)(request, messages(application)).toString

        verify(mockCacheConnector, atLeastOnce).saveUserAnswerFunc(refEq(pptId))(any)
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      when(mockTaxReturnHelper.nextOpenObligationAndIfFirst(any)(any)).thenReturn(Future.successful(Some((obligation, isFirst))))
      when(mockCacheConnector.set(any, any)(any)).thenReturn(Future.successful(HttpResponse.apply(200, "")))

      val userAnswers = UserAnswers(userAnswersId).set(StartYourReturnPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .configure("bootstrap.filters.csrf.enabled" -> false)
        .overrides(
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

      val userAnswers = UserAnswers(userAnswersId)
        .setOrFail(ObligationCacheable, taxReturnOb)
        .setOrFail("isFirstReturn", true)

      when(mockCacheConnector.set(any, any)(any)) thenReturn Future.successful(mockResponse)
      when(config.isFeatureEnabled(Features.creditsForReturnsEnabled)) thenReturn true
      when(navigator.startYourReturnRoute(any, any)) thenReturn Call("GET", "/toast")

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[CacheConnector].toInstance(mockCacheConnector),
            bind[TaxReturnHelper].toInstance(mockTaxReturnHelper),
            bind[AuditConnector].toInstance(mockAuditConnector),
            bind[FrontendAppConfig].toInstance(config),
            bind[ReturnsJourneyNavigator] to navigator
          )
          .build()

      val result = running(application) {
        val request = FakeRequest(POST, startYourReturnRoute).withFormUrlEncodedBody(("value", "true"))
        route(application, request).value
      }

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual "/toast"
      verify(navigator).startYourReturnRoute(true, true)
    }

    "must audit started event when user answers yes" in {

      Mockito.reset(mockAuditConnector)

      val userAnswers = UserAnswers(userAnswersId)
        .setOrFail(ObligationCacheable, taxReturnOb)
        .setOrFail("isFirstReturn", true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
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
          sendExplicitAudit(eqTo(ReturnStarted.eventType), any[ReturnStarted])(any, any, any)

      }
    }

    "must not audit started event when user answers no" in {

      Mockito.reset(mockAuditConnector)

      val userAnswers = UserAnswers(userAnswersId)
        .setOrFail(ObligationCacheable, taxReturnOb)
        .setOrFail("isFirstReturn", true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
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
          sendExplicitAudit(eqTo(ReturnStarted.eventType), any[ReturnStarted])(any, any, any)

      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockTaxReturnHelper.nextOpenObligationAndIfFirst(any)(any)).thenReturn(Future.successful(Some((obligation, isFirst))))

      val userAnswers = UserAnswers(userAnswersId)
        .setOrFail(ObligationCacheable, taxReturnOb)
        .setOrFail("isFirstReturn", true)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).overrides(
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

      when(mockTaxReturnHelper.nextOpenObligationAndIfFirst(any)(any)).thenReturn(Future.successful(None))
      when(mockCacheConnector.set(any, any)(any)).thenReturn(Future.successful(HttpResponse.apply(200, "")))


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
