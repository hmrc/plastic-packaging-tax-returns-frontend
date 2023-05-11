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

package controllers.returns

import audit.returns.ReturnStarted
import base.SpecBase
import cacheables.ReturnObligationCacheable
import config.{Features, FrontendAppConfig}
import connectors.CacheConnector
import controllers.helpers.TaxReturnHelper
import forms.returns.StartYourReturnFormProvider
import models.Mode.NormalMode
import models.UserAnswers
import models.returns.TaxReturnObligation
import navigation.ReturnsJourneyNavigator
import org.mockito.ArgumentMatchers.{eq => meq}
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito._
import org.mockito.MockitoSugar.mock
import org.mockito.{ArgumentMatchers, Mockito}
import org.scalatest.BeforeAndAfterEach
import pages.returns.StartYourReturnPage
import play.api.data.Form
import play.api.inject.bind
import play.api.libs.json.JsPath
import play.api.mvc.{Call, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import views.html.returns.StartYourReturnView

import java.time.LocalDate
import scala.concurrent.Future

class StartYourReturnControllerSpec extends SpecBase with BeforeAndAfterEach {

  private lazy val startYourReturnRoute = controllers.returns.routes.StartYourReturnController.onPageLoad().url
  private val mockTaxReturnHelper: TaxReturnHelper = mock[TaxReturnHelper]
  private val formProvider: StartYourReturnFormProvider = new StartYourReturnFormProvider()
  private val mockFormProvider = mock[StartYourReturnFormProvider]
  private val mockAuditConnector: AuditConnector = mock[AuditConnector]
  private val mockCacheConnector = mock[CacheConnector]
  private val navigator = mock[ReturnsJourneyNavigator]
  private val isFirst = true
  private val view = mock[StartYourReturnView]

  private val obligation: TaxReturnObligation = TaxReturnObligation(
    fromDate = LocalDate.parse("2022-04-01"),
    toDate = LocalDate.parse("2022-06-30"),
    dueDate = LocalDate.parse("2022-09-30"),
    periodKey = "22AC"
  )

  override protected def beforeEach(): Unit = {
    super.beforeEach
    reset(mockTaxReturnHelper, mockAuditConnector, mockCacheConnector, navigator, view, mockFormProvider)
    when(mockCacheConnector.saveUserAnswerFunc(any)(any)) thenReturn ((_, _) => Future.successful(true))
  }

  private def any[T] = ArgumentMatchers.any[T]()

  "onPageLoad should" - {

    "show the view if user has obligation" in {

      when(mockTaxReturnHelper.nextOpenObligationAndIfFirst(any)(any)).thenReturn(Future.successful(Some((obligation, isFirst))))
      when(view.apply(any, any, any)(any, any)) thenReturn HtmlFormat.raw("bake")
      val form = mock[Form[Boolean]]
      when(mockFormProvider.apply()) thenReturn form
      when(form.fill(any)) thenReturn form

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[TaxReturnHelper].toInstance(mockTaxReturnHelper),
          bind[CacheConnector].toInstance(mockCacheConnector),
          bind[StartYourReturnView] to view)
        .build()

      running(application) {
        val request = FakeRequest(GET, startYourReturnRoute)
        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual "bake"

        verify(mockCacheConnector, atLeastOnce).saveUserAnswerFunc(meq("123"))(any)
        verify(view).apply(any, meq(obligation), meq(true))(any, any)
        verify(form, never()).fill(any)
        verifyNoInteractions(form)
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      when(mockTaxReturnHelper.nextOpenObligationAndIfFirst(any)(any)).thenReturn(Future.successful(Some((obligation, isFirst))))
      when(view.apply(any, any, any)(any, any)) thenReturn HtmlFormat.raw("bake")

      val form = mock[Form[Boolean]]
      when(mockFormProvider.apply()) thenReturn form
      when(form.fill(any)) thenReturn form

      val userAnswers = UserAnswers(userAnswersId).set(StartYourReturnPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[TaxReturnHelper].toInstance(mockTaxReturnHelper),
          bind[CacheConnector].toInstance(mockCacheConnector),
          bind[StartYourReturnFormProvider] to mockFormProvider,
          bind[StartYourReturnView] to view
        ).build()

      running(application) {
        val request = FakeRequest(GET, startYourReturnRoute)
        val result = route(application, request).value

        status(result) mustEqual OK
        verify(view).apply(meq(form), meq(obligation), meq(true))(any, any)
        verify(form).fill(true)
      }
    }

    "redirect to account home when no obligation to start a return" in {

      when(mockTaxReturnHelper.nextOpenObligationAndIfFirst(any)(any)).thenReturn(Future.successful(None))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(
        bind[TaxReturnHelper].toInstance(mockTaxReturnHelper),
        bind[CacheConnector].toInstance(mockCacheConnector),
      ).build()

      running(application) {
        val request = FakeRequest(GET, startYourReturnRoute)
        val result = route(application, request).value

        redirectLocation(result) mustBe Some(controllers.routes.IndexController.onPageLoad.url)
        verifyNoInteractions(cacheConnector)
      }
    }
  }

  "onSubmit should" - {

    "must redirect to the next page when valid data is submitted" in {

      val userAnswers = UserAnswers(userAnswersId)
        .setOrFail(ReturnObligationCacheable, taxReturnOb)
        .setOrFail(JsPath \ "isFirstReturn", true)

      when(config.isFeatureEnabled(Features.creditsForReturnsEnabled)) thenReturn true
      when(navigator.startYourReturnRoute(any, any)) thenReturn Call("GET", "/toast")
      
      val form = mock[Form[Boolean]]
      when(mockFormProvider.apply()) thenReturn form
      when(form.bindFromRequest()(any, any)) thenReturn form

      when(form.fold(any, any)) thenAnswer(i => i.getArgument(1).asInstanceOf[Boolean => Future[Result]].apply(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[CacheConnector].toInstance(mockCacheConnector),
            bind[TaxReturnHelper].toInstance(mockTaxReturnHelper),
            bind[AuditConnector].toInstance(mockAuditConnector),
            bind[FrontendAppConfig].toInstance(config),
            bind[StartYourReturnFormProvider] to mockFormProvider,
            bind[ReturnsJourneyNavigator] to navigator,
            bind[StartYourReturnView] to view
          )
          .build()

      val request = FakeRequest(POST, startYourReturnRoute)
      val result = running(application) {
        route(application, request).value
      }

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual "/toast"
      verify(navigator).startYourReturnRoute(true, true)
    }

    "must audit started event when user answers yes" in {

      Mockito.reset(mockAuditConnector)

      val userAnswers = UserAnswers(userAnswersId)
        .setOrFail(ReturnObligationCacheable, taxReturnOb)
        .setOrFail(JsPath \ "isFirstReturn", true)

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
        .setOrFail(ReturnObligationCacheable, taxReturnOb)
        .setOrFail(JsPath \ "isFirstReturn", true)

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
        .setOrFail(ReturnObligationCacheable, taxReturnOb)
        .setOrFail(JsPath \ "isFirstReturn", true)

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
        contentAsString(result) mustEqual view(boundForm, obligation, isFirst)(request, messages(application)).toString

      }
    }
  }


}
