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

package controllers.returns.credits

import base.utils.JourneyActionAnswer
import connectors.CacheConnector
import controllers.BetterMockActionSyntax
import controllers.actions.JourneyAction
import forms.returns.credits.CancelCreditsClaimFormProvider
import models.UserAnswers
import models.UserAnswers.SaveUserAnswerFunc
import models.requests.DataRequest
import models.returns.CreditsAnswer
import navigation.ReturnsJourneyNavigator
import org.mockito.ArgumentMatchersSugar._
import org.mockito.integrations.scalatest.ResetMocksAfterEachTest
import org.mockito.{Answers, MockitoSugar}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import pages.returns.credits._
import play.api.data.Form
import play.api.data.Forms.boolean
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, Call}
import play.api.test.Helpers.{await, contentAsString, defaultAwaitTimeout, redirectLocation, status, stubMessagesControllerComponents}
import play.twirl.api.Html
import uk.gov.hmrc.http.HttpVerbs.GET
import views.html.returns.credits.CancelCreditsClaimView

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future
import scala.util.Try

class CancelCreditsClaimControllerSpec extends PlaySpec
  with JourneyActionAnswer with MockitoSugar with BeforeAndAfterEach with ResetMocksAfterEachTest {

  private val journeyAction = mock[JourneyAction]
  private val messagesApi = mock[MessagesApi]
  private val cacheConnector = mock[CacheConnector]
  private val navigator = mock[ReturnsJourneyNavigator]
  private val formProvider = mock[CancelCreditsClaimFormProvider]
  private val controllerComponents = stubMessagesControllerComponents()
  private val view = mock[CancelCreditsClaimView]
  private val request = mock[DataRequest[AnyContent]](Answers.RETURNS_DEEP_STUBS)
  private val form = mock[Form[Boolean]]
  private val saveFunction = mock[SaveUserAnswerFunc]

  private val sut = new CancelCreditsClaimController(
    messagesApi,
    cacheConnector,
    navigator,
    journeyAction,
    formProvider,
    controllerComponents,
    view
  )(global)

  override def beforeEach(): Unit = {
    super.beforeEach()

    when(journeyAction.apply(any)) thenAnswer byConvertingFunctionArgumentsToAction
    when(journeyAction.async(any)) thenAnswer byConvertingFunctionArgumentsToFutureAction

    when(view.apply(any, any)(any, any)) thenReturn Html("the view")
    when(formProvider.apply()) thenReturn form

    when(navigator.cancelCreditRoute(any, any)) thenReturn Call(GET, "/next-page")
    when(cacheConnector.saveUserAnswerFunc(any)(any)) thenReturn saveFunction

    val x = request.userAnswers
    when(request.userAnswers.remove(any, any)) thenReturn Try(x)
    when(request.userAnswers.change(any, any, any)(any)) thenReturn Future.successful(true)
  }

  "onPageLoad" should {

    "use the journey action" in {
      when(journeyAction.apply(any)) thenReturn mock[Action[AnyContent]]
      sut.onPageLoad("year-key")(request)
      verify(journeyAction).apply(any)
    }

    "return a 200" in {
      val result = sut.onPageLoad("year-key")(request)
      status(result) mustBe OK
      contentAsString(result) mustBe "the view"
    }

    "return a view with correct form" in {
      when(formProvider.apply()).thenReturn(form)
      sut.onPageLoad("year-key")(request)
      verify(view).apply(eqTo("year-key"), eqTo(form))(any, any)
    }

  }

  "onSubmit" should {

    "use the journey action" in {
      when(journeyAction.async(any)) thenReturn mock[Action[AnyContent]]
      sut.onSubmit("year-key")(request)
      verify(journeyAction).async(any)
    }

    "redirect with answer yes" in {
      when(form.bindFromRequest()(any, any)) thenReturn Form("v" -> boolean).fill(true)

      val result = await(sut.onSubmit("year-key").skippingJourneyAction(request))
      verify(request.userAnswers).remove(eqTo(ExportedCreditsPage("year-key")), any)
      verify(request.userAnswers).remove(eqTo(ConvertedCreditsPage("year-key")), any)
      verify(request.userAnswers).change(eqTo(WhatDoYouWantToDoPage), eqTo(false), any)(any)
      verify(navigator).cancelCreditRoute("year-key", true)

      result.header.status mustBe SEE_OTHER
      redirectLocation(Future.successful(result)).value mustBe "/next-page"
    }

    "redirect with answer No" in {
      when(form.bindFromRequest()(any, any)) thenReturn Form("v" -> boolean).fill(false)

      val result = await(sut.onSubmit("year-key").skippingJourneyAction(request))
      verify(request.userAnswers, never).remove(any, any)
      verify(request.userAnswers, never).change(any, any, any)(any)
      verify(navigator).cancelCreditRoute("year-key", false)
      verifyZeroInteractions(saveFunction)

      result.header.status mustBe SEE_OTHER
      redirectLocation(Future.successful(result)).value mustBe "/next-page"
    }

    "handle a bad form" in {
      val formWithErrors = Form("v" -> boolean).withError("error", "error message")
      when(form.bindFromRequest()(any, any)) thenReturn formWithErrors

      val result = await(sut.onSubmit("year-key").skippingJourneyAction(request))
      verify(view).apply(eqTo("year-key"), eqTo(formWithErrors)) (eqTo(request), any)
      verifyZeroInteractions(saveFunction)

      result.header.status mustBe BAD_REQUEST
      contentAsString(Future.successful(result)) mustBe "the view"
    }

  }

  def createUserAnswer = {
    UserAnswers("123")
      .set(ExportedCreditsPage("year-key"), CreditsAnswer.answerWeightWith(10L)).get
      .set(ConvertedCreditsPage("year-key"), CreditsAnswer.answerWeightWith(10L)).get
  }
}
