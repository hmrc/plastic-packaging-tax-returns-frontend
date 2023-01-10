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

import connectors.CacheConnector
import controllers.BetterMockActionSyntax
import controllers.actions.JourneyAction
import controllers.actions.JourneyAction.{RequestAsyncFunction, RequestFunction}
import controllers.helpers.InjectableNonExportedAmountHelper
import forms.returns.PlasticExportedByAnotherBusinessFormProvider
import models.Mode.{CheckMode, NormalMode}
import models.UserAnswers
import models.requests.DataRequest
import navigation.ReturnsJourneyNavigator
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.ArgumentMatchers.{eq => meq}
import org.mockito.MockitoSugar.{reset, verify, when}
import org.mockito.{Answers, ArgumentCaptor, ArgumentMatchers}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import pages.returns._
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, RequestHeader}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import queries.Gettable
import views.html.returns.PlasticExportedByAnotherBusinessView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PlasticExportedByAnotherBusinessControllerSpec
  extends PlaySpec
    with MockitoSugar
    with BeforeAndAfterEach {

  def onwardRoute = Call("GET", "/foo")

  private val formProvider = mock[PlasticExportedByAnotherBusinessFormProvider]
  private val bindForm = new PlasticExportedByAnotherBusinessFormProvider()()
  private val messagesApi = mock[MessagesApi]
  private val cacheConnector = mock[CacheConnector]
  private val returnsNavigator = mock[ReturnsJourneyNavigator]
  private val journeyAction = mock[JourneyAction]
  private val view = mock[PlasticExportedByAnotherBusinessView]
  private val dataRequest = mock[DataRequest[AnyContent]](Answers.RETURNS_DEEP_STUBS)
  private val mockNonExportedAmountHelper = mock[InjectableNonExportedAmountHelper]

  private val sut = new PlasticExportedByAnotherBusinessController(
    messagesApi,
    cacheConnector,
    journeyAction,
    formProvider,
    returnsNavigator,
    mockNonExportedAmountHelper,
    stubMessagesControllerComponents(),
    view
  )

  def byConvertingFunctionArgumentsToAction: (RequestFunction) => Action[AnyContent] = (function: RequestFunction) =>
    when(mock[Action[AnyContent]].apply(any))
      .thenAnswer((request: DataRequest[AnyContent]) => Future.successful(function(request)))
      .getMock[Action[AnyContent]]

  def byConvertingFunctionArgumentsToFutureAction: (RequestAsyncFunction) => Action[AnyContent] = (function: RequestAsyncFunction) =>
    when(mock[Action[AnyContent]].apply(any))
      .thenAnswer((request: DataRequest[AnyContent]) => function(request))
      .getMock[Action[AnyContent]]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(dataRequest, journeyAction, messagesApi, view, mockNonExportedAmountHelper)

    when(formProvider.apply()).thenReturn(bindForm)
    when(journeyAction.apply(any)) thenAnswer byConvertingFunctionArgumentsToAction
    when(journeyAction.async(any)) thenAnswer byConvertingFunctionArgumentsToFutureAction
    when(messagesApi.preferred(any[RequestHeader])) thenReturn mock[Messages]
    when(view.apply(any, any, any)(any,any)).thenReturn(HtmlFormat.empty)
    when(mockNonExportedAmountHelper.totalPlastic(any)).thenReturn(Some(50L))
  }

  "onPageLoad" should {

    "use the journey action" in {
      sut.onPageLoad(NormalMode)
      verify(journeyAction).apply(any)
    }

    "return OK" in {

      val result = sut.onPageLoad(NormalMode)(dataRequest)

      status(result) mustBe OK
    }

    "return a view with no answer" in {
      when(dataRequest.userAnswers.fill(any[Gettable[Boolean]], any)(any)).thenReturn(bindForm)
      await(sut.onPageLoad(NormalMode)(dataRequest))

      verify(view).apply(meq(bindForm), meq(NormalMode), meq(50L))(any, any)
    }

    "prepopulate the form" in {
      await(sut.onPageLoad(NormalMode).skippingJourneyAction(dataRequest))
      verify(dataRequest.userAnswers).fill(meq(PlasticExportedByAnotherBusinessPage),meq(bindForm))(any)
    }

    "view should show total plastic packaging amount" in {

      await(sut.onPageLoad(NormalMode)(dataRequest))

      verify(view).apply(any, any, ArgumentMatchers.eq(50L))(any, any)
    }


    "redirect to the account page if cannot calculate total plastic" in {
      reset(mockNonExportedAmountHelper)
      when(mockNonExportedAmountHelper.totalPlastic(any)).thenReturn(None)
     val result = sut.onPageLoad(NormalMode)(dataRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.IndexController.onPageLoad.url
      verify(mockNonExportedAmountHelper).totalPlastic(dataRequest.userAnswers)
    }
  }

  "onSubmit" should {
    val form = mock[Form[Boolean]]

    "redirect to AnotherBusinessExportWeight page" in {
      when(form.bindFromRequest()(any,any)).thenReturn(bindForm.fill(true))
      when(formProvider.apply()).thenReturn(form)
      when(cacheConnector.saveUserAnswerFunc(any)(any)).thenReturn((_, _) => Future.successful(true))
      when(returnsNavigator.exportedByAnotherBusinessRoute(any, any)) thenReturn Call("", "some-url")
      val answer = dataRequest.userAnswers
      when(dataRequest.userAnswers.setOrFail(any, any,any)(any)).thenReturn(answer)
      when(dataRequest.userAnswers.save(any)(any)).thenReturn(Future.successful(answer))

      val result = sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest)

      status(result) mustBe SEE_OTHER
      verify(returnsNavigator).exportedByAnotherBusinessRoute(dataRequest.userAnswers, NormalMode)
    }

    "should save answer to the cache" in {
      when(form.bindFromRequest()(any,any)).thenReturn(bindForm.bind(Map("value" -> "true")))
      when(formProvider.apply()).thenReturn(form)
      when(cacheConnector.saveUserAnswerFunc(any)(any)).thenReturn((_, _) => Future.successful(true))
      when(dataRequest.pptReference).thenReturn("123")

      await(sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest))

      verify(cacheConnector).saveUserAnswerFunc(ArgumentMatchers.eq("123"))(any)
    }

    "should return an error with bad request" in {
      when(formProvider.apply()).thenReturn(form)
      when(form.bindFromRequest()(any,any)).thenReturn(bindForm.withError("error", "error"))

      val result = sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest)

      status(result) mustBe BAD_REQUEST
    }

    "should return a view with error and total plastic" in {
      val errorForm = bindForm.withError("error", "error")
      when(formProvider.apply()).thenReturn(form)
      when(form.bindFromRequest()(any,any)).thenReturn(errorForm)

      await(sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest))

      verify(view).apply(
        ArgumentMatchers.eq(errorForm),
        ArgumentMatchers.eq(NormalMode),
        ArgumentMatchers.eq(50L)
      )(any, any)
    }

    "should redirect to account page when total plastic cannot be calculated" in {
      reset(mockNonExportedAmountHelper)
      when(mockNonExportedAmountHelper.totalPlastic(any)).thenReturn(None)
      val errorForm = bindForm.withError("error", "error")
      when(formProvider.apply()).thenReturn(form)
      when(form.bindFromRequest()(any,any)).thenReturn(errorForm)

      val result = sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.IndexController.onPageLoad.url)
      verify(mockNonExportedAmountHelper).totalPlastic(dataRequest.userAnswers)
    }
  }
}
