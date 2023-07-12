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

package controllers.changeGroupLead

import connectors.CacheConnector
import controllers.BetterMockActionSyntax
import controllers.actions.JourneyAction
import controllers.actions.JourneyAction.{RequestAsyncFunction, RequestFunction}
import forms.changeGroupLead.MainContactJobTitleFormProvider
import models.Mode.NormalMode
import models.requests.DataRequest
import navigation.ChangeGroupLeadNavigator
import org.mockito.Answers
import org.mockito.ArgumentMatchers.{eq => meq}
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar.reset
import org.mockito.MockitoSugar.{mock, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import pages.changeGroupLead.MainContactJobTitlePage
import play.api.data.Form
import play.api.data.Forms.text
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import queries.{Gettable, Settable}
import views.html.changeGroupLead.MainContactJobTitleView

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future
import scala.util.Try


class MainContactJobTitleControllerSpec extends PlaySpec with BeforeAndAfterEach {

  private val mockMessagesApi: MessagesApi = mock[MessagesApi]
  private val controllerComponents = stubMessagesControllerComponents()
  private val mockView = mock[MainContactJobTitleView]
  private val mockFormProvider = mock[MainContactJobTitleFormProvider]
  private val mockCache = mock[CacheConnector]
  private val journeyAction = mock[JourneyAction]
  private val dataRequest = mock[DataRequest[AnyContent]](Answers.RETURNS_DEEP_STUBS)
  private val form = mock[Form[String]]
  private val mockNavigator =  mock[ChangeGroupLeadNavigator]

  val sut = new MainContactJobTitleController(
    mockMessagesApi,
    mockCache,
    mockNavigator,
    journeyAction,
    mockFormProvider,
    controllerComponents,
    mockView
  )(global)

  object TestException extends Exception("test")

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockMessagesApi,
      journeyAction,
      mockView,
      mockFormProvider,
      mockCache,
      form,
      mockNavigator,
      dataRequest
    )

    when(mockView.apply(any, any, any)(any, any)).thenReturn(Html("correct view"))
    when(dataRequest.userAnswers.fill(any[Gettable[String]], any)(any)) thenReturn form
    when(dataRequest.userAnswers.getOrFail(any[Gettable[String]])(any, any)) thenReturn "job-title"
    when(journeyAction.apply(any)) thenAnswer byConvertingFunctionArgumentsToAction
    when(journeyAction.async(any)) thenAnswer byConvertingFunctionArgumentsToFutureAction
    when(mockNavigator.mainContactJobTitle(any)).thenReturn(Call("GET", "/test-foo"))
  }

  def byConvertingFunctionArgumentsToAction: (RequestFunction) => Action[AnyContent] = (function: RequestFunction) =>
    when(mock[Action[AnyContent]].apply(any))
      .thenAnswer((request: DataRequest[AnyContent]) =>
        Future.successful(function(request)))
      .getMock[Action[AnyContent]]

  def byConvertingFunctionArgumentsToFutureAction: (RequestAsyncFunction) => Action[AnyContent] = (function: RequestAsyncFunction) =>
    when(mock[Action[AnyContent]].apply(any))
      .thenAnswer((request: DataRequest[AnyContent]) => function(request))
      .getMock[Action[AnyContent]]

  "onPageLoad" must {
    "invoke the journey action" in {
      when(journeyAction.apply(any)) thenReturn mock[Action[AnyContent]]
      Try(await(sut.onPageLoad(NormalMode)(FakeRequest())))
      verify(journeyAction).apply(any)
    }

    "return a view" in {
      val result = sut.onPageLoad(NormalMode).skippingJourneyAction(dataRequest)
      status(result) mustBe OK
      contentAsString(result) mustBe "correct view"
      verify(mockView).apply(meq(form), meq("job-title"), meq(NormalMode))(any, any)
    }

    "get any previous user answer" in {
      when(mockFormProvider.apply()) thenReturn form
      await(sut.onPageLoad(NormalMode).skippingJourneyAction(dataRequest))
      verify(dataRequest.userAnswers).fill(meq(MainContactJobTitlePage), meq(form))(any)
    }

    "create the form" in {
      await(sut.onPageLoad(NormalMode).skippingJourneyAction(dataRequest))
      verify(mockFormProvider).apply()
    }
  }

  "onSubmit" must {
    "invoke the journey action" in {
      when(journeyAction.async(any)) thenReturn mock[Action[AnyContent]]
      Try(await(sut.onSubmit(NormalMode)(FakeRequest())))
      verify(journeyAction).async(any)
    }

    "bind the form and error" in {
      when(mockFormProvider.apply()) thenReturn form
      val errorForm = Form("value" -> text()).withError("key", "error")
      when(form.bindFromRequest()(any, any)).thenReturn(errorForm)

      val result = sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "correct view"
      verify(mockView).apply(meq(errorForm), meq("job-title"),  meq(NormalMode))(any, any)
      verify(mockFormProvider).apply()
      verify(form).bindFromRequest()(meq(dataRequest),any)
    }

    "bind the form and bind the value" in {
      when(mockFormProvider.apply()) thenReturn form
      val boundForm = Form("value" -> text()).fill("test-name")
      when(form.bindFromRequest()(any, any)).thenReturn(boundForm)
      val userAnswers = dataRequest.userAnswers
      when(dataRequest.userAnswers.setOrFail(any[Settable[String]], any, any)(any)).thenReturn(userAnswers)
      when(mockCache.saveUserAnswerFunc(any)(any)).thenReturn({case _ => Future.successful(true)})
      when(userAnswers.save(any)(any)).thenReturn(Future.successful(userAnswers))

      val result = sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some("/test-foo")

      verify(mockFormProvider).apply()
      verify(form).bindFromRequest()(meq(dataRequest),any)
      withClue("the selected member must be cached"){
        verify(dataRequest.userAnswers).setOrFail(MainContactJobTitlePage, "test-name")
        verify(dataRequest.userAnswers).save(mockCache.saveUserAnswerFunc(dataRequest.pptReference)(dataRequest.headerCarrier))(global)
      }
    }

    "error" when {
      "the user answers setOrFail fails" in {
        when(mockFormProvider.apply()) thenReturn form
        val boundForm = Form("value" -> text()).fill("test-name")
        when(form.bindFromRequest()(any, any)).thenReturn(boundForm)
        when(dataRequest.userAnswers.setOrFail(any[Settable[String]], any, any)(any)).thenThrow(TestException)

        intercept[TestException.type](await(sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest)))
      }

      "the cache save fails" in {
        when(mockFormProvider.apply()) thenReturn form
        val boundForm = Form("value" -> text()).fill("test-name")
        when(form.bindFromRequest()(any, any)).thenReturn(boundForm)
        val userAnswers = dataRequest.userAnswers
        when(dataRequest.userAnswers.setOrFail(any[Settable[String]], any, any)(any)).thenReturn(userAnswers)
        when(mockCache.saveUserAnswerFunc(any)(any)).thenReturn({case _ => Future.successful(true)})
        when(userAnswers.save(any)(any)).thenReturn(Future.failed(TestException))

        intercept[TestException.type](await(sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest)))
      }
    }

  }
}
