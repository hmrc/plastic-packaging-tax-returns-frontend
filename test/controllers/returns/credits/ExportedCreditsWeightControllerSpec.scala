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
import forms.returns.credits.ExportedCreditsWeightFormProvider
import models.Mode.NormalMode
import models.UserAnswers
import models.UserAnswers.SaveUserAnswerFunc
import models.requests.DataRequest
import models.returns.CreditsAnswer
import navigation.ReturnsJourneyNavigator
import org.mockito.Answers
import org.mockito.ArgumentMatchers.{eq => meq}
import org.mockito.ArgumentMatchersSugar.{any, eqTo}
import org.mockito.MockitoSugar.{reset, verify, when}
import org.mockito.captor.ArgCaptor
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.PlaySpec
import pages.returns.credits.{ExportedCreditsWeightPage, OldExportedCreditsPage}
import play.api.data.Form
import play.api.data.Forms.{ignored, longNumber}
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, await, contentAsString, defaultAwaitTimeout, status, stubMessagesControllerComponents}
import play.twirl.api.{Html, HtmlFormat}
import queries.{Gettable, Settable}
import views.html.returns.credits.ExportedCreditsWeightView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ExportedCreditsWeightControllerSpec extends PlaySpec with JourneyActionAnswer with BeforeAndAfterEach{

  private val form = mock[Form[Long]]
  private val dataRequest    = mock[DataRequest[AnyContent]](Answers.RETURNS_DEEP_STUBS)
  private val saveFunction = mock[SaveUserAnswerFunc]
  private val userAnswers = mock[UserAnswers]
  private val messagesApi = mock[MessagesApi]
  private val journeyAction = mock[JourneyAction]
  private val cacheConnector = mock[CacheConnector]
  private val view = mock[ExportedCreditsWeightView]
  private val formProvider = mock[ExportedCreditsWeightFormProvider]
  private val navigator = mock[ReturnsJourneyNavigator]

  private val sut = new ExportedCreditsWeightController(
    messagesApi,
    journeyAction,
    cacheConnector,
    stubMessagesControllerComponents(),
    view,
    formProvider,
    navigator
  )

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(journeyAction, view, dataRequest, navigator, form, cacheConnector, saveFunction, userAnswers)

    when(formProvider.apply()).thenReturn(form)
    when(view.apply(any, any)(any, any)).thenReturn(HtmlFormat.empty)
    when(journeyAction.apply(any)).thenAnswer(byConvertingFunctionArgumentsToAction)
    when(journeyAction.async(any)).thenAnswer(byConvertingFunctionArgumentsToFutureAction)

  }
  "onPageLoad" should {

    "use the journey action" in {
      sut.onPageLoad(NormalMode)
      verify(journeyAction).apply(any)
    }

    "return 200" in {
      val result = sut.onPageLoad(NormalMode)(dataRequest)
      status(result) mustBe OK
    }

    "fill the weight in the form" in {
      await(sut.onPageLoad(NormalMode)(dataRequest))
      val func = ArgCaptor[CreditsAnswer => Option[Long]]
      verify(dataRequest.userAnswers).genericFill(eqTo(OldExportedCreditsPage), eqTo(form), func) (any)

      withClue("gets weight or None for displaying") {
        val creditsAnswer = mock[CreditsAnswer]
        func.value(creditsAnswer)
        verify(creditsAnswer).weightForForm
      }
    }

    "return a view" in {
      val boundForm = Form("value" -> longNumber).fill(10L)
      when(dataRequest.userAnswers.genericFill(any, any[Form[Long]], any) (any)) thenReturn boundForm
      await(sut.onPageLoad(NormalMode)(dataRequest))
      verify(view).apply(eqTo(boundForm), eqTo(NormalMode)) (any, any)
    }
  }

  "onSubmit" should {
    "invoke the journey action" in {
      when(journeyAction.async(any)) thenReturn mock[Action[AnyContent]]
      sut.onSubmit(NormalMode)(FakeRequest())
      verify(journeyAction).async(any)
    }

    "get the weight from the form" in {
      when(form.bindFromRequest() (any, any)) thenReturn Form("value" -> longNumber).fill(10L)
      when(navigator.exportedCreditsWeight(NormalMode)).thenReturn(Call(GET, "foo"))
      await(sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest))
      verify(dataRequest.userAnswers).setOrFail(eqTo(OldExportedCreditsPage), 
        eqTo(CreditsAnswer(true, Some(10))), any) (any)
    }

    "save the weight" in {
      setupMocks

      await(sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest))

      withClue("save weight to user Answer") {
        verify(userAnswers).save(meq(saveFunction))(any)
      }

      withClue("save weight to cache") {
        verify(cacheConnector).saveUserAnswerFunc(meq("123"))(any)
      }
    }

    "redirect" in {
      setupMocks

      val result = sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest)

      status(result) mustBe SEE_OTHER
      verify(navigator).exportedCreditsWeight(NormalMode)
    }

    "return an error" when {
      "error on form" in {
        when(view.apply(any, any)(any, any)).thenReturn(Html("correct view"))
        val firmWithError = Form("value" -> ignored(1L)).withError("key", "error")
        when(form.bindFromRequest()(any, any)).thenReturn(firmWithError)

        val result = sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest)

        status(result) mustBe BAD_REQUEST

        withClue("pass an error to the view") {
          contentAsString(result) mustBe "correct view"
          verify(view).apply(meq(firmWithError), meq(NormalMode))(any,any)
        }
      }
    }

  }

  private def setupMocks = {
    when(form.bindFromRequest()(any, any)).thenReturn(Form("value" -> longNumber).fill(10L))
    when(dataRequest.userAnswers.setOrFail(any[Settable[Long]], any, any)(any)).thenReturn(userAnswers)
    when(userAnswers.save(any)(any)).thenReturn(Future.successful(mock[UserAnswers]))
    when(cacheConnector.saveUserAnswerFunc(any)(any)).thenReturn(saveFunction)
    when(dataRequest.pptReference).thenReturn("123")
    when(navigator.exportedCreditsWeight(NormalMode)).thenReturn(Call(GET, "foo"))
  }
}
