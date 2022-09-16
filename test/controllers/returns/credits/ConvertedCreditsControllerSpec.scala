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

package controllers.returns.credits

import connectors.CacheConnector
import controllers.returns.credits.JourneyAction.{RequestAsyncFunction, RequestFunction}
import forms.returns.credits.ConvertedCreditsFormProvider
import models.Mode.NormalMode
import models.UserAnswers
import models.requests.DataRequest
import models.returns.CreditsAnswer
import navigation.ReturnsJourneyNavigator
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.{eq => meq}
import org.mockito.Mockito.{reset, verify, verifyNoInteractions, when}
import org.mockito.MockitoSugar.{when => scalaWhen}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import pages.returns.credits.{ConvertedCreditsPage, ExportedCreditsPage}
import play.api.data.Form
import play.api.http.Status
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc._
import play.api.test.Helpers._
import play.twirl.api.Html
import views.html.returns.credits.ConvertedCreditsView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class ConvertedCreditsControllerSpec extends PlaySpec with MockitoSugar with BeforeAndAfterEach {

  private val mockCacheConnector: CacheConnector = mock[CacheConnector]
  private val mockNavigator: ReturnsJourneyNavigator = mock[ReturnsJourneyNavigator]
  private val view = mock[ConvertedCreditsView]
  private val formProvider = mock[ConvertedCreditsFormProvider]
  private val form = mock[Form[CreditsAnswer]]
  private val userAnswers = mock[UserAnswers]
  private val journeyAction = mock[JourneyAction]
  private val dataRequest = mock[DataRequest[AnyContent]]
  private val messagesApi = mock[MessagesApi]
  private val messages = mock[Messages]

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

  private def any[T] = ArgumentMatchers.any[T]()

  def byConvertingFunctionArgumentsToAction: (RequestFunction) => Action[AnyContent] = (function: RequestFunction) =>
    scalaWhen(mock[Action[AnyContent]].apply(any))
      .thenAnswer((request: DataRequest[AnyContent]) => Future.successful(function(request)))
      .getMock[Action[AnyContent]]

  def byConvertingFunctionArgumentsToFutureAction: (RequestAsyncFunction) => Action[AnyContent] = (function: RequestAsyncFunction) =>
    scalaWhen(mock[Action[AnyContent]].apply(any))
      .thenAnswer((request: DataRequest[AnyContent]) => function(request))
      .getMock[Action[AnyContent]]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(
      messagesApi,
      mockCacheConnector,
      mockNavigator, 
      view,
      journeyAction,
      formProvider,
      form,
      userAnswers,
      dataRequest,
      messages
    )
    when(userAnswers.fill[CreditsAnswer](any, any)(any)) thenReturn form
    when(userAnswers.setOrFail(any, any, any)(any)) thenReturn userAnswers
    when(userAnswers.save(any)(any)) thenReturn Future.successful(userAnswers)

    when(formProvider.apply()).thenReturn(form)
    when(form.bindFromRequest()(any, any)).thenReturn(form)
    when(view.apply(any, any)(any, any)).thenReturn(Html("correct view"))

    when(mockCacheConnector.saveUserAnswerFunc(any)(any)) thenReturn ((_, _) => Future.successful(false))
    when(mockNavigator.convertedCreditsRoute(any, any)).thenReturn(Call("GET", "/next/page"))

    when(dataRequest.userAnswers) thenReturn userAnswers
    
    scalaWhen(journeyAction.apply(any)) thenAnswer byConvertingFunctionArgumentsToAction
    scalaWhen(journeyAction.async(any)) thenAnswer byConvertingFunctionArgumentsToFutureAction
    when(messagesApi.preferred(any[RequestHeader])) thenReturn messages
  }
  
  "onPageLoad" must {
    
    "use the journey action" in {
      controller.onPageLoad(NormalMode)
      verify(journeyAction).apply(any)
    }
    
    "fill the form with user's previous answer" in {
      controller.onPageLoad(NormalMode) (dataRequest)
      verify(userAnswers).fill[CreditsAnswer](ConvertedCreditsPage, form)
    }
    
    "render the page" in {
      controller.onPageLoad(NormalMode) (dataRequest)
      verify(messagesApi).preferred(dataRequest)
      verify(view).apply(form, NormalMode)(dataRequest, messages)
    }
    
    "200 ok the client" in {
      val futureResult = controller.onPageLoad(NormalMode) (dataRequest)
      status(futureResult) mustBe Status.OK
      contentAsString(futureResult) mustBe "correct view"
    }
  }

  "onSubmit" must {

    "remember the user's answers" in {
      
      // Invokes the "form is good" side of the fold() call
      when(form.fold(any, any)).thenAnswer(i => i.getArgument[CreditsAnswer => Future[Result]](1).apply(CreditsAnswer(true, Some(20))))
      await(controller.onSubmit(NormalMode) (dataRequest))
      verify(userAnswers).setOrFail(meq(ConvertedCreditsPage), meq(CreditsAnswer(true, Some(20))), meq(false))(any)
      verify(userAnswers).save(any)(any)
    }

    "redirect to the next page" in {
      // Invokes the "form is good" side of the fold() call
      when(form.fold(any, any)).thenAnswer(i => i.getArgument[CreditsAnswer => Future[Result]](1).apply(CreditsAnswer(false, None)))
      val result = controller.onSubmit(NormalMode) (dataRequest)
      status(result) mustEqual SEE_OTHER
      redirectLocation(result) mustBe Some("/next/page")
      verify(mockNavigator).convertedCreditsRoute(meq(NormalMode), any)
    }
    
    "pass the two credit claim answers to the navigator" in {
      when(userAnswers.getOrFail(meq(ExportedCreditsPage))(any)) thenReturn CreditsAnswer(true, Some(11))
      when(userAnswers.getOrFail(meq(ConvertedCreditsPage))(any)) thenReturn CreditsAnswer(true, Some(22))

      // Invokes the "form is good" side of the fold() call
      when(form.fold(any, any)).thenAnswer(i => i.getArgument[CreditsAnswer => Future[Result]](1).apply(CreditsAnswer(false, None)))
      await(controller.onSubmit(NormalMode) (dataRequest))

      val expectedCreditsClaim = ClaimedCredits(CreditsAnswer(true, Some(11)), CreditsAnswer(true, Some(22)))
      verify(mockNavigator).convertedCreditsRoute(meq(NormalMode), meq(expectedCreditsClaim))
    }

    "display any errors" in {
      // Invokes the "form is bad" side of the fold() call
      when(form.fold(any, any)).thenAnswer(i => i.getArgument[Form[CreditsAnswer] => Future[Result]](0).apply(form))
      val result = controller.onSubmit(NormalMode) (dataRequest)

      status(result) mustEqual BAD_REQUEST
      contentAsString(result) mustBe "correct view"
      
      verify(view).apply(meq(form), meq(NormalMode))(any, any)
      verifyNoInteractions(userAnswers)
    }

  }
}

