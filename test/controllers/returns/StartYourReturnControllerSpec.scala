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

import audit.Auditor
import base.utils.JourneyActionAnswer
import cacheables.ReturnObligationCacheable
import connectors.CacheConnector
import controllers.actions.JourneyAction
import controllers.helpers.TaxReturnHelper
import forms.returns.StartYourReturnFormProvider
import models.UserAnswers
import models.UserAnswers.SaveUserAnswerFunc
import models.requests.DataRequest
import models.returns.TaxReturnObligation
import navigation.ReturnsJourneyNavigator
import org.mockito.Answers
import org.mockito.ArgumentMatchersSugar.{any, eqTo}
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.MockitoSugar.{mock, reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.libs.json.JsPath
import play.api.mvc.AnyContent
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import queries.{Gettable, Settable}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import views.html.returns.StartYourReturnView

import java.time.LocalDate
import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class StartYourReturnControllerSpec extends PlaySpec with JourneyActionAnswer with BeforeAndAfterEach {

  private lazy val startYourReturnRoute = controllers.returns.routes.StartYourReturnController.onPageLoad().url // TODO fix
  private val mockTaxReturnHelper: TaxReturnHelper = mock[TaxReturnHelper]
  private val formProvider: StartYourReturnFormProvider = new StartYourReturnFormProvider()
  private val mockFormProvider = mock[StartYourReturnFormProvider]
  private val mockAuditConnector: AuditConnector = mock[AuditConnector]
  private val mockCacheConnector = mock[CacheConnector]
  private val navigator = mock[ReturnsJourneyNavigator]
  private val view = mock[StartYourReturnView]
  private val auditor = mock[Auditor]
  private val messagesApi = mock[MessagesApi]
  private val journeyAction = mock[JourneyAction]
  private val request = mock[DataRequest[AnyContent]](Answers.RETURNS_DEEP_STUBS)
  private val form = mock[Form[Boolean]]
  private val ans = mock[UserAnswers]
  private val saveFunc = mock[SaveUserAnswerFunc]


  private val obligation: TaxReturnObligation = TaxReturnObligation(
    fromDate = LocalDate.parse("2022-04-01"),
    toDate = LocalDate.parse("2022-06-30"),
    dueDate = LocalDate.parse("2022-09-30"),
    periodKey = "22AC"
  )

  private val sut = new StartYourReturnController(
    messagesApi,
    mockCacheConnector,
    journeyAction: JourneyAction,
    formProvider,
    stubMessagesControllerComponents(),
    view,
    mockTaxReturnHelper,
    auditor,
    navigator
  )(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(
      mockTaxReturnHelper,
      mockAuditConnector,
      mockCacheConnector,
      navigator,
      view,
      mockFormProvider,
      request,
      ans
    )

    when(mockCacheConnector.saveUserAnswerFunc(any)(any)) thenReturn ((_, _) => Future.successful(true))
    when(mockFormProvider.apply()) thenReturn form
    when(journeyAction.apply(any)).thenAnswer(byConvertingFunctionArgumentsToAction)
    when(journeyAction.async(any)).thenAnswer(byConvertingFunctionArgumentsToFutureAction)
  }

  "onPageLoad" should {

    "use the journey action" in {
      sut.onPageLoad
      verify(journeyAction).async(any)
    }

    "show the view if user has obligation" in {

      setUpMocks

      val result = sut.onPageLoad()(request)

      status(result) mustEqual OK
      verify(request.userAnswers).setOrFail(eqTo(ReturnObligationCacheable), eqTo(obligation), any)(any)
      verify(ans).setOrFail(eqTo((JsPath \ "isFirstReturn")), eqTo(true))(any)
      verify(ans).save(eqTo(saveFunc))(any)
      verify(mockCacheConnector).saveUserAnswerFunc(eqTo("123"))(any)
      verify(view).apply(eqTo(form), eqTo(obligation), eqTo(true))(any, any)
      }

    "redirect to account home when no obligation to start a return" in {

      when(mockTaxReturnHelper.nextOpenObligationAndIfFirst(any)(any)).thenReturn(Future.successful(None))

      val result = sut.onPageLoad()(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.IndexController.onPageLoad.url)

      withClue("not set and save the obligation") {
        verifyNoInteractions(mockCacheConnector)
        verifyNoInteractions(request.userAnswers)
      }
    }

    "redirect to WhatDoYouWantToDOPage when not first return" in {
      setUpMocks

      when(mockTaxReturnHelper.nextOpenObligationAndIfFirst(any)(any))
        .thenReturn(Future.successful(Some((obligation, false))))

      val result = sut.onPageLoad()(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.returns.credits.routes.WhatDoYouWantToDoController.onPageLoad.url)

      withClue("set and save the obligation") {
        verify(request.userAnswers).setOrFail(eqTo(ReturnObligationCacheable), eqTo(obligation), any)(any)
        verify(ans).setOrFail(eqTo((JsPath \ "isFirstReturn")), eqTo(false))(any)
        verify(ans).save(eqTo(saveFunc))(any)
        verify(mockCacheConnector).saveUserAnswerFunc(eqTo("123"))(any)
        verifyNoInteractions(view)
      }
    }
  }

  private def setUpMocks = {
    when(request.pptReference).thenReturn("123")
    when(mockTaxReturnHelper.nextOpenObligationAndIfFirst(any)(any)).thenReturn(Future.successful(Some((obligation, true))))
    when(view.apply(any, any, any)(any, any)) thenReturn HtmlFormat.raw("bake")
    when(request.userAnswers.fill(any[Gettable[Boolean]], any)(any)).thenReturn(form)
    when(request.userAnswers.setOrFail(any[Settable[TaxReturnObligation]], any, any)(any)).thenReturn(ans)
    when(ans.setOrFail(any[JsPath], any)(any)).thenReturn(ans)
    when(ans.save(any)(any)).thenReturn(Future.successful(ans))
    when(mockCacheConnector.saveUserAnswerFunc(any)(any)).thenReturn(saveFunc)
  }

  "onSubmit" should {


//    "must redirect to the next page when valid data is submitted" in {
//
//      val userAnswers = UserAnswers(userAnswersId)
//        .setOrFail(ReturnObligationCacheable, taxReturnOb)
//        .setOrFail(JsPath \ "isFirstReturn", true)
//
//      when(config.isFeatureEnabled(Features.creditsForReturnsEnabled)) thenReturn true
//      when(navigator.startYourReturn) thenReturn Call("GET", "/toast")
//
//      val form = mock[Form[Boolean]]
//      when(mockFormProvider.apply()) thenReturn form
//      when(form.bindFromRequest()(any, any)) thenReturn form
//
//      when(form.fold(any, any)) thenAnswer (i => i.getArgument(1).asInstanceOf[Boolean => Future[Result]].apply(true))
//
//      val application =
//        applicationBuilder(userAnswers = Some(userAnswers))
//          .overrides(
//            bind[CacheConnector].toInstance(mockCacheConnector),
//            bind[TaxReturnHelper].toInstance(mockTaxReturnHelper),
//            bind[AuditConnector].toInstance(mockAuditConnector),
//            bind[FrontendAppConfig].toInstance(config),
//            bind[StartYourReturnFormProvider] to mockFormProvider,
//            bind[ReturnsJourneyNavigator] to navigator,
//            bind[StartYourReturnView] to view
//          )
//          .build()
//
//      val request = FakeRequest(POST, startYourReturnRoute)
//      val result = running(application) {
//        route(application, request).value
//      }
//
//      status(result) mustEqual SEE_OTHER
//      redirectLocation(result).value mustEqual "/toast"
//      verify(navigator).startYourReturn
//    }
//
//    "must audit started event when user answers yes" in {
//
//      Mockito.reset(mockAuditConnector)
//
//      val userAnswers = UserAnswers(userAnswersId)
//        .setOrFail(ReturnObligationCacheable, taxReturnOb)
//        .setOrFail(JsPath \ "isFirstReturn", true)
//
//      val application =
//        applicationBuilder(userAnswers = Some(userAnswers))
//          .overrides(
//            bind[CacheConnector].toInstance(mockCacheConnector),
//            bind[TaxReturnHelper].toInstance(mockTaxReturnHelper),
//            bind[AuditConnector].toInstance(mockAuditConnector)
//          )
//          .build()
//
//      running(application) {
//        val request =
//          FakeRequest(POST, startYourReturnRoute)
//            .withFormUrlEncodedBody(("value", "true"))
//
//        val result = route(application, request).value
//
//        status(result) mustEqual SEE_OTHER
//
//        verify(mockAuditConnector, times(1)).
//          sendExplicitAudit(eqTo(ReturnStarted.eventType), any[ReturnStarted])(any, any, any)
//
//      }
//    }
//
//    "must not audit started event when user answers no" in {
//
//      Mockito.reset(mockAuditConnector)
//
//      val userAnswers = UserAnswers(userAnswersId)
//        .setOrFail(ReturnObligationCacheable, taxReturnOb)
//        .setOrFail(JsPath \ "isFirstReturn", true)
//
//      val application =
//        applicationBuilder(userAnswers = Some(userAnswers))
//          .overrides(
//            bind[CacheConnector].toInstance(mockCacheConnector),
//            bind[TaxReturnHelper].toInstance(mockTaxReturnHelper),
//            bind[AuditConnector].toInstance(mockAuditConnector)
//          )
//          .build()
//
//      running(application) {
//        val request =
//          FakeRequest(POST, startYourReturnRoute)
//            .withFormUrlEncodedBody(("value", "false"))
//
//        val result = route(application, request).value
//
//        status(result) mustEqual SEE_OTHER
//
//        verify(mockAuditConnector, times(0)).
//          sendExplicitAudit(eqTo(ReturnStarted.eventType), any[ReturnStarted])(any, any, any)
//
//      }
//    }

//    "must return a Bad Request and errors when invalid data is submitted" in {
//
//      when(mockTaxReturnHelper.nextOpenObligationAndIfFirst(any)(any)).thenReturn(Future.successful(Some((obligation, isFirst))))
//
//      val userAnswers = UserAnswers(userAnswersId)
//        .setOrFail(ReturnObligationCacheable, taxReturnOb)
//        .setOrFail(JsPath \ "isFirstReturn", true)
//
//      val application = applicationBuilder(userAnswers = Some(userAnswers)).overrides(
//        bind[TaxReturnHelper].toInstance(mockTaxReturnHelper)
//      ).build()
//
//      running(application) {
//        val request =
//          FakeRequest(POST, startYourReturnRoute)
//            .withFormUrlEncodedBody(("value", ""))
//
//        val boundForm = formProvider().bind(Map("value" -> ""))
//
//        val view = application.injector.instanceOf[StartYourReturnView]
//
//        val result = route(application, request).value
//
//        status(result) mustEqual BAD_REQUEST
//        contentAsString(result) mustEqual view(boundForm, obligation, isFirst)(request, messages(application)).toString
//
//      }
//    }
  }
}
