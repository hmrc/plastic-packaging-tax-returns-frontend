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

import base.utils.JourneyActionAnswer._
import connectors.CacheConnector
import controllers.actions.JourneyAction
import forms.returns.credits.ConvertedCreditsFormProvider
import models.Mode.NormalMode
import models.UserAnswers
import models.requests.DataRequest
import models.returns.CreditsAnswer
import navigation.ReturnsJourneyNavigator
import org.mockito.ArgumentMatchersSugar._
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.MockitoSugar
import org.mockito.captor.ArgCaptor
import org.mockito.integrations.scalatest.ResetMocksAfterEachTest
import org.mockito.stubbing.ReturnsDeepStubs
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import pages.returns.credits.OldConvertedCreditsPage
import play.api.data.Form
import play.api.data.Forms.boolean
import play.api.http.Status
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{AnyContent, Call, RequestHeader}
import play.api.test.Helpers._
import play.twirl.api.Html
import views.html.returns.credits.ConvertedCreditsView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class ConvertedCreditsControllerSpec extends PlaySpec 
  with MockitoSugar with BeforeAndAfterEach with ResetMocksAfterEachTest {

  private val mockCacheConnector: CacheConnector = mock[CacheConnector]
  private val mockNavigator: ReturnsJourneyNavigator = mock[ReturnsJourneyNavigator]
  private val view = mock[ConvertedCreditsView]
  private val formProvider = mock[ConvertedCreditsFormProvider]
  private val initialForm = mock[Form[Boolean]]("initial form")
  private val preparedForm = mock[Form[Boolean]]("prepared form")
  private val journeyAction = mock[JourneyAction]
  private val request = mock[DataRequest[AnyContent]](ReturnsDeepStubs)
  private val messagesApi = mock[MessagesApi]
  private val messages = mock[Messages]
  private val saveUserAnswerFunc = mock[UserAnswers.SaveUserAnswerFunc]

  private val controllerComponents = stubMessagesControllerComponents()

  private val controller: ConvertedCreditsController = new ConvertedCreditsController(
    messagesApi,
    mockCacheConnector,
    mockNavigator,
    journeyAction,
    formProvider,
    controllerComponents,
    view
  )

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    when(formProvider.apply()).thenReturn(initialForm)
    when(initialForm.bindFromRequest()(any, any)).thenReturn(preparedForm)
    when(view.apply(any, any)(any, any)).thenReturn(Html("correct view"))

    when(mockCacheConnector.saveUserAnswerFunc(any)(any)) thenReturn saveUserAnswerFunc
    when(mockNavigator.convertedCreditsYesNo(any, any)).thenReturn(Call("GET", "/next/page"))
    
    when(journeyAction.apply(any)) thenAnswer byConvertingFunctionArgumentsToAction
    when(journeyAction.async(any)) thenAnswer byConvertingFunctionArgumentsToFutureAction
    when(messagesApi.preferred(any[RequestHeader])) thenReturn messages
  }
  
  "onPageLoad" must {
    
    "use the journey action" in {
      controller.onPageLoad(NormalMode)
      verify(journeyAction).apply(any)
    }
    
    "fill the form with user's previous answer" in {
      controller.onPageLoad(NormalMode) (request)
      val function = ArgCaptor[CreditsAnswer => Option[Boolean]]
      verify(request.userAnswers).genericFill(eqTo(OldConvertedCreditsPage), eqTo(initialForm), function) (any)
      
      withClue("using correct function") {
        val creditsAnswer = mock[CreditsAnswer]
        function.value(creditsAnswer)
        verify(creditsAnswer).yesNo
      }
    }
    
    "render the page" in {
      when(request.userAnswers.genericFill(any, any[Form[Boolean]], any) (any)) thenReturn preparedForm
      controller.onPageLoad(NormalMode) (request)
      verify(messagesApi).preferred(request)
      verify(view).apply(preparedForm, NormalMode)(request, messages)
    }
    
    "200 ok the client" in {
      val futureResult = controller.onPageLoad(NormalMode) (request)
      status(futureResult) mustBe Status.OK
      contentAsString(futureResult) mustBe "correct view"
    }
  }

  "onSubmit" must {

    "remember the user's answers" in {
      // Invokes the "form is good" side of the fold() call
      when(initialForm.bindFromRequest()(any, any)) thenReturn Form("v" -> boolean).fill(true)
      await(controller.onSubmit(NormalMode) (request))
      
      // TODO tweak CreditsAnswer.changeYesNoTo so we can test for it here
      verify(request.userAnswers).changeWithFunc(eqTo(OldConvertedCreditsPage), any, eqTo(saveUserAnswerFunc)) (any, any)
    }

    "redirect to the next page" in {
      // Invokes the "form is good" side of the fold() call
      when(initialForm.bindFromRequest()(any, any)) thenReturn Form("v" -> boolean).fill(true)
      when(request.userAnswers.changeWithFunc(any, any, any) (any, any)) thenReturn Future.unit
      
      val result = await { controller.onSubmit(NormalMode) (request) }
      verify(mockNavigator).convertedCreditsYesNo(eqTo(NormalMode), eqTo(true))
      
      result.header.status mustBe Status.SEE_OTHER
      redirectLocation(Future.successful(result)) mustBe Some("/next/page")
    }
    
    "display any errors" in {
      // Invokes the "form is bad" side of the fold() call
      val formWithErrors = Form("v" -> boolean).withError("key", "message")
      when(initialForm.bindFromRequest()(any, any)) thenReturn formWithErrors

      val result = await { controller.onSubmit(NormalMode)(request) }
      verify(view).apply(eqTo(formWithErrors), eqTo(NormalMode)) (eqTo(request), eqTo(messages))
      verifyNoInteractions(request.userAnswers)

      result.header.status mustBe Status.BAD_REQUEST
      contentAsString(Future.successful(result)) mustBe "correct view"
    }

  }
}

