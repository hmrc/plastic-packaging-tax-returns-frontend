/*
 * Copyright 2022 HM Revenue & Customs
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

import akka.stream.testkit.NoMaterializer
import base.FakeIdentifierActionWithEnrolment
import connectors.{CalculateCreditsConnector, DownstreamServiceError}
import controllers.actions.{DataRequiredActionImpl, FakeDataRetrievalAction}
import models.Mode.{CheckMode, NormalMode}
import models.{CreditBalance, UserAnswers}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{never, reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import views.html.returns.credits.{ConfirmPackagingCreditView, TooMuchCreditClaimedView}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ConfirmPackagingCreditControllerSpec extends PlaySpec with MockitoSugar with BeforeAndAfterEach {

  private val mockCalculateCreditConnector = mock[CalculateCreditsConnector]
  private val mockMessagesApi: MessagesApi = mock[MessagesApi]
  private val controllerComponents = stubMessagesControllerComponents()
  private val mockView = mock[ConfirmPackagingCreditView]
  private val tooMuchCreditView = mock[TooMuchCreditClaimedView]


  private val sut = new ConfirmPackagingCreditController(
    mockMessagesApi,
    mockCalculateCreditConnector,
    new FakeIdentifierActionWithEnrolment(stubPlayBodyParsers(NoMaterializer)),
    new FakeDataRetrievalAction(Some(UserAnswers("123"))),
    new DataRequiredActionImpl(),
    controllerComponents,
    mockView,
    tooMuchCreditView
  )

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockCalculateCreditConnector, mockView, tooMuchCreditView)
  }


  "ConfirmPackagingCreditController" should {
    "return OK on pageLoading" in {
      setUpMockForConfirmCreditsView()

      val result = sut.onPageLoad(NormalMode)(FakeRequest("GET", ""))

      status(result) mustBe OK
    }

    "return the ConfirmPackagingCreditView with the credit amount on page loading" when {
      "total requested credit is less than available credit - (NormalMode)" in {
        setUpMockForConfirmCreditsView()

        await(sut.onPageLoad(NormalMode)(FakeRequest("GET", "")))

        verify(mockView).apply(
          ArgumentMatchers.eq(BigDecimal(5)),
          any(),
          ArgumentMatchers.eq(controllers.returns.routes.NowStartYourReturnController.onPageLoad)
        )(any(),any())
      }

      "total requested credit is less than available credit - (CheckMode)" in {
        setUpMockForConfirmCreditsView()

        await(sut.onPageLoad(CheckMode)(FakeRequest("GET", "")))

        verify(mockView).apply(
          ArgumentMatchers.eq(BigDecimal(5)),
          any(),
          ArgumentMatchers.eq(controllers.returns.routes.ReturnsCheckYourAnswersController.onPageLoad())
        )(any(),any())
      }
    }

    "return too muchCreditView" when {
      "total requested credit is grater than available credit" in {
        when(tooMuchCreditView.apply()(any(),any())).thenReturn(Html("too much credit view"))
        when(mockCalculateCreditConnector.get(any())(any()))
          .thenReturn(Future.successful(Right(CreditBalance(10, 20, 500L, false))))

        await(sut.onPageLoad(NormalMode)(FakeRequest("GET", "")))

        verify(tooMuchCreditView).apply()(any(),any())
        verify(mockView, never()).apply(any(), any(), any())(any(),any())
      }
    }

    "display the exported and converted weight" when {
      "total requested credit is less than available credit" in {
        setUpMockForConfirmCreditsView()

        await(sut.onPageLoad(NormalMode)(FakeRequest("GET", "")))

        verify(mockView).apply(ArgumentMatchers.eq(BigDecimal(5)), ArgumentMatchers.eq(500L), any())(any(),any())
        verify(tooMuchCreditView, never()).apply()(any(),any())
      }

      "only exported weight is Available" in {
        setUpMockForConfirmCreditsView()

        await(sut.onPageLoad(NormalMode)(FakeRequest("GET", "")))

        verify(mockView).apply(ArgumentMatchers.eq(BigDecimal(5)), ArgumentMatchers.eq(500L), any())(any(),any())
      }

      "only converted weight is Available" in {
        setUpMockForConfirmCreditsView()

        await(sut.onPageLoad(NormalMode)(FakeRequest("GET", "")))

        verify(mockView).apply(ArgumentMatchers.eq(BigDecimal(5)), ArgumentMatchers.eq(500L), any())(any(),any())
      }
    }

    "return an error page" in {
      when(mockCalculateCreditConnector.get(any())(any()))
        .thenReturn(Future.successful(Left(DownstreamServiceError("Error", new Exception("error")))))

      val result = sut.onPageLoad(NormalMode)(FakeRequest("GET", ""))

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad.url
    }
  }

  private def setUpMockForConfirmCreditsView(): Unit = {
    when(mockView.apply(any(), any(), any())(any(),any())).thenReturn(Html("correct view"))
    when(mockCalculateCreditConnector.get(any())(any()))
      .thenReturn(Future.successful(Right(CreditBalance(10, 5, 500, true))))
  }
}
