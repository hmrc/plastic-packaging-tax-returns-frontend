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

package controllers

import base.SpecBase
import connectors.CacheConnector
import controllers.actions.JourneyAction
import controllers.actions.JourneyAction.{RequestAsyncFunction, RequestFunction}
import controllers.changeGroupLead.FeatureGuard
import controllers.helpers.InjectableNonExportedAmountHelper
import controllers.returns.AnotherBusinessExportWeightController
import forms.AnotherBusinessExportWeightFormProvider
import models.Mode.NormalMode
import models.UserAnswers
import models.UserAnswers.SaveUserAnswerFunc
import models.requests.DataRequest
import navigation.{FakeNavigator, Navigator, ReturnsJourneyNavigator}
import org.mockito.{Answers, Mockito}
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.ArgumentMatchers.{eq => meq}
import org.mockito.Mockito.reset
import org.mockito.MockitoSugar.{mock, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import pages.returns.{AnotherBusinessExportWeightPage, DirectlyExportedComponentsPage, ExportedPlasticPackagingWeightPage, ImportedPlasticPackagingPage, ImportedPlasticPackagingWeightPage, ManufacturedPlasticPackagingPage, ManufacturedPlasticPackagingWeightPage, PlasticExportedByAnotherBusinessPage}
import play.api.data.Form
import play.api.data.Forms.{longNumber, text}
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.mvc.{Action, AnyContent, Call, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import queries.Gettable
import repositories.SessionRepository
import views.html.returns.AnotherBusinessExportWeightView

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future
import scala.util.Try

class AnotherBusinessExportWeightControllerSpec extends PlaySpec with BeforeAndAfterEach{

  private val mockMessagesApi: MessagesApi = mock[MessagesApi]
  private val controllerComponents = stubMessagesControllerComponents()
  private val mockView = mock[AnotherBusinessExportWeightView]
  private val mockFormProvider = mock[AnotherBusinessExportWeightFormProvider]
  private val mockCache = mock[CacheConnector]
  private val journeyAction = mock[JourneyAction]
  private val dataRequest = mock[DataRequest[AnyContent]](Answers.RETURNS_DEEP_STUBS)
  private val form = mock[Form[Long]]
  private val mockNavigator = mock[ReturnsJourneyNavigator]
  private val mockNonExportedAmountHelper = mock[InjectableNonExportedAmountHelper]

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
    when(dataRequest.userAnswers.fill(any[AnotherBusinessExportWeightPage.type], any)(any)).thenReturn(form)
    when(journeyAction.apply(any)) thenAnswer byConvertingFunctionArgumentsToAction
    when(journeyAction.async(any)) thenAnswer byConvertingFunctionArgumentsToFutureAction
    when(mockNavigator.exportedByAnotherBusinessWeightRoute(any, any)).thenReturn(Call("GET", "/foo"))
    when(mockNonExportedAmountHelper.totalPlastic(any)).thenReturn(Some(200L))
  }

  //TODO add helper for defs below shared by lots of specs
  def byConvertingFunctionArgumentsToAction: (RequestFunction) => Action[AnyContent] = (function: RequestFunction) =>
    when(mock[Action[AnyContent]].apply(any))
      .thenAnswer((request: DataRequest[AnyContent]) =>
        Future.successful(function(request)))
      .getMock[Action[AnyContent]]

  def byConvertingFunctionArgumentsToFutureAction: (RequestAsyncFunction) => Action[AnyContent] = (function: RequestAsyncFunction) =>
    when(mock[Action[AnyContent]].apply(any))
      .thenAnswer((request: DataRequest[AnyContent]) => function(request))
      .getMock[Action[AnyContent]]

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
      verify(dataRequest.userAnswers).fill(meq(AnotherBusinessExportWeightPage), meq(form))(any)
    }

   "redirect to index controller when total plastic cannot be calculated" in {
     reset(mockNonExportedAmountHelper)
     when(mockNonExportedAmountHelper.totalPlastic(any)).thenReturn(None)

     val result = sut.onPageLoad(NormalMode)(dataRequest)

     status(result) mustEqual SEE_OTHER
     redirectLocation(result).value mustEqual controllers.routes.IndexController.onPageLoad.url
     verify(mockNonExportedAmountHelper).totalPlastic(dataRequest.userAnswers)
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
      reset(mockNonExportedAmountHelper)
      when(mockNonExportedAmountHelper.totalPlastic(any)).thenReturn(None)

      val result = sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.IndexController.onPageLoad.url
      verify(mockNonExportedAmountHelper).totalPlastic(dataRequest.userAnswers)
    }

    "redirect to exportedByAnotherBusinessWeightRoute" in {
      val saveFunc:SaveUserAnswerFunc = (_, bool) => Future.successful(bool)
      val boundForm = Form("value" -> longNumber()).fill(20L)
      when(form.bindFromRequest()(any, any)).thenReturn(boundForm)
      val answers = dataRequest.userAnswers
      when(dataRequest.userAnswers.setOrFail(any, any, any)(any)).thenReturn(answers)
      when(dataRequest.userAnswers.save(any)(any)).thenReturn(Future.successful(answers))
      when(mockCache.saveUserAnswerFunc(any)(any)).thenReturn(saveFunc)

      val result = sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual "/foo"
      verify(dataRequest.userAnswers).setOrFail(meq(AnotherBusinessExportWeightPage), meq(20L), any)(any)
      verify(dataRequest.userAnswers).save(meq(saveFunc))(any)
      verify(mockCache).saveUserAnswerFunc(meq(dataRequest.pptReference))(any)
    }
  }
}
