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
import forms.returns.DirectlyExportedComponentsFormProvider
import models.Mode.NormalMode
import models.UserAnswers
import models.requests.DataRequest
import navigation.Navigator
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar.{reset, verify, when}
import org.mockito.{Answers, ArgumentCaptor, ArgumentMatchers}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import pages.returns._
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{AnyContent, Call}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.returns.DirectlyExportedComponentsView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DirectlyExportedComponentsControllerSpec extends PlaySpec with MockitoSugar with JourneyActionAnswer with BeforeAndAfterEach {

  private val dataRequest    = mock[DataRequest[AnyContent]](Answers.RETURNS_DEEP_STUBS)
  private val messagesApi    = mock[MessagesApi]
  private val cacheConnector = mock[CacheConnector]
  private val navigator      = mock[Navigator]
  private val journeyAction  = mock[JourneyAction]
  private val formProvider   = mock[DirectlyExportedComponentsFormProvider]
  private val view           = mock[DirectlyExportedComponentsView]
  private val nonExportedAmountHelper = mock[NonExportedAmountHelper]
  private val sut            = new DirectlyExportedComponentsController(
    messagesApi,
    cacheConnector,
    navigator,
    journeyAction,
    nonExportedAmountHelper,
    formProvider,
    stubMessagesControllerComponents(),
    view)

  override def beforeEach() = {
    super.beforeEach()

    reset(journeyAction, view, cacheConnector, dataRequest, navigator, nonExportedAmountHelper)

    when(view.apply(any, any, any)(any, any)).thenReturn(HtmlFormat.empty)
    when(journeyAction.apply(any)).thenAnswer(byConvertingFunctionArgumentsToAction)
    when(journeyAction.async(any)).thenAnswer(byConvertingFunctionArgumentsToFutureAction)
    when(nonExportedAmountHelper.totalPlastic(any)).thenReturn(Some(10L))
  }

  "onPageLoad" should {

    "use the journey action" in {
      sut.onPageLoad(NormalMode)
      verify(journeyAction).apply(any)
    }

    "return OK" in {
      setUpForm(createUserAnswer, new DirectlyExportedComponentsFormProvider()())

      val result = sut.onPageLoad(NormalMode)(dataRequest)

      status(result) mustEqual OK
    }

    "return a view with an empty form and total plastic" in {
      val form = new DirectlyExportedComponentsFormProvider()()
      setUpForm(createUserAnswer, form)

      await(sut.onPageLoad(NormalMode)(dataRequest))

      verify(view).apply(ArgumentMatchers.eq(form), ArgumentMatchers.eq(NormalMode), ArgumentMatchers.eq(10L))(any, any)
    }

    "return a view with question answered YES " in {
      setUpForm(createUserAnswer.set(DirectlyExportedComponentsPage, true).get, new DirectlyExportedComponentsFormProvider()())

      await(sut.onPageLoad(NormalMode)(dataRequest))

      verifyAndAssertAnswerValue(true)
    }

    "return a view with question answered No " in {
      val form = new DirectlyExportedComponentsFormProvider()()
      when(formProvider.apply()).thenReturn(form)
      when(dataRequest.userAnswers).thenReturn(createUserAnswer.set(DirectlyExportedComponentsPage, false).get)

      await(sut.onPageLoad(NormalMode)(dataRequest))

      verifyAndAssertAnswerValue(false)
    }

    "redirect to account page if total plastic cannot be calculated" in {
      when(nonExportedAmountHelper.totalPlastic(any)).thenReturn(None)
      val form = new DirectlyExportedComponentsFormProvider()()
      when(formProvider.apply()).thenReturn(form)
      when(dataRequest.userAnswers).thenReturn(UserAnswers("123"))

      val result = sut.onPageLoad(NormalMode)(dataRequest)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result) mustEqual Some(controllers.routes.IndexController.onPageLoad.url)
    }
  }

  "onSubmit" should {
    "redirect" in {
      val form = mock[Form[Boolean]]
      when(formProvider.apply()).thenReturn(form)
      when(form.bindFromRequest()(any, any)).thenReturn(new DirectlyExportedComponentsFormProvider()().bind(Map("value" -> "true")))
      when(dataRequest.userAnswers).thenReturn(createUserAnswer)
      when(cacheConnector.saveUserAnswerFunc(any)(any)).thenReturn((_, _) => Future.successful(true))
      when(navigator.nextPage(any, any, any)).thenReturn(Call(GET, "/foo"))

      val result = sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result) mustEqual Some("/foo")
      verify(navigator).nextPage(ArgumentMatchers.eq(DirectlyExportedComponentsPage), ArgumentMatchers.eq(NormalMode), any[UserAnswers])
    }

    "set userAnswer" in {
      var saveUserAnswerToCache: Option[UserAnswers] = Option.empty
      val ans                                        = createUserAnswer
      val form                                       = mock[Form[Boolean]]
      when(formProvider.apply()).thenReturn(form)
      when(form.bindFromRequest()(any, any)).thenReturn(new DirectlyExportedComponentsFormProvider()().bind(Map("value" -> "true")))
      when(dataRequest.userAnswers).thenReturn(ans)
      when(cacheConnector.saveUserAnswerFunc(any)(any)).thenReturn { (a: UserAnswers, b: Boolean) =>
        saveUserAnswerToCache = Some(a)
        Future.successful(true)
      }
      when(navigator.nextPage(any, any, any)).thenReturn(Call(GET, "/foo"))

      await(sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest))

      saveUserAnswerToCache mustBe Some(ans.set(DirectlyExportedComponentsPage, true).get)
    }

    "save user answer to the cache" in {
      val form = mock[Form[Boolean]]
      when(formProvider.apply()).thenReturn(form)
      when(form.bindFromRequest()(any, any)).thenReturn(new DirectlyExportedComponentsFormProvider()().bind(Map("value" -> "true")))
      when(dataRequest.userAnswers).thenReturn(createUserAnswer)
      when(dataRequest.pptReference).thenReturn("123")
      when(cacheConnector.saveUserAnswerFunc(any)(any)).thenReturn((_, _) => Future.successful(true))
      when(navigator.nextPage(any, any, any)).thenReturn(Call(GET, "/foo"))

      await(sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest))

      verify(cacheConnector).saveUserAnswerFunc(ArgumentMatchers.eq("123"))(any)
    }

    "return an error when error on form" in {
      val form = mock[Form[Boolean]]
      val formError = new DirectlyExportedComponentsFormProvider()().withError("error", "error message")
      when(formProvider.apply()).thenReturn(form)
      when(form.bindFromRequest()(any, any)).thenReturn(formError)
      when(dataRequest.userAnswers).thenReturn(createUserAnswer)

      val result = sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest)

      status(result) mustEqual BAD_REQUEST
      verify(view).apply(ArgumentMatchers.eq(formError), ArgumentMatchers.eq(NormalMode), ArgumentMatchers.eq(10L))(any, any)
    }

    "redirect when total plastic cannot be calculated" in {
      when(nonExportedAmountHelper.totalPlastic(any)).thenReturn(None)
      val formError = new DirectlyExportedComponentsFormProvider()().withError("error", "error message")
      val form = mock[Form[Boolean]]
      when(formProvider.apply()).thenReturn(form)
      when(form.bindFromRequest()(any, any)).thenReturn(formError)
      when(dataRequest.userAnswers).thenReturn(UserAnswers("123"))

      val result = sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result) mustEqual Some(controllers.routes.IndexController.onPageLoad.url)
    }
  }

  private def setUpForm(userAnswer: UserAnswers, form: Form[Boolean]) = {
    when(formProvider.apply()).thenReturn(form)
    when(dataRequest.userAnswers).thenReturn(userAnswer)
  }

  private def verifyAndAssertAnswerValue(answer: Boolean) = {
    val captor: ArgumentCaptor[Form[Boolean]] = ArgumentCaptor.forClass(classOf[Form[Boolean]])
    verify(view).apply(captor.capture(), any, any)(any, any)
    captor.getValue.value mustBe Some(answer)
  }

  private def createUserAnswer =
    UserAnswers("123")
      .set(ManufacturedPlasticPackagingPage, true).get
      .set(ManufacturedPlasticPackagingWeightPage, 5L).get
      .set(ImportedPlasticPackagingPage, true).get
      .set(ImportedPlasticPackagingWeightPage, 5L).get
}
