/*
 * Copyright 2025 HM Revenue & Customs
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
import cacheables.{IsFirstReturnCacheable, ReturnObligationCacheable}
import connectors.CacheConnector
import controllers.actions.JourneyAction
import controllers.helpers.TaxReturnHelper
import forms.returns.StartYourReturnFormProvider
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
import play.api.mvc.{AnyContent, Call}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import queries.{Gettable, Settable}
import views.html.returns.StartYourReturnView

import java.time.LocalDate
import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class StartYourReturnControllerSpec extends PlaySpec with JourneyActionAnswer with BeforeAndAfterEach {

  private val mockTaxReturnHelper: TaxReturnHelper = mock[TaxReturnHelper]
  private val mockFormProvider                     = mock[StartYourReturnFormProvider]
  private val mockCacheConnector                   = mock[CacheConnector]
  private val navigator                            = mock[ReturnsJourneyNavigator]
  private val view                                 = mock[StartYourReturnView]
  private val auditor                              = mock[Auditor]
  private val messagesApi                          = mock[MessagesApi]
  private val journeyAction                        = mock[JourneyAction]
  private val request                              = mock[DataRequest[AnyContent]](Answers.RETURNS_DEEP_STUBS)
  private val form                                 = mock[Form[Boolean]]
  private val saveFunc                             = mock[SaveUserAnswerFunc]
  private val realFrom                             = new StartYourReturnFormProvider().apply()

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
    mockFormProvider,
    stubMessagesControllerComponents(),
    view,
    mockTaxReturnHelper,
    auditor,
    navigator
  )(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(
      journeyAction,
      mockTaxReturnHelper,
      mockCacheConnector,
      navigator,
      view,
      mockFormProvider,
      request
    )

    when(mockCacheConnector.saveUserAnswerFunc(any)(any)) thenReturn ((_, _) => Future.successful(true))
    when(mockFormProvider.apply()) thenReturn form
    when(journeyAction.apply(any)).thenAnswer(byConvertingFunctionArgumentsToAction)
    when(journeyAction.async(any)).thenAnswer(byConvertingFunctionArgumentsToFutureAction)
  }

  "onPageLoad" should {

    "use the journey action" in {
      sut.onPageLoad()
      verify(journeyAction).async(any)
    }

    "show the view if user has obligation" in {
      setUpMocks()
      when(mockTaxReturnHelper.nextOpenObligationAndIfFirst(any)(any)).thenReturn(
        Future.successful(Some((obligation, true)))
      )

      val result = sut.onPageLoad()(request)

      status(result) mustEqual OK
      verify(request.userAnswers).setOrFail(eqTo(ReturnObligationCacheable), eqTo(obligation), any)(any)
      verify(request.userAnswers).setOrFail(eqTo(IsFirstReturnCacheable), eqTo(true), any)(any)
      verify(request.userAnswers).save(eqTo(saveFunc))(any)
      verify(mockCacheConnector).saveUserAnswerFunc(eqTo("test-ppt-ref"))(any)
      verify(view).apply(eqTo(form), eqTo(obligation), eqTo(true))(any, any)
    }

    "redirect to account home when no obligation to start a return" in {
      setUpMocks()
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
      setUpMocks()
      when(mockTaxReturnHelper.nextOpenObligationAndIfFirst(any)(any))
        .thenReturn(Future.successful(Some((obligation, false))))

      val result = sut.onPageLoad()(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(
        controllers.returns.credits.routes.WhatDoYouWantToDoController.onPageLoad.url
      )

      withClue("set and save the obligation") {
        verify(request.userAnswers).setOrFail(eqTo(ReturnObligationCacheable), eqTo(obligation), any)(any)
        verify(request.userAnswers).setOrFail(eqTo(IsFirstReturnCacheable), eqTo(false), any)(any)
        verify(request.userAnswers).save(eqTo(saveFunc))(any)
        verify(mockCacheConnector).saveUserAnswerFunc(eqTo("test-ppt-ref"))(any)
        verifyNoInteractions(view)
      }
    }
  }

  "onSubmit" should {
    "use the journey action" in {
      sut.onSubmit()
      verify(journeyAction).async(any)
    }

    "return view as bad request" when {
      "form binds with error" in {
        setUpMocks()
        val erroredForm = realFrom.withError("some", "form-error")
        when(form.bindFromRequest()(any, any)).thenReturn(erroredForm)

        val result = sut.onSubmit()(request)

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe "correct view"
        verify(view).apply(eqTo(erroredForm), eqTo(obligation), eqTo(true))(any, any)
      }
    }

    "redirect to the next page" when {
      "form binds correctly" in {
        setUpMocks()
        val boundForm = realFrom.fill(true)
        when(form.bindFromRequest()(any, any)).thenReturn(boundForm)

        val result = sut.onSubmit()(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/next-page")

        withClue("should audit doing first return") {
          verify(auditor).returnStarted(any, any)(any, any)
        }
      }
    }

    "error" when {
      object TestException extends Exception("Boom!")
      "obligation is not present" in {
        setUpMocks()
        when(request.userAnswers.getOrFail(eqTo(ReturnObligationCacheable))(any, any)).thenThrow(TestException)

        intercept[TestException.type](await(sut.onSubmit()(request)))
      }
      "isFirstReturn is not present" in {
        setUpMocks()
        when(request.userAnswers.getOrFail(eqTo(IsFirstReturnCacheable))(any, any)).thenThrow(TestException)

        intercept[TestException.type](await(sut.onSubmit()(request)))
      }
      "save user answers fails" in {
        setUpMocks()
        val boundForm = realFrom.fill(true)
        when(form.bindFromRequest()(any, any)).thenReturn(boundForm)

        when(request.userAnswers.save(any)(any)).thenThrow(TestException)

        intercept[TestException.type](await(sut.onSubmit()(request)))
      }
    }
  }

  private def setUpMocks() = {
    when(request.pptReference).thenReturn("test-ppt-ref")
    when(request.userAnswers.fill(any[Gettable[Boolean]], any)(any)).thenReturn(form)
    val ans2 = request.userAnswers // just for setting when for deep stub back to its self.
    when(request.userAnswers.setOrFail(any[Settable[TaxReturnObligation]], any, any)(any)).thenReturn(ans2)
    when(request.userAnswers.setOrFail(any[Settable[Boolean]], any, any)(any)).thenReturn(ans2)
    when(request.userAnswers.setOrFail(any[Settable[Boolean]], any, any)(any)).thenReturn(ans2)
    when(request.userAnswers.getOrFail(eqTo(ReturnObligationCacheable))(any, any)).thenReturn(obligation)
    when(request.userAnswers.getOrFail(eqTo(IsFirstReturnCacheable))(any, any)).thenReturn(true)
    when(request.userAnswers.save(any)(any)).thenReturn(Future.successful(ans2))
    when(mockCacheConnector.saveUserAnswerFunc(any)(any)).thenReturn(saveFunc)
    when(view.apply(any, any, any)(any, any)) thenReturn HtmlFormat.raw("correct view")
    when(navigator.startYourReturn(any)).thenReturn(Call("GET", "/next-page"))
  }
}
