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

package controllers.amends

import cacheables.AmendObligationCacheable
import connectors.CacheConnector
import controllers.BetterMockActionSyntax
import controllers.actions.JourneyAction
import controllers.actions.JourneyAction.{RequestAsyncFunction, RequestFunction}
import forms.amends.CancelAmendFormProvider
import models.UserAnswers
import models.requests.DataRequest
import models.returns.TaxReturnObligation
import org.mockito.ArgumentMatchers.{eq => meq}
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.{Answers, MockitoSugar}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, RequestHeader}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import queries.Gettable
import views.html.amends.{AmendAlreadyCancelledView, CancelAmendView}

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CancelAmendControllerSpec extends PlaySpec with MockitoSugar with BeforeAndAfterEach {

  val userAnswer: UserAnswers = mock[UserAnswers]
  val aTaxObligation: TaxReturnObligation = TaxReturnObligation(
    LocalDate.now(),
    LocalDate.now().plusWeeks(12),
    LocalDate.now().plusWeeks(16),
    "PK1"
  )


  val form = mock[Form[Boolean]]
  private val dataRequest = mock[DataRequest[AnyContent]](Answers.RETURNS_DEEP_STUBS)
  private val messagesApi = mock[MessagesApi]
  private val cacheConnector = mock[CacheConnector]
  private val journeyAction = mock[JourneyAction]
  private val cancelAmendView = mock[CancelAmendView]
  private val amendAlreadyCancelledView = mock[AmendAlreadyCancelledView]
  private val formProvider = mock[CancelAmendFormProvider]
  private val messages = mock[Messages]

  private val sut = new CancelAmendController(
    messagesApi,
    cacheConnector,
    journeyAction,
    formProvider,
    stubMessagesControllerComponents(),
    cancelAmendView,
    amendAlreadyCancelledView
  )
  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(
      cacheConnector,
      messagesApi,
      journeyAction,
      cancelAmendView,
      amendAlreadyCancelledView,
      dataRequest,
      userAnswer
    )

    when(dataRequest.userAnswers) thenReturn userAnswer
    when(journeyAction.apply(any)).thenAnswer(byConvertingFunctionArgumentsToAction)
    when(journeyAction.async(any)).thenAnswer(byConvertingFunctionArgumentsToFutureAction)
    when(messagesApi.preferred(any[RequestHeader])).thenReturn(messages)
  }

  def byConvertingFunctionArgumentsToAction: (RequestFunction) => Action[AnyContent] = (function: RequestFunction) =>
    when(mock[Action[AnyContent]].apply(any))
      .thenAnswer((request: DataRequest[AnyContent]) => Future.successful(function(request)))
      .getMock[Action[AnyContent]]

  def byConvertingFunctionArgumentsToFutureAction: (RequestAsyncFunction) => Action[AnyContent] = (function: RequestAsyncFunction) =>
    when(mock[Action[AnyContent]].apply(any))
      .thenAnswer((request: DataRequest[AnyContent]) => function(request))
      .getMock[Action[AnyContent]]

  "onPageLoad" should {

    "invoke the journey action" in {
      when(journeyAction.apply(any)).thenReturn(mock[Action[AnyContent]])
      sut.onPageLoad(FakeRequest())
      verify(journeyAction).apply(any)
    }

    "return ok" in {
      when(userAnswer.get(any[Gettable[Any]])(any)).thenReturn(Some(aTaxObligation))
      when(cancelAmendView.apply(any, any)(any,any)).thenReturn(HtmlFormat.empty)

      val result = sut.onPageLoad.skippingJourneyAction(dataRequest)

      status(result) mustBe OK
    }

    "display the cancelAmendView page" in {
      when(userAnswer.get(any[Gettable[Any]])(any)).thenReturn(Some(aTaxObligation))
      when(formProvider.apply()).thenReturn(form)
      when(cancelAmendView.apply(any, any)(any,any)).thenReturn(HtmlFormat.empty)

      val result = sut.onPageLoad.skippingJourneyAction(dataRequest)

      status(result) mustBe OK
      verify(cancelAmendView).apply(meq(form), meq(aTaxObligation))(any,any)
    }

    "display the amendAlreadyCancelledView page" in {
      when(amendAlreadyCancelledView.apply()(any,any)).thenReturn(HtmlFormat.empty)
      when(userAnswer.get(any[Gettable[Any]])(any)).thenReturn(None)

      val result = sut.onPageLoad.skippingJourneyAction(dataRequest)

      status(result) mustBe OK
      verify(amendAlreadyCancelledView).apply()(any,any)
    }
  }

  "onSubmit" should {
    "invoke the journey action" in {
      when(journeyAction.async(any)).thenReturn(mock[Action[AnyContent]])
      sut.onSubmit(FakeRequest())
      verify(journeyAction).async(any)
    }

    "save a empty userAnswer to cache" in {
      var saveUserAnswerToCache: Option[UserAnswers] = None

      when(form.bindFromRequest()(any, any)).thenReturn(new CancelAmendFormProvider()().fillAndValidate(true))
      when(formProvider.apply()).thenReturn(form)
      when(dataRequest.userAnswers).thenReturn(UserAnswers("234").set(AmendObligationCacheable, aTaxObligation).get)
      when(cacheConnector.saveUserAnswerFunc(any)(any)).thenReturn((a: UserAnswers, b: Boolean) => {
        saveUserAnswerToCache = Some(a)
        Future.successful(true)
      })
      when(amendAlreadyCancelledView.apply()(any, any)).thenReturn(HtmlFormat.empty)

      await(sut.onSubmit(dataRequest))

      saveUserAnswerToCache.get.data.value mustBe empty
      saveUserAnswerToCache.get.id mustBe "234"
    }

    "return 200" in {
      setUpMocks

      val result = sut.onSubmit(dataRequest)

      status(result) mustEqual OK
    }

    "return the amendAlreadyCancelledView" in {
      setUpMocks

      val result = sut.onSubmit(dataRequest)

      status(result) mustEqual OK
      verify(amendAlreadyCancelledView).apply()(any,any)
    }

    "redirect to check your answer page if not cancelled" in {
      when(userAnswer.get(any[Gettable[Any]])(any)).thenReturn(Some(aTaxObligation))
      when(form.bindFromRequest()(any, any)).thenReturn(new CancelAmendFormProvider()().fillAndValidate(false))
      when(formProvider.apply()).thenReturn(form)

      val result = sut.onSubmit(dataRequest)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.amends.routes.CheckYourAnswersController.onPageLoad.url
    }

    "return bad request if error on form" in {
      when(userAnswer.get(any[Gettable[Any]])(any)).thenReturn(Some(aTaxObligation))
      when(formProvider.apply()).thenReturn(form)
      val errorForm = new CancelAmendFormProvider()().withError("error", "error message")
      when(form.bindFromRequest()(any, any)).thenReturn(errorForm)
      when(cancelAmendView.apply(any,any)(any,any)).thenReturn(HtmlFormat.empty)

      val result = sut.onSubmit(dataRequest)

      status(result) mustEqual BAD_REQUEST
      verify(cancelAmendView).apply(meq(errorForm), meq(aTaxObligation))(any,any)
    }

    "throw an exception if obligation not found" in {
      when(dataRequest.userAnswers).thenReturn(UserAnswers("234"))

      intercept[IllegalStateException] {
        await(sut.onSubmit(dataRequest))
      }
    }
  }

  private def setUpMocks = {
    when(userAnswer.get(any[Gettable[Any]])(any)).thenReturn(Some(aTaxObligation))
    when(userAnswer.removeAll()).thenReturn(userAnswer)
    when(userAnswer.save(any)(any)).thenReturn(Future.successful(userAnswer))
    when(dataRequest.request.pptReference) thenReturn ("123")
    when(amendAlreadyCancelledView.apply()(any, any)).thenReturn(HtmlFormat.empty)
    when(form.bindFromRequest()(any, any)).thenReturn(new CancelAmendFormProvider()().fillAndValidate(true))
    when(formProvider.apply()).thenReturn(form)
  }
}
