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

package controllers.returns.credits

import base.utils.JourneyActionAnswer
import cacheables.ReturnObligationCacheable
import connectors.CacheConnector
import controllers.BetterMockActionSyntax
import controllers.actions.JourneyAction
import forms.returns.credits.ExportedCreditsWeightFormProvider
import models.Mode.NormalMode
import models.UserAnswers
import models.UserAnswers.SaveUserAnswerFunc
import models.requests.DataRequest
import models.returns.credits.SingleYearClaim
import models.returns.{CreditRangeOption, CreditsAnswer, TaxReturnObligation}
import navigation.ReturnsJourneyNavigator
import org.mockito.Answers
import org.mockito.ArgumentMatchers.{eq => meq}
import org.mockito.ArgumentMatchersSugar.{any, eqTo}
import org.mockito.MockitoSugar.{reset, verify, when}
import org.mockito.captor.ArgCaptor
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.PlaySpec
import pages.returns.credits.ExportedCreditsPage
import play.api.data.Form
import play.api.data.Forms.{ignored, longNumber}
import play.api.http.Status
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.i18n.MessagesApi
import play.api.libs.json.JsPath
import play.api.mvc.{Action, AnyContent, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, await, contentAsString, defaultAwaitTimeout, redirectLocation, status, stubMessagesControllerComponents}
import play.twirl.api.{Html, HtmlFormat}
import queries.Settable
import views.html.returns.credits.ExportedCreditsWeightView

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ExportedCreditsWeightControllerSpec extends PlaySpec with JourneyActionAnswer with BeforeAndAfterEach {

  private val form              = mock[Form[Long]]
  private val request           = mock[DataRequest[AnyContent]](Answers.RETURNS_DEEP_STUBS)
  private val saveFunction      = mock[SaveUserAnswerFunc]
  private val userAnswers       = mock[UserAnswers]
  private val messagesApi       = mock[MessagesApi]
  private val journeyAction     = mock[JourneyAction]
  private val cacheConnector    = mock[CacheConnector]
  private val view              = mock[ExportedCreditsWeightView]
  private val formProvider      = mock[ExportedCreditsWeightFormProvider]
  private val navigator         = mock[ReturnsJourneyNavigator]
  private val creditRangeOption = CreditRangeOption(LocalDate.of(2023, 4, 1), LocalDate.of(2024, 3, 31))

  private val sut = new ExportedCreditsWeightController(
    messagesApi,
    journeyAction,
    cacheConnector,
    stubMessagesControllerComponents(),
    view,
    formProvider,
    navigator
  )

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(journeyAction, view, request, navigator, form, cacheConnector, saveFunction, userAnswers)

    when(formProvider.apply()).thenReturn(form)
    when(view.apply(any, any, any, any)(any, any)).thenReturn(HtmlFormat.empty)
    when(journeyAction.apply(any)).thenAnswer(byConvertingFunctionArgumentsToAction)
    when(journeyAction.async(any)).thenAnswer(byConvertingFunctionArgumentsToFutureAction)

    val aDate = LocalDate.of(2000, 1, 2)
    when(request.userAnswers.get(eqTo(ReturnObligationCacheable))(any)) thenReturn Some(
      TaxReturnObligation(aDate, aDate, aDate, "period-key")
    )

    when(request.userAnswers.getOrFail[String](eqTo(JsPath \ "credit" \ "year-key" \ "fromDate"))(any, any))
      .thenReturn("2023-04-01")
    when(request.userAnswers.getOrFail[String](eqTo(JsPath \ "credit" \ "year-key" \ "toDate"))(any, any))
      .thenReturn("2024-03-31")
    when(request.userAnswers.get[SingleYearClaim](eqTo(JsPath \ "credit" \ "year-key"))(any)) thenReturn Some(
      SingleYearClaim(
        fromDate = LocalDate.of(2023, 4, 1),
        toDate = LocalDate.of(2024, 3, 31),
        exportedCredits = None,
        convertedCredits = None
      )
    )
  }

  "onPageLoad" should {

    "use the journey action" in {
      sut.onPageLoad("year-key", NormalMode)
      verify(journeyAction).async(any)
    }

    "return 200" in {
      val result = sut.onPageLoad("year-key", NormalMode)(request)
      status(result) mustBe OK
    }

    "fill the weight in the form" in {
      await(sut.onPageLoad("year-key", NormalMode)(request))
      val func = ArgCaptor[CreditsAnswer => Option[Long]]
      verify(request.userAnswers).fillWithFunc(eqTo(ExportedCreditsPage("year-key")), eqTo(form), func)(any)

      withClue("gets weight or None for displaying") {
        val creditsAnswer = mock[CreditsAnswer]
        func.value(creditsAnswer)
        verify(creditsAnswer).weightForForm
      }
    }

    "return a view" in {
      val boundForm = Form("value" -> longNumber).fill(10L)
      when(request.userAnswers.fillWithFunc(any, any[Form[Long]], any)(any)) thenReturn boundForm
      await(sut.onPageLoad("year-key", NormalMode)(request))
      verify(view).apply(eqTo(boundForm), eqTo("year-key"), eqTo(NormalMode), eqTo(creditRangeOption))(any, any)
    }

    "redirect if obligation is missing from user answers" in {
      when(request.userAnswers.get(eqTo(ReturnObligationCacheable))(any)) thenReturn None
      val result = sut.onPageLoad("year-key", NormalMode)(request)
      status(result) mustBe Status.SEE_OTHER
      redirectLocation(result).value mustBe controllers.routes.IndexController.onPageLoad.url
    }

    "redirect if year is missing from user answers" in {
      when(request.userAnswers.get[SingleYearClaim](eqTo(JsPath \ "credit" \ "year-key"))(any)) thenReturn None
      val result = sut.onPageLoad("year-key", NormalMode)(request)
      status(result) mustBe Status.SEE_OTHER
      redirectLocation(result).value mustBe routes.CreditsClaimedListController.onPageLoad(NormalMode).url
    }
  }

  "onSubmit" should {
    "invoke the journey action" in {
      when(journeyAction.async(any)) thenReturn mock[Action[AnyContent]]
      sut.onSubmit("year-key", NormalMode)(FakeRequest())
      verify(journeyAction).async(any)
    }

    "get the weight from the form" in {
      when(form.bindFromRequest()(any, any)) thenReturn Form("value" -> longNumber).fill(10L)
      when(navigator.exportedCreditsWeight("year-key", NormalMode, userAnswers)).thenReturn(Call(GET, "foo"))
      await(sut.onSubmit("year-key", NormalMode).skippingJourneyAction(request))
      verify(request.userAnswers).setOrFail(
        eqTo(ExportedCreditsPage("year-key")),
        eqTo(CreditsAnswer(true, Some(10))),
        any
      )(any)
    }

    "save the weight" in {
      setupMocks

      await(sut.onSubmit("year-key", NormalMode).skippingJourneyAction(request))

      withClue("save weight to user Answer") {
        verify(userAnswers).save(meq(saveFunction))(any)
      }

      withClue("to the correct year") {
        verify(request.userAnswers).setOrFail(meq(ExportedCreditsPage("year-key")), any, any)(any)
      }

      withClue("save weight to cache") {
        verify(cacheConnector).saveUserAnswerFunc(meq("123"))(any)
      }
    }

    "redirect" in {
      setupMocks

      val result = sut.onSubmit("year-key", NormalMode).skippingJourneyAction(request)

      status(result) mustBe SEE_OTHER
      verify(navigator).exportedCreditsWeight("year-key", NormalMode, request.userAnswers)
    }

    "return an error" when {
      "error on form" in {
        when(view.apply(any, any, any, any)(any, any)).thenReturn(Html("correct view"))
        val firmWithError = Form("value" -> ignored(1L)).withError("key", "error")
        when(form.bindFromRequest()(any, any)).thenReturn(firmWithError)

        val result = sut.onSubmit("year-key", NormalMode).skippingJourneyAction(request)

        status(result) mustBe BAD_REQUEST

        withClue("pass an error to the view") {
          contentAsString(result) mustBe "correct view"
          verify(view).apply(meq(firmWithError), meq("year-key"), meq(NormalMode), meq(creditRangeOption))(any, any)
        }
      }
    }

  }

  private def setupMocks = {
    when(form.bindFromRequest()(any, any)).thenReturn(Form("value" -> longNumber).fill(10L))
    when(request.userAnswers.setOrFail(any[Settable[Long]], any, any)(any)).thenReturn(userAnswers)
    when(request.userAnswers.setOrFail(any[Settable[CreditsAnswer]], any, any)(any)).thenReturn(userAnswers)
    when(userAnswers.save(any)(any)).thenReturn(Future.successful(mock[UserAnswers]))
    when(cacheConnector.saveUserAnswerFunc(any)(any)).thenReturn(saveFunction)
    when(request.pptReference).thenReturn("123")
    when(navigator.exportedCreditsWeight(any, any, any)).thenReturn(Call(GET, "foo"))
  }
}
