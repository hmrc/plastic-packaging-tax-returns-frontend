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
import forms.returns.ExportedPlasticPackagingWeightFormProvider
import models.Mode._
import models.UserAnswers
import models.requests.DataRequest
import navigation.ReturnsJourneyNavigator
import org.mockito.ArgumentMatchers.{eq => meq}
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar.{reset, verify, when}
import org.mockito.{Answers, ArgumentMatchers}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import pages.returns.DirectlyExportedWeightPage
import play.api.data.Form
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.i18n.MessagesApi
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, defaultAwaitTimeout, redirectLocation, status, stubMessagesControllerComponents}
import play.twirl.api.Html
import queries.Gettable
import views.html.returns.ExportedPlasticPackagingWeightView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

class ExportedPlasticPackagingWeightControllerSpec
    extends PlaySpec
    with MockitoSugar
    with JourneyActionAnswer
    with BeforeAndAfterEach {

  private val dataRequest                 = mock[DataRequest[AnyContent]](Answers.RETURNS_DEEP_STUBS)
  private val form                        = mock[Form[Long]]
  private val messagesApi                 = mock[MessagesApi]
  private val cacheConnector              = mock[CacheConnector]
  private val navigator                   = mock[ReturnsJourneyNavigator]
  private val journeyAction               = mock[JourneyAction]
  private val formProvider                = mock[ExportedPlasticPackagingWeightFormProvider]
  private val view                        = mock[ExportedPlasticPackagingWeightView]
  private val mockNonExportedAmountHelper = mock[NonExportedAmountHelper]

  private val sut = new ExportedPlasticPackagingWeightController(
    messagesApi,
    cacheConnector,
    navigator,
    mockNonExportedAmountHelper,
    journeyAction,
    formProvider,
    stubMessagesControllerComponents(),
    view
  )

  val saveUserAnswersFunc: UserAnswers.SaveUserAnswerFunc = { case _ =>
    Future.successful(true)
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(
      messagesApi,
      journeyAction,
      view,
      formProvider,
      cacheConnector,
      form,
      dataRequest,
      mockNonExportedAmountHelper
    )

    when(view.apply(any, any, any)(any, any)).thenReturn(Html("correct view"))
    when(journeyAction.apply(any)).thenAnswer(byConvertingFunctionArgumentsToAction)
    when(journeyAction.async(any)).thenAnswer(byConvertingFunctionArgumentsToFutureAction)
    when(mockNonExportedAmountHelper.totalPlasticAdditions(any)).thenReturn(Some(100L))
    when(formProvider.apply()).thenReturn(form)
    when(dataRequest.pptReference).thenReturn("ppt Ref")
    when(cacheConnector.saveUserAnswerFunc(any)(any)).thenReturn(saveUserAnswersFunc)
    when(navigator.exportedPlasticPackagingWeightRoute(any, any)).thenReturn(Call("GET", "foo"))
    val answers = dataRequest.userAnswers
    when(dataRequest.userAnswers.setOrFail(any, any, any)(any)).thenReturn(answers)
    when(dataRequest.userAnswers.save(any)(any)).thenReturn(Future.successful(answers))

  }

  "onPageLoad" should {

    "use the journey action" in {
      sut.onPageLoad(NormalMode)
      verify(journeyAction).apply(any)
    }

    "return OK" in {

      val result = sut.onPageLoad(NormalMode)(dataRequest)

      status(result) mustEqual OK
    }

    "return a view total plastic" in {
      when(dataRequest.userAnswers.fill(any[Gettable[Long]], any)(any)).thenReturn(form)

      await(sut.onPageLoad(NormalMode)(dataRequest))
      verify(view).apply(ArgumentMatchers.eq(form), meq(NormalMode), meq(100L))(any, any)
      verify(dataRequest.userAnswers).fill(meq(DirectlyExportedWeightPage), meq(form))(any)
    }

    "redirect to index controller when cannot calculate total plastic" in {
      when(mockNonExportedAmountHelper.totalPlasticAdditions(any)).thenReturn(None)

      val result = sut.onPageLoad(NormalMode)(dataRequest)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.IndexController.onPageLoad.url
    }
  }

  "onSubmit" should {
    "invoke the journey action" in {
      when(journeyAction.async(any)) thenReturn mock[Action[AnyContent]]
      Try(await(sut.onSubmit(NormalMode)(FakeRequest())))
      verify(journeyAction).async(any)
    }

    "save to the cache and redirect to the next page" in {
      when(form.bindFromRequest()(any, any))
        .thenReturn(new ExportedPlasticPackagingWeightFormProvider()().fill(5))

      val result = sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest)

      status(result) mustEqual SEE_OTHER
      verify(navigator).exportedPlasticPackagingWeightRoute(true, NormalMode)
      verify(cacheConnector).saveUserAnswerFunc(ArgumentMatchers.eq("ppt Ref"))(any)
      verify(dataRequest.userAnswers).save(meq(saveUserAnswersFunc))(any)
    }

    "save userAnswer with clean up equal to false in checkMode" in {
      when(form.bindFromRequest()(any, any))
        .thenReturn(new ExportedPlasticPackagingWeightFormProvider()().fill(5))

      await(sut.onSubmit(CheckMode).skippingJourneyAction(dataRequest))

      verify(dataRequest.userAnswers).setOrFail(meq(DirectlyExportedWeightPage), meq(5L), meq(false))(any)
    }

    "save userAnswer with clean up equal to true in NormalMode" in {
      when(form.bindFromRequest()(any, any))
        .thenReturn(new ExportedPlasticPackagingWeightFormProvider()().fill(5))

      await(sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest))

      verify(dataRequest.userAnswers).setOrFail(meq(DirectlyExportedWeightPage), meq(5L), meq(true))(any)
    }

    "return an error" when {
      "invalid form" in {
        val errorForm = new ExportedPlasticPackagingWeightFormProvider()().withError("error", "message")
        when(form.bindFromRequest()(any, any))
          .thenReturn(errorForm)

        val result = sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest)

        status(result) mustEqual BAD_REQUEST
        verify(view).apply(meq(errorForm), meq(NormalMode), meq(100L))(any, any)
      }

      "redirect to index controller if cannot calculate total plastic" in {
        when(mockNonExportedAmountHelper.totalPlasticAdditions(any)).thenReturn(None)
        when(form.bindFromRequest()(any, any))
          .thenReturn(new ExportedPlasticPackagingWeightFormProvider()().withError("error", "message"))

        val result = sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.IndexController.onPageLoad.url)
      }
    }
  }

}
