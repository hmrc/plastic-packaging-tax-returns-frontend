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
import models.requests.DataRequest
import navigation.{Navigator, ReturnsJourneyNavigator}
import org.mockito.{Answers, ArgumentMatchers}
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.data.Form
import play.api.data.Forms.boolean
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import play.api.test.Helpers.{defaultAwaitTimeout, status, stubMessagesControllerComponents}
import play.twirl.api.{Html, HtmlFormat}
import queries.Gettable
import views.html.returns.credits.CancelCreditsClaimView

import scala.concurrent.ExecutionContext.global

class CancelCreditsClaimControllerSpec extends PlaySpec with JourneyActionAnswer with MockitoSugar with BeforeAndAfterEach{

  private val journeyAction = mock[JourneyAction]
  private val messagesApi = mock[MessagesApi]
  private val cacheConnector = mock[CacheConnector]
  private val navigator = mock[ReturnsJourneyNavigator]
  private val formProvider = mock[CancelCreditsClaimFormProvider]
  private val controllerComponents = stubMessagesControllerComponents()
  private val view = mock[CancelCreditsClaimView]
  private val dataRequest = mock[DataRequest[AnyContent]](Answers.RETURNS_DEEP_STUBS)
  private val form = Form("value" -> boolean)

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
    reset(
      journeyAction,
      dataRequest,
      view
    )
    when(journeyAction.apply(any)).thenAnswer(byConvertingFunctionArgumentsToAction)
    when(journeyAction.async(any)).thenAnswer(byConvertingFunctionArgumentsToFutureAction)
    when(dataRequest.userAnswers.fill(any[Gettable[Boolean]], any)(any)).thenReturn(form)
    when(view.apply(any)(any, any)).thenReturn(HtmlFormat.empty)
  }

  "onPageLoad" should {
    "use the journey action" in {
      when(journeyAction.apply(any)) thenReturn mock[Action[AnyContent]]
      sut.onPageLoad(dataRequest)
      verify(journeyAction).apply(any)
    }

    "return a 200" in {
      val result = sut.onPageLoad(dataRequest)
      status(result) mustBe OK
    }

    "return a view with correct form" in {
      sut.onPageLoad(dataRequest)
      verify(view).apply(ArgumentMatchers.eq(form))(any, any)
    }
  }

  "onSubmit" should {
    "use the journey action" in {
      when(journeyAction.async(any)) thenReturn mock[Action[AnyContent]]
      sut.onSubmit(dataRequest)
      verify(journeyAction).async(any)
    }

    "redirect with answer yes" in {
      val boundForm = mock[Form[Boolean]]
      when(formProvider.apply()).thenReturn(boundForm)
      when(boundForm.bindFromRequest()(any, any)).thenReturn(form.fill(true))

      val result = sut.onSubmit.skippingJourneyAction(dataRequest)
      status(result) mustBe SEE_OTHER
      withClue("call the navigator with Yes parameter"){
        verify(navigator).cancelCreditRoute(ArgumentMatchers.eq(true))
      }
    }
  }
}
