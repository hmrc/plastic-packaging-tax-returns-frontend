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
import forms.returns.ExportedPlasticPackagingWeightFormProvider
import models.Mode.NormalMode
import models.UserAnswers
import models.requests.DataRequest
import navigation.{Navigator, ReturnsJourneyNavigator}
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.Mockito.never
import org.mockito.MockitoSugar.{reset, verify, when}
import org.mockito.{Answers, ArgumentMatchers}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import pages.returns.{DirectlyExportedComponentsPage, ExportedPlasticPackagingWeightPage, ImportedPlasticPackagingWeightPage, ManufacturedPlasticPackagingWeightPage}
import play.api.data.Form
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.i18n.MessagesApi
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, defaultAwaitTimeout, redirectLocation, status, stubMessagesControllerComponents}
import play.twirl.api.Html
import views.html.returns.ExportedPlasticPackagingWeightView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

class ExportedPlasticPackagingWeightControllerSpec
  extends PlaySpec
    with MockitoSugar
    with JourneyActionAnswer
    with BeforeAndAfterEach {

   private val ans = UserAnswers("123")
          .set(ManufacturedPlasticPackagingWeightPage, 7L).get
          .set(ImportedPlasticPackagingWeightPage, 5L).get

  private val dataRequest = mock[DataRequest[AnyContent]](Answers.RETURNS_DEEP_STUBS)
  private val form = mock[Form[Long]]
  private val messagesApi = mock[MessagesApi]
  private val cacheConnector = mock[CacheConnector]
  private val navigator = mock[ReturnsJourneyNavigator]
  private val journeyAction = mock[JourneyAction]
  private val formProvider = mock[ExportedPlasticPackagingWeightFormProvider]
  private val view = mock[ExportedPlasticPackagingWeightView]

  private val sut = new ExportedPlasticPackagingWeightController(
    messagesApi,
    cacheConnector,
    navigator,
    journeyAction,
    formProvider,
    stubMessagesControllerComponents,
    view
  )

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(
      messagesApi,
      journeyAction,
      view,
      formProvider,
      cacheConnector,
      form,
      dataRequest
    )

    when(view.apply(any, any, any)(any, any)).thenReturn(Html("correct view"))
    when(journeyAction.apply(any)).thenAnswer(byConvertingFunctionArgumentsToAction)
    when(journeyAction.async(any)).thenAnswer(byConvertingFunctionArgumentsToFutureAction)
  }

  "onPageLoad" should {

    "use the journey action" in {
      sut.onPageLoad(NormalMode)
      verify(journeyAction).apply(any)
    }

    "return OK" in {
      when(dataRequest.userAnswers).thenReturn(ans)

      val result = sut.onPageLoad(NormalMode)(dataRequest)

      status(result) mustEqual OK
    }

    "return a view total plastic" in {
      when(formProvider.apply()).thenReturn(form)
      when(dataRequest.userAnswers).thenReturn(ans)

      await(sut.onPageLoad(NormalMode)(dataRequest))

      verify(view).apply(ArgumentMatchers.eq(form), ArgumentMatchers.eq(NormalMode), ArgumentMatchers.eq(12L))(any,any)
    }

    "return an empty form" in {
      when(formProvider.apply()).thenReturn(form)
      when(dataRequest.userAnswers).thenReturn(ans)

      await(sut.onPageLoad(NormalMode)(dataRequest))

      verify(form, never()).fill(any)
    }

    "return a filled form" in {
      ans.set(ExportedPlasticPackagingWeightPage, 10L).get

      when(formProvider.apply()).thenReturn(form)
      when(dataRequest.userAnswers).thenReturn(ans.set(ExportedPlasticPackagingWeightPage, 10L).get)

      await(sut.onPageLoad(NormalMode)(dataRequest))

      verify(form).fill(10L)
    }

    "redirect to index controller when cannot calculate total plastic" in {
      when(formProvider.apply()).thenReturn(form)
      when(dataRequest.userAnswers).thenReturn(UserAnswers("123"))

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

    "set userAnswer and redirect to the next page" in {

      val expectedAnswer = ans
        .set(ExportedPlasticPackagingWeightPage, 5L).get
        .set(DirectlyExportedComponentsPage, true).get

      setUpMocks

      val result = sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest)

      status(result) mustEqual SEE_OTHER
      verify(navigator).exportedPlasticPackagingWeightRoute(false, NormalMode)
    }

    "save the value to the cache" in {
      setUpMocks

      await(sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest))

      verify(cacheConnector).saveUserAnswerFunc(ArgumentMatchers.eq("123"))(any)
    }

    "return an error" when {
      "invalid form" in {
        when(dataRequest.userAnswers).thenReturn(ans)
        setUpFormWithError

        val result = sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest)

        status(result) mustEqual BAD_REQUEST
        verify(view).apply(any[Form[Long]], ArgumentMatchers.eq(NormalMode), ArgumentMatchers.eq(12L))(any,any)
      }

      "redirect to index controller if cannot calculate total plastic" in {
        when(dataRequest.userAnswers).thenReturn(UserAnswers("123"))
        setUpFormWithError

        val result = sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustEqual Some(controllers.routes.IndexController.onPageLoad.url)
      }
    }
  }

  private def setUpFormWithError: Unit = {
    when(formProvider.apply()).thenReturn(form)
    when(form.bindFromRequest()(any, any))
      .thenReturn(new ExportedPlasticPackagingWeightFormProvider()().withError("error", "message"))
  }

  private def setUpMocks: Unit = {
    when(formProvider.apply()).thenReturn(form)
    when(form.bindFromRequest()(any, any))
      .thenReturn(new ExportedPlasticPackagingWeightFormProvider()().bind(Map("value" -> "5")))
    when(dataRequest.userAnswers).thenReturn(ans)
    when(dataRequest.pptReference).thenReturn("123")
    when(cacheConnector.saveUserAnswerFunc(any)(any)).thenReturn({ case _ => Future.successful(true) })
    when(navigator.exportedPlasticPackagingWeightRoute(any, any)).thenReturn(Call("GET", "foo"))
  }
}
