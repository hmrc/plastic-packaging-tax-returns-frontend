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

package controllers.returns.credits

import base.utils.JourneyActionAnswer
import connectors.CacheConnector
import controllers.BetterMockActionSyntax
import controllers.actions.JourneyAction
import forms.returns.credits.ClaimForWhichYearFormProvider
import forms.returns.credits.ClaimForWhichYearFormProvider.CreditRangeOption
import models.Mode.NormalMode
import models.UserAnswers
import models.requests.DataRequest
import navigation.ReturnsJourneyNavigator
import org.mockito.Answers
import org.mockito.ArgumentMatchers.{eq => meq}
import org.mockito.ArgumentMatchersSugar.{any, eqTo}
import org.mockito.Mockito.{atLeastOnce, verifyNoInteractions}
import org.mockito.MockitoSugar.{mock, reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import pages.returns.credits.WhatDoYouWantToDoPage
import play.api.data.{Form, FormError}
import play.api.data.Forms.{boolean, ignored, longNumber}
import play.api.http.Status
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json.JsPath
import play.api.mvc.{Action, AnyContent, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import queries.Gettable
import views.html.returns.credits.ClaimForWhichYearView

import java.time.LocalDate
import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class ClaimForWhichYearControllerSpec extends PlaySpec with JourneyActionAnswer with MockitoSugar with BeforeAndAfterEach  {

  private val mockMessagesApi: MessagesApi = mock[MessagesApi]
  private val messages = mock[Messages]
  private val mockNavigator: ReturnsJourneyNavigator = mock[ReturnsJourneyNavigator]
  private val mockView = mock[ClaimForWhichYearView]
  private val mockFormProvider = mock[ClaimForWhichYearFormProvider]
  private val journeyAction = mock[JourneyAction]
  private val controllerComponents = stubMessagesControllerComponents()
  private val mockCache = mock[CacheConnector]
  private val dataRequest = mock[DataRequest[AnyContent]](Answers.RETURNS_DEEP_STUBS)
  private val form = mock[Form[CreditRangeOption]]
  val testForm: Form[CreditRangeOption] = Form("x" -> ignored(CreditRangeOption(LocalDate.now(), LocalDate.now())))
  val saveUserAnswerFunc = mock[UserAnswers.SaveUserAnswerFunc]

  val sut: ClaimForWhichYearController = new ClaimForWhichYearController(
    mockMessagesApi,
    journeyAction,
    controllerComponents,
    mockView,
    mockFormProvider,
    mockNavigator,
    mockCache
  )(global)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(
      mockMessagesApi,
      journeyAction,
      mockView,
      mockFormProvider,
      form,
      mockNavigator,
      dataRequest,
      messages
    )
    when(journeyAction.apply(any)).thenAnswer(byConvertingFunctionArgumentsToAction)
    when(journeyAction.async(any)).thenAnswer(byConvertingFunctionArgumentsToFutureAction)
    when(mockView.apply(any, any, any)(any, any)).thenReturn(Html("correct view"))
    when(mockFormProvider.apply(any)) thenReturn form
  }

  "onPageLoad" must {

    "use the journey action" in {
      when(journeyAction.apply(any)) thenReturn mock[Action[AnyContent]]
      sut.onPageLoad(NormalMode)(dataRequest)
      verify(journeyAction).apply(any)
    }

    "return a 200" in {
      val result = sut.onPageLoad(NormalMode)(dataRequest)
      status(result) mustBe OK
    }

    "take in a value from form & return view" in {
      val availableYears = sut.availableYears //todo get there from somewhere
      // when(something.getAvailableYears).thenReturn(availableYears)

      val result = sut.onPageLoad(NormalMode)(dataRequest)

      status(result) mustBe OK
      verify(mockView).apply(meq(form), meq(availableYears), meq(NormalMode))(any, any)
    }
  }

  "onSubmit" must {

    "invoke the journey action" in {
      when(journeyAction.async(any)) thenReturn mock[Action[AnyContent]]
      sut.onSubmit(NormalMode)(FakeRequest())
      verify(journeyAction).async(any)
    }

    "set the endDate in the UserAnswers" in {
      val ua = dataRequest.userAnswers
      when(mockNavigator.claimForWhichYear(any, any)).thenReturn(Call(GET, "/next/page"))
      when(mockCache.saveUserAnswerFunc(any)(any)).thenReturn(saveUserAnswerFunc)
      when(dataRequest.userAnswers.setOrFail(any[JsPath], any)(any)).thenReturn(ua)

      val creditRangeOption = CreditRangeOption(LocalDate.of(1000, 1, 1), LocalDate.of(1996, 3, 27))
      when(form.bindFromRequest()(any, any)) thenReturn testForm.fill(creditRangeOption)

      await(sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest))

      verify(dataRequest.userAnswers, atLeastOnce()).setOrFail(JsPath \ "credit" \ "1000-01-01-1996-03-27" \ "endDate", LocalDate.of(1996, 3, 27))
      verify(mockCache).saveUserAnswerFunc(meq(dataRequest.pptReference))(any)
    }

    "redirect to the next page" in {
      when(mockNavigator.claimForWhichYear(any, any)).thenReturn(Call(GET, "/next/page"))

      when(mockCache.saveUserAnswerFunc(any)(any)).thenReturn(saveUserAnswerFunc)
      when(dataRequest.pptReference).thenReturn("KILL YOURSELF")
      val userAnswers = dataRequest.userAnswers
      when(dataRequest.userAnswers.setOrFail(any[JsPath], any)(any)).thenReturn(userAnswers)
      when(userAnswers.save(any)(any)).thenReturn(Future.successful(UserAnswers("fin")))

      when(form.bindFromRequest()(any, any)) thenReturn testForm.fill(CreditRangeOption(LocalDate.now(), LocalDate.now()))

      val result = sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest)
      verify(userAnswers).save(any)(any)

      status(result) mustBe Status.SEE_OTHER
      redirectLocation(result) mustBe Some("/next/page")
      verify(mockNavigator).claimForWhichYear(meq(CreditRangeOption(LocalDate.now(), LocalDate.now())), meq(NormalMode))
    }

    "display any errors" in {
      val availableYears = sut.availableYears //todo get there from somewhere
      //when(something.getAvailableYears).thenReturn(availableYears)

      val formWithErrors = testForm.withError("key", "message")
      when(form.bindFromRequest()(any, any)) thenReturn formWithErrors

      val result = await(sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest))

      verify(mockView).apply(eqTo(formWithErrors), meq(availableYears), meq(NormalMode)) (eqTo(dataRequest), any)

      result.header.status mustBe Status.BAD_REQUEST
      contentAsString(Future.successful(result)) mustBe "correct view"
    }

  }



}
