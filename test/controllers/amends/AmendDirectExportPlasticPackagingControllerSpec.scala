/*
 * Copyright 2025 HM Revenue & Customs
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

import base.utils.JourneyActionAnswer
import connectors.CacheConnector
import controllers.BetterMockActionSyntax
import controllers.actions.JourneyAction
import forms.amends.AmendDirectExportPlasticPackagingFormProvider
import models.UserAnswers
import models.UserAnswers.SaveUserAnswerFunc
import models.requests.DataRequest
import models.returns._
import org.mockito.Answers
import org.mockito.ArgumentMatchers.{eq => meq}
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import pages.amends.AmendDirectExportPlasticPackagingPage
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import queries.{Gettable, Settable}
import support.AmendExportedData
import views.html.amends.AmendDirectExportPlasticPackagingView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

class AmendDirectExportPlasticPackagingControllerSpec
    extends PlaySpec
    with JourneyActionAnswer
    with MockitoSugar
    with AmendExportedData
    with BeforeAndAfterEach {

  private val dataRequest    = mock[DataRequest[AnyContent]](Answers.RETURNS_DEEP_STUBS)
  private val form           = mock[Form[Long]]
  private val saveFunction   = mock[SaveUserAnswerFunc]
  private val formProvider   = mock[AmendDirectExportPlasticPackagingFormProvider]
  private val messagesApi    = mock[MessagesApi]
  private val cacheConnector = mock[CacheConnector]
  private val journeyAction  = mock[JourneyAction]
  private val view           = mock[AmendDirectExportPlasticPackagingView]

  private val sut = new AmendDirectExportPlasticPackagingController(
    messagesApi,
    cacheConnector,
    journeyAction,
    formProvider,
    stubMessagesControllerComponents(),
    view
  )

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(messagesApi, cacheConnector, journeyAction, view, dataRequest, form, saveFunction)

    when(view.apply(any)(any, any)).thenReturn(Html("correct view"))
    when(journeyAction.apply(any)).thenAnswer(byConvertingFunctionArgumentsToAction)
    when(journeyAction.async(any)).thenAnswer(byConvertingFunctionArgumentsToFutureAction)
  }

  "onPageLoad" should {
    "use the journey action" in {
      sut.onPageLoad
      verify(journeyAction).apply(any)
    }

    "return 200" in {
      when(dataRequest.userAnswers.fill(any[Gettable[Long]], any)(any)).thenReturn(mock[Form[Long]])
      when(dataRequest.userAnswers.get(any[Gettable[TaxReturnObligation]])(any)).thenReturn(Some(taxReturnOb))

      val result = sut.onPageLoad(dataRequest)

      status(result) mustEqual OK
    }

    "display a view" in {
      when(dataRequest.userAnswers.fill(any[Gettable[Long]], any)(any)).thenReturn(form)
      when(dataRequest.userAnswers.get(any[Gettable[TaxReturnObligation]])(any)).thenReturn(Some(taxReturnOb))

      val result = sut.onPageLoad(dataRequest)

      status(result) mustEqual OK
      verify(view).apply(meq(form))(any, any)
    }

    "redirect if already submitted" in {
      when(dataRequest.userAnswers.fill(any[Gettable[Long]], any)(any)).thenReturn(mock[Form[Long]])
      when(dataRequest.userAnswers.get(any[Gettable[TaxReturnObligation]])(any)).thenReturn(None)

      val result = sut.onPageLoad(dataRequest)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result) mustBe Some(routes.SubmittedReturnsController.onPageLoad().url)
    }
  }

  "onSubmit" should {
    "invoke the journey action" in {
      when(journeyAction.async(any)) thenReturn mock[Action[AnyContent]]
      Try(await(sut.onSubmit(FakeRequest())))
      verify(journeyAction).async(any)
    }

    "set UserAnswer" in {
      setUpMocks()

      await(sut.onSubmit.skippingJourneyAction(dataRequest))

      verify(dataRequest.userAnswers).setOrFail(meq(AmendDirectExportPlasticPackagingPage), meq(10L), any)(any)
    }

    "save a empty userAnswer to cache" in {
      val ans = createUserAnswers

      setUpMocks()
      when(dataRequest.userAnswers).thenReturn(ans)
      when(dataRequest.pptReference).thenReturn("123")

      await(sut.onSubmit(dataRequest))

      verify(cacheConnector).saveUserAnswerFunc(meq("123"))(any)
      verify(saveFunction).apply(ans.set(AmendDirectExportPlasticPackagingPage, 10L).get, true)
    }

    "redirect to amend exported by another business page" in {
      setUpMocks()

      val result = sut.onSubmit.skippingJourneyAction(dataRequest)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result) mustBe Some(
        controllers.amends.routes.AmendExportedByAnotherBusinessController.onPageLoad.url
      )
    }

    "return a bad request with an error on view" in {
      val formError = new AmendDirectExportPlasticPackagingFormProvider()().withError("error", "error message")
      when(formProvider.apply()).thenReturn(form)
      when(form.bindFromRequest()(any, any)).thenReturn(formError)

      val result = sut.onSubmit.skippingJourneyAction(dataRequest)

      status(result) mustEqual BAD_REQUEST
      verify(view).apply(meq(formError))(any, any)
    }

  }

  private def setUpMocks(): Unit = {
    when(formProvider.apply()).thenReturn(form)
    when(form.bindFromRequest()(any, any)).thenReturn(
      new AmendDirectExportPlasticPackagingFormProvider()().bind(Map("value" -> "10"))
    )
    when(dataRequest.userAnswers.setOrFail(any[Settable[Long]], any, any)(any)).thenReturn(UserAnswers("123"))
    when(cacheConnector.saveUserAnswerFunc(any)(any)).thenReturn(saveFunction)
    when(saveFunction.apply(any, any)).thenReturn(Future.successful(true))
  }
}
