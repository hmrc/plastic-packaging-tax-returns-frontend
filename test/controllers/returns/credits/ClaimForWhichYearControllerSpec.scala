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
import controllers.BetterMockActionSyntax
import controllers.actions.JourneyAction
import forms.returns.credits.ClaimForWhichYearFormProvider
import forms.returns.credits.ClaimForWhichYearFormProvider.YearOption
import models.Mode.NormalMode
import models.requests.DataRequest
import navigation.ReturnsJourneyNavigator
import org.mockito.Answers
import org.mockito.ArgumentMatchers.{eq => meq}
import org.mockito.ArgumentMatchersSugar.{any, eqTo}
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.MockitoSugar.{mock, reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import pages.returns.credits.WhatDoYouWantToDoPage
import play.api.data.{Form, FormError}
import play.api.data.Forms.{boolean, ignored, longNumber}
import play.api.http.Status
import play.api.i18n.{Messages, MessagesApi}
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
  private val dataRequest = mock[DataRequest[AnyContent]](Answers.RETURNS_DEEP_STUBS)
  private val form = mock[Form[YearOption]]

  val sut: ClaimForWhichYearController = new ClaimForWhichYearController(
    mockMessagesApi,
    journeyAction,
    controllerComponents,
    mockView,
    mockFormProvider,
    mockNavigator
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
    when(mockView.apply(any, any)(any, any)).thenReturn(Html("correct view"))
    when(mockFormProvider.apply(any)) thenReturn form
  }

  "onPageLoad" must {

    "use the journey action" in {
      when(journeyAction.apply(any)) thenReturn mock[Action[AnyContent]]
      sut.onPageLoad(dataRequest)
      verify(journeyAction).apply(any)
    }

    "return a 200" in {
      val result = sut.onPageLoad(dataRequest)
      status(result) mustBe OK
    }

    "take in a value from form & return view" in {
      val availableYears = sut.availableYears //todo get there from somewhere
      // when(something.getAvailableYears).thenReturn(availableYears)

      val result = sut.onPageLoad(dataRequest)

      status(result) mustBe OK
      verify(mockView).apply(meq(form), meq(availableYears))(any, any)
    }
  }

  "onSubmit" must {

    "invoke the journey action" in {
      when(journeyAction.async(any)) thenReturn mock[Action[AnyContent]]
      sut.onSubmit(FakeRequest())
      verify(journeyAction).async(any)
    }

    "redirect to the next page" in {
      when(mockNavigator.claimForWhichYear(any)).thenReturn(Call(GET, "/next/page"))
      val testForm: Form[YearOption] = Form("x" -> ignored(YearOption(LocalDate.now(), LocalDate.now())))
      when(form.bindFromRequest()(any, any)) thenReturn testForm.fill(YearOption(LocalDate.now(), LocalDate.now()))

      val result = await(sut.onSubmit.skippingJourneyAction(dataRequest))
      verify(mockNavigator).claimForWhichYear(meq(YearOption(LocalDate.now(), LocalDate.now())))

      result.header.status mustBe Status.SEE_OTHER
      redirectLocation(Future.successful(result)) mustBe Some("/next/page")
    }

    "display any errors" in {
      val availableYears = sut.availableYears //todo get there from somewhere
      //when(something.getAvailableYears).thenReturn(availableYears)

      val testForm: Form[YearOption] = Form("x" -> ignored(YearOption(LocalDate.now(), LocalDate.now())))
      val formWithErrors = testForm.withError("key", "message")
      when(form.bindFromRequest()(any, any)) thenReturn formWithErrors

      val result = await(sut.onSubmit.skippingJourneyAction(dataRequest))

      verify(mockView).apply(eqTo(formWithErrors), meq(availableYears)) (eqTo(dataRequest), any)

      result.header.status mustBe Status.BAD_REQUEST
      contentAsString(Future.successful(result)) mustBe "correct view"
    }

  }



}
