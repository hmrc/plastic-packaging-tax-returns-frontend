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

package controllers

import org.apache.pekko.stream.testkit.NoMaterializer
import base.FakeIdentifierActionWithEnrolment
import config.FrontendAppConfig
import connectors.{FinancialsConnector, ObligationsConnector}
import controllers.actions.FakeDataRetrievalAction
import models.PPTSubscriptionDetails
import models.financials.PPTFinancials
import models.obligations.PPTObligations
import models.returns.TaxReturnObligation
import models.subscription.LegalEntityDetails
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.refEq
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.PlaySpec
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import repositories.SessionRepository
import repositories.SessionRepository.Paths
import views.html.IndexView

import java.time.LocalDate.now
import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class IndexControllerSpec extends PlaySpec with BeforeAndAfterEach {

  val mockObligationsConnector: ObligationsConnector = mock[ObligationsConnector]
  val mockFinancialsConnector: FinancialsConnector   = mock[FinancialsConnector]
  val mockSessionRepository: SessionRepository       = mock[SessionRepository]
  val mockView: IndexView                            = mock[IndexView]
  val mockMessagesApi: MessagesApi                   = mock[MessagesApi]
  val mockAppConfig: FrontendAppConfig               = mock[FrontendAppConfig]
  val mockLegalEntityDetails: LegalEntityDetails     = mock[LegalEntityDetails]
  val mockPPTFinancials: PPTFinancials               = mock[PPTFinancials]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(
      mockObligationsConnector,
      mockFinancialsConnector,
      mockSessionRepository,
      mockView,
      mockMessagesApi,
      mockAppConfig,
      mockLegalEntityDetails,
      mockPPTFinancials
    )
    when(mockView.apply(any, any, any, any, any)(any, any)).thenReturn(Html.apply("test view"))
  }

  val sut = new IndexController(
    stubMessagesControllerComponents().messagesApi,
    new FakeIdentifierActionWithEnrolment(stubPlayBodyParsers(NoMaterializer)),
    mockView,
    mockAppConfig,
    mockSessionRepository,
    mockFinancialsConnector,
    mockObligationsConnector,
    new FakeDataRetrievalAction(None)
  )(global)

  "onPageLoad" must {
    "construct and return the account home page" in {
      when(mockSessionRepository.get[Any](any, any)(any)).thenReturn(
        Future.successful(Some(PPTSubscriptionDetails(mockLegalEntityDetails)))
      )
      when(mockFinancialsConnector.getPaymentStatement(any)(any)).thenReturn(Future.successful(mockPPTFinancials))
      when(mockObligationsConnector.getOpen(any)(any)).thenReturn(
        Future.successful(PPTObligations(None, None, 1, true, true))
      )
      when(mockObligationsConnector.getFulfilled(any)(any)).thenReturn(Future.successful(Seq.empty))

      when(mockPPTFinancials.paymentStatement()(any)).thenReturn("Test payment statement")

      val result = sut.onPageLoad()(FakeRequest())

      status(result) mustBe OK
      contentAsString(result) mustBe "test view"

      verify(mockView).apply(
        refEq(mockLegalEntityDetails),
        refEq(Some(PPTObligations(None, None, 1, true, true))),
        refEq(true),
        refEq(Some("Test payment statement")),
        any
      )(
        any,
        any
      )

      withClue("session should be called to get legalEntityDetails") {
        verify(mockSessionRepository).get(ArgumentMatchers.contains("SomeId-123"), refEq(Paths.SubscriptionIsActive))(
          any
        )
      }
      withClue("payments should be called") {
        verify(mockFinancialsConnector).getPaymentStatement(refEq("123"))(any)
      }
      withClue("obligations Open should be called") {
        verify(mockObligationsConnector).getOpen(refEq("123"))(any)
      }
      withClue("obligations Fulfilled should be called") {
        verify(mockObligationsConnector).getFulfilled(refEq("123"))(any)
      }
    }

    "calculate ifFirstReturn to be false, when fulfilled obligation is nonEmpty" in {
      when(mockSessionRepository.get[Any](any, any)(any)).thenReturn(
        Future.successful(Some(PPTSubscriptionDetails(mockLegalEntityDetails)))
      )
      when(mockFinancialsConnector.getPaymentStatement(any)(any)) thenReturn (Future.successful(
        PPTFinancials(None, None, None)
      ))
      when(mockObligationsConnector.getOpen(any)(any)).thenReturn(
        Future.successful(PPTObligations(None, None, 1, true, true))
      )
      when(mockPPTFinancials.paymentStatement()(any)).thenReturn("Test payment statement")

      when(mockObligationsConnector.getFulfilled(any[String])(any)).thenReturn(
        Future.successful(Seq(TaxReturnObligation(now(), now(), now(), "")))
      )

      await(sut.onPageLoad()(FakeRequest()))

      verify(mockView).apply(any, any, refEq(false), any, any)(any, any)
    }

    "financials data not returned" in {
      when(mockSessionRepository.get[Any](any, any)(any)).thenReturn(
        Future.successful(Some(PPTSubscriptionDetails(mockLegalEntityDetails)))
      )
      when(mockFinancialsConnector.getPaymentStatement(any)(any)).thenReturn(Future.failed(new Exception("boom")))
      when(mockObligationsConnector.getOpen(any)(any)).thenReturn(
        Future.successful(PPTObligations(None, None, 1, true, true))
      )

      when(mockObligationsConnector.getFulfilled(any)(any)).thenReturn(
        Future.successful(Seq(TaxReturnObligation(now(), now(), now(), "")))
      )

      await(sut.onPageLoad()(FakeRequest()))

      verify(mockView).apply(any, any, any, any, any)(any, any)
    }

    "getFulfilled obligations fails" in {
      when(mockSessionRepository.get[Any](any, any)(any)).thenReturn(
        Future.successful(Some(PPTSubscriptionDetails(mockLegalEntityDetails)))
      )
      when(mockFinancialsConnector.getPaymentStatement(any)(any)) thenReturn (Future.successful(
        PPTFinancials(None, None, None)
      ))
      when(mockObligationsConnector.getOpen(any)(any)).thenReturn(
        Future.successful(PPTObligations(None, None, 1, true, true))
      )
      when(mockPPTFinancials.paymentStatement()(any)).thenReturn("Test payment statement")

      when(mockObligationsConnector.getFulfilled(any)(any)).thenReturn(Future.failed(new Exception("boom")))

      await(sut.onPageLoad()(FakeRequest()))

      verify(mockView).apply(any, any, refEq(false), any, any)(any, any)
    }

    "getObligationDetail fails" in {
      when(mockSessionRepository.get[Any](any, any)(any)).thenReturn(
        Future.successful(Some(PPTSubscriptionDetails(mockLegalEntityDetails)))
      )
      when(mockFinancialsConnector.getPaymentStatement(any)(any)) thenReturn (Future.successful(
        PPTFinancials(None, None, None)
      ))
      when(mockPPTFinancials.paymentStatement()(any)).thenReturn("Test payment statement")
      when(mockObligationsConnector.getFulfilled(any)(any)).thenReturn(
        Future.successful(Seq(TaxReturnObligation(now(), now(), now(), "")))
      )

      when(mockObligationsConnector.getOpen(any)(any)).thenReturn(Future.failed(new Exception("boom")))

      await(sut.onPageLoad()(FakeRequest()))

      verify(mockView).apply(any, refEq(None), any, any, any)(any, any)
    }
  }

}
