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
import forms.returns.credits.ExportedCreditsFormProvider
import models.Mode.NormalMode
import models.requests.DataRequest
import models.returns.CreditsAnswer
import navigation.ReturnsJourneyNavigator
import org.mockito.ArgumentMatchersSugar._
import org.mockito.MockitoSugar
import org.mockito.captor.ArgCaptor
import org.mockito.integrations.scalatest.ResetMocksAfterEachTest
import org.mockito.stubbing.ReturnsDeepStubs
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import pages.returns.credits.OldExportedCreditsPage
import play.api.data.Form
import play.api.data.Forms.boolean
import play.api.http.Status
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{AnyContent, Call, RequestHeader}
import play.api.test.Helpers._
import play.twirl.api.Html
import views.html.returns.credits.ExportedCreditsView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ExportedCreditsControllerSpec extends PlaySpec 
  with MockitoSugar with BeforeAndAfterEach with ResetMocksAfterEachTest {

  private val controllerComponents = stubMessagesControllerComponents()
  
  private val messagesApi = mock[MessagesApi]
  private val mockCacheConnector = mock[CacheConnector]
  private val mockNavigator = mock[ReturnsJourneyNavigator]
  private val view = mock[ExportedCreditsView]
  private val formProvider = mock[ExportedCreditsFormProvider]
  private val mockJourneyAction = mock[JourneyAction]

  private val preparedForm = mock[Form[Boolean]]("prepared form")
  private val initialForm = mock[Form[Boolean]]("initial form")
  private val messages = mock[Messages]
  private val request = mock[DataRequest[AnyContent]](ReturnsDeepStubs)

  val sut: ExportedCreditsController = new ExportedCreditsController(
    messagesApi,
    mockCacheConnector,
    mockNavigator,
    mockJourneyAction, 
    formProvider,
    controllerComponents,
    view)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    
    when(mockJourneyAction.apply(any)) thenAnswer byConvertingFunctionArgumentsToAction
    when(mockJourneyAction.async(any)) thenAnswer byConvertingFunctionArgumentsToFutureAction
    
    when(view.apply(any, any)(any, any)) thenReturn Html("correct view")
    when(messagesApi.preferred(any[RequestHeader])) thenReturn messages

    when(formProvider.apply()) thenReturn initialForm
    when(request.userAnswers.genericFill(any, any[Form[Boolean]], any) (any)) thenReturn preparedForm
  }

  "onPageLoad" must {

    "display the page" in {
      val result = sut.onPageLoad(NormalMode)(request)
      await(result)

      verify(view).apply(preparedForm, NormalMode)(request, messages)

      val func = ArgCaptor[CreditsAnswer => Option[Boolean]]
      verify(request.userAnswers).genericFill(eqTo(OldExportedCreditsPage), eqTo(initialForm), func)(any)

      status(result) mustEqual Status.OK
      contentAsString(result) mustBe "correct view"

      withClue("fills the form value with correct function") {
        val creditsAnswer = mock[CreditsAnswer]
        func.value(creditsAnswer)
        verify(creditsAnswer).yesNo
      }
    }
    
  }
  
  "onSubmit" must {

    "must redirect to the next page using the navigator" in { 
      // Follow the "form is good" side
      when(initialForm.bindFromRequest()(any, any)) thenReturn Form("v" -> boolean).fill(true)
      
      when(request.userAnswers.changeWithFunc(any, any, any) (any, any)) thenReturn Future.unit
      when(mockNavigator.exportedCreditsYesNo(any, any)) thenReturn Call("me", "mr")

      val result = await { sut.onSubmit(NormalMode)(request) }
      verify(mockCacheConnector).saveUserAnswerFunc(any)(any)
      verify(request.userAnswers).changeWithFunc(any, any, any) (any, any)
      verify(mockNavigator).exportedCreditsYesNo(any, any)
      
      result.header.status mustEqual SEE_OTHER
      redirectLocation(Future.successful(result)).value mustBe "mr"
    }

    "handle answer that fails form validation" in {
      // Follow the "form is bad" side
      val formWithError = Form("v" -> boolean).withError("key", "message")
      when(initialForm.bindFromRequest()(any, any)) thenReturn formWithError

      val result = await { sut.onSubmit(NormalMode)(request) }
      verify(view).apply(formWithError, NormalMode) (request, messages)

      result.header.status mustEqual BAD_REQUEST
      contentAsString(Future.successful(result)) mustBe "correct view"
    }
    
  }
}
