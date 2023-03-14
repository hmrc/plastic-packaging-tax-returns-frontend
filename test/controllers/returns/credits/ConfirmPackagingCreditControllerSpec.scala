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
import connectors.{CacheConnector, CalculateCreditsConnector, DownstreamServiceError}
import controllers.BetterMockActionSyntax
import controllers.actions.JourneyAction
import models.Mode.{CheckMode, NormalMode}
import models.UserAnswers.SaveUserAnswerFunc
import models.requests.DataRequest
import models.{CreditBalance, UserAnswers}
import navigation.ReturnsJourneyNavigator
import org.mockito.Answers
import org.mockito.ArgumentMatchers.{eq => meq}
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.Mockito.{never, reset}
import org.mockito.MockitoSugar.{verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import pages.returns.credits.WhatDoYouWantToDoPage
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, Call}
import play.api.test.Helpers._
import play.twirl.api.Html
import queries.Settable
import services.LocalDateService
import views.html.returns.credits.{ConfirmPackagingCreditView, TooMuchCreditClaimedView}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

class ConfirmPackagingCreditControllerSpec
  extends PlaySpec
    with JourneyActionAnswer
    with MockitoSugar
    with BeforeAndAfterEach {

  private val saveAnsFun = mock[SaveUserAnswerFunc]
  private val dataRequest = mock[DataRequest[AnyContent]](Answers.RETURNS_DEEP_STUBS)
  private val mockCalculateCreditConnector = mock[CalculateCreditsConnector]
  private val mockMessagesApi: MessagesApi = mock[MessagesApi]
  private val controllerComponents = stubMessagesControllerComponents()
  private val mockView = mock[ConfirmPackagingCreditView]
  private val tooMuchCreditView = mock[TooMuchCreditClaimedView]
  private val cacheConnector = mock[CacheConnector]
  private val returnsJourneyNavigator = mock[ReturnsJourneyNavigator]
  private val localDateService = mock[LocalDateService]
  private val journeyAction = mock[JourneyAction]

  private val sut = new ConfirmPackagingCreditController(
    mockMessagesApi,
    mockCalculateCreditConnector,
    journeyAction,
    controllerComponents,
    mockView,
    tooMuchCreditView,
    cacheConnector, 
    returnsJourneyNavigator,
    localDateService
  )

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(
      mockCalculateCreditConnector,
      mockView,
      tooMuchCreditView,
      journeyAction,
      dataRequest,
      cacheConnector,
      returnsJourneyNavigator)

    when(dataRequest.pptReference).thenReturn("123")
    when(journeyAction.async(any)).thenAnswer(byConvertingFunctionArgumentsToFutureAction)
  }


  "onPageLoad" should {

    "use the journey action" in {
      sut.onPageLoad(NormalMode)
      verify(journeyAction).async(any)
    }

    "return OK" in {

      setUpMockForConfirmCreditsView()

      val result = sut.onPageLoad(NormalMode)(dataRequest)

      status(result) mustBe OK
    }

    "view the tax rate per tonne" when {
      "before 1st April 2023" in {
        setUpMockForConfirmCreditsView()
        when(localDateService.isTodayPostTaxRegimeStartDate).thenReturn(false)

        await(sut.onPageLoad(NormalMode)(dataRequest))

        verify(mockView).apply(any, any, any, any, meq(false))(any, any)
      }

      "after 1st April 2023" in {
        setUpMockForConfirmCreditsView()
        when(localDateService.isTodayPostTaxRegimeStartDate).thenReturn(true)

        await(sut.onPageLoad(NormalMode)(dataRequest))

        verify(mockView).apply(any, any, any, any, meq(true))(any, any)
      }
    }

    "return the ConfirmPackagingCreditView with the credit amount on page loading" when {
      "total requested credit is less than available credit - (NormalMode)" in {
        when(returnsJourneyNavigator.confirmCreditRoute(any)) thenReturn Call("Hi", "You")
        setUpMockForConfirmCreditsView()
        await(sut.onPageLoad(NormalMode)(dataRequest))
        verify(mockView).apply(meq(BigDecimal(5)), meq(500L), meq(Call("Hi", "You")), meq(NormalMode), any)(any,any)
      }

      "total requested credit is less than available credit - (CheckMode)" in {
        when(returnsJourneyNavigator.confirmCreditRoute(any)) thenReturn Call("get", "cheese")
        setUpMockForConfirmCreditsView()
        await(sut.onPageLoad(CheckMode)(dataRequest))
        verify(mockView).apply(meq(BigDecimal(5)), meq(500L), meq(Call("get", "cheese")), meq(CheckMode), any
        )(any,any)
      }
    }

    "return too muchCreditView" when {
      "total requested credit is grater than available credit" in {
        when(tooMuchCreditView.apply(any, any)(any,any)).thenReturn(Html("too much credit view"))
        when(mockCalculateCreditConnector.get(any)(any))
          .thenReturn(Future.successful(Right(CreditBalance(10, 20, 500L, false))))

        await(sut.onPageLoad(NormalMode)(dataRequest))

        verify(tooMuchCreditView).apply(any, any)(any,any)
        verify(mockView, never()).apply(any, any, any, any, any)(any,any)
      }
    }

    "display the exported and converted weight" when {
      "total requested credit is less than available credit" in {
        setUpMockForConfirmCreditsView()

        await(sut.onPageLoad(NormalMode)(dataRequest))

        verify(mockView).apply(meq(BigDecimal(5)), meq(500L), any, any, any)(any,any)
        verify(tooMuchCreditView, never()).apply(any, any)(any,any)
      }

      "only exported weight is Available" in {
        setUpMockForConfirmCreditsView()

        await(sut.onPageLoad(NormalMode)(dataRequest))

        verify(mockView).apply(meq(BigDecimal(5)), meq(500L), any, any, any)(any,any)
      }

      "only converted weight is Available" in {
        setUpMockForConfirmCreditsView()

        await(sut.onPageLoad(NormalMode)(dataRequest))

        verify(mockView).apply(meq(BigDecimal(5)), meq(500L), any, any, any)(any,any)
      }
    }

    "return an error page" in {
      when(mockCalculateCreditConnector.get(any)(any))
        .thenReturn(Future.successful(Left(DownstreamServiceError("Error", new Exception("error")))))

      val result = sut.onPageLoad(NormalMode)(dataRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad.url
    }
  }

  "onCancelClaim" should {
    "invoke the journey action" in {
      when(journeyAction.async(any)) thenReturn mock[Action[AnyContent]]
      Try(await(sut.onCancelClaim(NormalMode)(dataRequest)))
      verify(journeyAction).async(any)
    }

    "set userAnswer" in {
      setUpMockForCancelCredit(mock[UserAnswers])

      await(sut.onCancelClaim(NormalMode).skippingJourneyAction(dataRequest))

      verify(dataRequest.userAnswers).setOrFail(meq(WhatDoYouWantToDoPage), meq(false), any)(any)
    }

    "save user answer to cache" in {
      val ans = mock[UserAnswers]
      setUpMockForCancelCredit(ans)

      await(sut.onCancelClaim(NormalMode).skippingJourneyAction(dataRequest))

      verify(ans).save(meq(saveAnsFun))(any)
      verify(cacheConnector).saveUserAnswerFunc(meq("123"))(any)
    }

    "navigate to a new page" in {
      setUpMockForCancelCredit(mock[UserAnswers])

      await(sut.onCancelClaim(NormalMode).skippingJourneyAction(dataRequest))

      verify(returnsJourneyNavigator).confirmCreditRoute(NormalMode)
    }
  }

  private def setUpMockForConfirmCreditsView(): Unit = {
    when(mockView.apply(any, any, any, any, any)(any,any)).thenReturn(Html("correct view"))
    when(mockCalculateCreditConnector.get(any)(any))
      .thenReturn(Future.successful(Right(CreditBalance(10, 5, 500, true))))
  }

  private def setUpMockForCancelCredit(ans: UserAnswers): Unit = {
    when(ans.save(any)(any)).thenReturn(Future.successful(mock[UserAnswers]))
    when(dataRequest.userAnswers.setOrFail(any[Settable[Boolean]], any, any)(any)).thenReturn(ans)
    when(cacheConnector.saveUserAnswerFunc(any)(any)).thenReturn(saveAnsFun)
    when(returnsJourneyNavigator.confirmCreditRoute(NormalMode)).thenReturn(Call("GET", "/foo"))
  }
}
