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
import cacheables.ReturnObligationCacheable
import controllers.actions.JourneyAction
import models.requests.DataRequest
import models.returns.TaxReturnObligation
import navigation.ReturnsJourneyNavigator
import org.mockito.Answers
import org.mockito.ArgumentMatchersSugar.{any, eqTo}
import org.mockito.MockitoSugar.{mock, reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import pages.returns.credits.WhatDoYouWantToDoPage
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{AnyContent, Call, RequestHeader}
import play.api.test.Helpers.{GET, await, defaultAwaitTimeout, redirectLocation, status, stubMessagesControllerComponents}
import play.twirl.api.HtmlFormat
import views.html.returns.NowStartYourReturnView

import java.time.LocalDate

class NowStartYourReturnControllerSpec extends PlaySpec with JourneyActionAnswer with BeforeAndAfterEach {

  val aTaxObligation: TaxReturnObligation =
    TaxReturnObligation(LocalDate.of(2022, 7, 5), LocalDate.of(2022, 10, 5), LocalDate.of(2023, 1, 5), "PK1")

  private val request          = mock[DataRequest[AnyContent]](Answers.RETURNS_DEEP_STUBS)
  private val messages         = mock[Messages]
  private val journeyAction    = mock[JourneyAction]
  private val messagesApi      = mock[MessagesApi]
  private val view             = mock[NowStartYourReturnView]
  private val returnsNavigator = mock[ReturnsJourneyNavigator]

  private val sut = new NowStartYourReturnController(
    messagesApi,
    journeyAction,
    stubMessagesControllerComponents(),
    view,
    returnsNavigator
  )
  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(view, request, returnsNavigator)

    when(request.userAnswers.get(eqTo(ReturnObligationCacheable))(any)).thenAnswer(Some(aTaxObligation))
    when(messagesApi.preferred(any[RequestHeader])).thenReturn(messages)
    when(view.apply(any, any, any)(any, any)).thenReturn(HtmlFormat.empty)
    when(journeyAction.apply(any)).thenAnswer(byConvertingFunctionArgumentsToAction)
    when(journeyAction.async(any)).thenAnswer(byConvertingFunctionArgumentsToFutureAction)
  }

  "onPageLoad" should {
    "use the journey action" in {
      sut.onPageLoad()

      verify(journeyAction).apply(any)
    }

    "return 200" in {
      when(returnsNavigator.firstPageOfReturnSection).thenReturn(Call(GET, "foo"))

      val result = sut.onPageLoad()(request)

      status(result) mustBe OK
    }

    "get the obligation" in {
      await(sut.onPageLoad()(request))

      verify(request.userAnswers).get(eqTo(ReturnObligationCacheable))(any)
    }

    "display a view" in {
      val nextPage = Call(GET, "foo")

      when(returnsNavigator.firstPageOfReturnSection).thenReturn(nextPage)
      when(messages.apply(any[String], any, any, any)).thenReturn("date")
      when(request.userAnswers.getOrFail(eqTo(WhatDoYouWantToDoPage))(any, any)).thenReturn(true)

      await(sut.onPageLoad()(request))

      verify(view).apply(eqTo("date"), eqTo(true), eqTo(nextPage))(any, any)
    }

    "redirect" in {
      when(request.userAnswers.get(eqTo(ReturnObligationCacheable))(any)).thenAnswer(None)

      val result = sut.onPageLoad()(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.IndexController.onPageLoad.url
    }
  }
}
