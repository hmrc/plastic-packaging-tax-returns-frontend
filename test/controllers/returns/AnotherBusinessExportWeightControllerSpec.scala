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

import base.utils.JourneyActionAnswer
import connectors.CacheConnector
import controllers.BetterMockActionSyntax
import controllers.actions.JourneyAction
import controllers.helpers.NonExportedAmountHelper
import forms.returns.AnotherBusinessExportWeightFormProvider
import models.Mode.{CheckMode, NormalMode}
import models.UserAnswers.SaveUserAnswerFunc
import models.requests.DataRequest
import navigation.ReturnsJourneyNavigator
import org.mockito.Answers
import org.mockito.ArgumentMatchers.{eq => meq}
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar.reset
import org.mockito.MockitoSugar.{mock, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import pages.returns.AnotherBusinessExportedWeightPage
import play.api.data.Form
import play.api.data.Forms.longNumber
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import views.html.returns.AnotherBusinessExportWeightView

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future
import scala.util.Try

class AnotherBusinessExportWeightControllerSpec extends PlaySpec with JourneyActionAnswer with BeforeAndAfterEach{

  private val mockMessagesApi: MessagesApi = mock[MessagesApi]
  private val controllerComponents = stubMessagesControllerComponents()
  private val mockView = mock[AnotherBusinessExportWeightView]
  private val mockFormProvider = mock[AnotherBusinessExportWeightFormProvider]
  private val mockCache = mock[CacheConnector]
  private val journeyAction = mock[JourneyAction]
  private val dataRequest = mock[DataRequest[AnyContent]](Answers.RETURNS_DEEP_STUBS)
  private val form = mock[Form[Long]]
  private val mockNavigator = mock[ReturnsJourneyNavigator]
  private val mockNonExportedAmountHelper = mock[NonExportedAmountHelper]

  val sut = new AnotherBusinessExportWeightController(
    mockMessagesApi,
    mockCache,
    mockNavigator,
    journeyAction,
    mockFormProvider,
    controllerComponents,
    mockView,
    mockNonExportedAmountHelper
  )(global)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockMessagesApi,
      journeyAction,
      mockCache,
      mockNavigator,
      mockFormProvider,
      mockView,
      form,
      dataRequest,
      mockNonExportedAmountHelper
    )

    when(mockView.apply(any, any, any)(any, any)).thenReturn(Html("correct view"))
    when(mockFormProvider.apply()).thenReturn(form)
    when(dataRequest.userAnswers.fill(any[AnotherBusinessExportedWeightPage.type], any)(any)).thenReturn(form)
    when(journeyAction.apply(any)) thenAnswer byConvertingFunctionArgumentsToAction
    when(journeyAction.async(any)) thenAnswer byConvertingFunctionArgumentsToFutureAction
    when(mockNavigator.exportedByAnotherBusinessWeightRoute(any, any)).thenReturn(Call("GET", "/foo"))
    when(mockNonExportedAmountHelper.totalPlasticAdditions(any)).thenReturn(Some(200L))
  }

  "onPageLoad" should {
    "invoke the journey action" in {
      Try(await(sut.onPageLoad(NormalMode)(FakeRequest())))
      verify(journeyAction).apply(any)
    }

    "return OK and correct view" in {
      val result = sut.onPageLoad(NormalMode).skippingJourneyAction(dataRequest)
     status(result) mustBe OK
      contentAsString(result) mustBe "correct view"
      verify(mockView).apply(meq(200L), meq(form), meq(NormalMode))(any, any)
    }

    "create the form" in {
      await(sut.onPageLoad(NormalMode).skippingJourneyAction(dataRequest))
      verify(mockFormProvider).apply()
    }

    "prepopulate the form" in {
      await(sut.onPageLoad(NormalMode).skippingJourneyAction(dataRequest))
      verify(dataRequest.userAnswers).fill(meq(AnotherBusinessExportedWeightPage), meq(form))(any)
    }

   "redirect to index controller when total plastic cannot be calculated" in {
     when(mockNonExportedAmountHelper.totalPlasticAdditions(any)).thenReturn(None)

     val result = sut.onPageLoad(NormalMode)(dataRequest)

     status(result) mustEqual SEE_OTHER
     redirectLocation(result).value mustEqual controllers.routes.IndexController.onPageLoad.url
     verify(mockNonExportedAmountHelper).totalPlasticAdditions(dataRequest.userAnswers)
   }
  }

  "onSubmit" should {
    "invoke the journey action" in {
      when(journeyAction.async(any)) thenReturn mock[Action[AnyContent]]
      Try(await(sut.onSubmit(NormalMode)(FakeRequest())))
      verify(journeyAction).async(any)
    }

    "return a BAD REQUEST (400) when the form errors" in {
      val errorForm = Form("value" -> longNumber()).withError("key", "error")
      when(form.bindFromRequest()(any, any)).thenReturn(errorForm)

      val result = sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest)

      status(result) mustEqual BAD_REQUEST
      contentAsString(result) mustBe "correct view"
      verify(mockView).apply(meq(200L), meq(errorForm), meq(NormalMode))(any, any)
    }

    "redirect to index controller when total plastic cannot be calculated" in {
      val errorForm = Form("value" -> longNumber()).withError("key", "error")
      when(form.bindFromRequest()(any, any)).thenReturn(errorForm)
      when(mockNonExportedAmountHelper.totalPlasticAdditions(any)).thenReturn(None)

      val result = sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.IndexController.onPageLoad.url
      verify(mockNonExportedAmountHelper).totalPlasticAdditions(dataRequest.userAnswers)
    }

    "set userAnswer with clean up false on CheckMode" in {
      when(form.bindFromRequest()(any, any)).thenReturn(Form("value" -> longNumber()).fill(20L))

      await(sut.onSubmit(CheckMode).skippingJourneyAction(dataRequest))

      verify(dataRequest.userAnswers).setOrFail(meq(AnotherBusinessExportedWeightPage), meq(20L), meq(false))(any)
    }

    "set userAnswer with clean up true in NormalMode" in {
      when(form.bindFromRequest()(any, any)).thenReturn(Form("value" -> longNumber()).fill(20L))

      await(sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest))

      verify(dataRequest.userAnswers).setOrFail(meq(AnotherBusinessExportedWeightPage), meq(20L), meq(true))(any)
    }

    "save userAnswer to cache" in {
      val saveFunc:SaveUserAnswerFunc = (_, bool) => Future.successful(bool)
      when(form.bindFromRequest()(any, any)).thenReturn(Form("value" -> longNumber()).fill(20L))
      val answers = dataRequest.userAnswers
      when(dataRequest.userAnswers.setOrFail(any, any, any)(any)).thenReturn(answers)
      when(dataRequest.userAnswers.save(any)(any)).thenReturn(Future.successful(answers))
      when(mockCache.saveUserAnswerFunc(any)(any)).thenReturn(saveFunc)

      await(sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest))

      verify(dataRequest.userAnswers).save(meq(saveFunc))(any)
      verify(mockCache).saveUserAnswerFunc(meq(dataRequest.pptReference))(any)
    }

    "redirect" in {
      when(form.bindFromRequest()(any, any)).thenReturn(Form("value" -> longNumber()).fill(20L))
      val answers = dataRequest.userAnswers
      when(dataRequest.userAnswers.setOrFail(any, any, any)(any)).thenReturn(answers)
      when(dataRequest.userAnswers.save(any)(any)).thenReturn(Future.successful(answers))
      when(mockCache.saveUserAnswerFunc(any)(any)).thenReturn((_, bool) => Future.successful(bool))

      val result = sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual "/foo"
    }
  }
}
