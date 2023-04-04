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
import forms.returns.credits.WhatDoYouWantToDoFormProvider
import models.Mode.NormalMode
import models.requests.DataRequest
import navigation.ReturnsJourneyNavigator
import org.mockito.Answers
import org.mockito.ArgumentMatchers.{eq => meq}
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar.{mock, reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import pages.returns.credits.WhatDoYouWantToDoPage
import play.api.data.Form
import play.api.data.Forms.boolean
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import queries.Gettable
import views.html.returns.credits.ClaimForWhichYearView

import scala.concurrent.ExecutionContext.global

class ClaimForWhichYearControllerSpec extends PlaySpec with JourneyActionAnswer with MockitoSugar with BeforeAndAfterEach  {

  private val mockMessagesApi: MessagesApi = mock[MessagesApi]
  private val messages = mock[Messages]
  private val mockNavigator: ReturnsJourneyNavigator = mock[ReturnsJourneyNavigator]
  private val mockView = mock[ClaimForWhichYearView]
  private val mockFormProvider = mock[WhatDoYouWantToDoFormProvider]
  private val journeyAction = mock[JourneyAction]
  private val controllerComponents = stubMessagesControllerComponents()
  private val dataRequest = mock[DataRequest[AnyContent]](Answers.RETURNS_DEEP_STUBS)
  private val form = mock[Form[Boolean]]

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
    when(mockView.apply(any)(any, any)).thenReturn(Html("correct view"))
    when(mockFormProvider.apply()) thenReturn form
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
      val validForm = Form("value" -> boolean).fill(true)
      when(dataRequest.userAnswers.fill(any[Gettable[Boolean]], any)(any)).thenReturn(validForm)

      val result = sut.onPageLoad(dataRequest)
      status(result) mustBe OK
      verify(dataRequest.userAnswers).fill(meq(WhatDoYouWantToDoPage), meq(form))(any)
      verify(mockView).apply(meq(validForm))(any, any)
    }
  }

  "onSubmit" must {

    "invoke the journey action" in {
      when(journeyAction.async(any)) thenReturn mock[Action[AnyContent]]
      sut.onSubmit(FakeRequest())
      verify(journeyAction).async(any)
    }

    "redirect to the next page" in {
      when(mockNavigator.claimForWhichYear).thenReturn(Call(GET, "/foo"))
      val result = sut.onSubmit.skippingJourneyAction(dataRequest)
      status(result) mustBe SEE_OTHER
      verify(mockNavigator).claimForWhichYear
    }

  }



}
