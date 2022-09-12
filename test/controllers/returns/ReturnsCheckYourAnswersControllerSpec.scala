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

package controllers.returns

import akka.stream.testkit.NoMaterializer
import base.{FakeIdentifierActionWithEnrolment, SpecBase}
import cacheables.{ObligationCacheable, ReturnDisplayApiCacheable}
import config.FrontendAppConfig
import connectors.{CalculateCreditsConnector, DownstreamServiceError, ServiceError, TaxReturnsConnector}
import controllers.actions.{DataRequiredActionImpl, FakeDataRetrievalAction}
import models.{CreditBalance, UserAnswers}
import models.returns.{Calculations, Credits, CreditsAnswer, CreditsClaimedDetails, NoCreditsClaimed, TaxReturnObligation}
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito.{reset, verify, verifyNoInteractions, when}
import org.mockito.MockitoSugar.mock
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import pages.returns.credits.{ConvertedCreditsPage, ExportedCreditsPage, WhatDoYouWantToDoPage}
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import repositories.SessionRepository
import viewmodels.govuk.SummaryListFluency
import views.html.returns.ReturnsCheckYourAnswersView

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReturnsCheckYourAnswersControllerSpec extends PlaySpec with SummaryListFluency with BeforeAndAfterEach {

  val taxReturnOb: TaxReturnObligation = TaxReturnObligation(
    LocalDate.parse("2022-04-01"),
    LocalDate.parse("2022-06-30"),
    LocalDate.parse("2022-06-30").plusWeeks(8),
    "00XX")

  val userAnswers = UserAnswers("123")
    .set(ObligationCacheable, taxReturnOb).get

  val calculations = Calculations(
    taxDue = 17,
    chargeableTotal = 85,
    deductionsTotal = 15,
    packagingTotal = 100,
    isSubmittable = true
  )

  val mockView = mock[ReturnsCheckYourAnswersView]
  val mockMessagesApi: MessagesApi = mock[MessagesApi]
  val controllerComponents = stubMessagesControllerComponents()
  val mockSessionRepository = mock[SessionRepository]
  val mockTaxReturnConnector = mock[TaxReturnsConnector]
  val mockCalculateCreditConnector = mock[CalculateCreditsConnector]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(
      mockSessionRepository,
      mockTaxReturnConnector,
      mockCalculateCreditConnector,
      mockView
    )

    when(mockView.apply(any(), any())(any(), any())).thenReturn(new Html(""))
  }

  "Returns Check Your Answers Controller" should {

    "return OK and the correct view for a GET" in {
      when(mockTaxReturnConnector.getCalculationReturns(any())(any())).thenReturn(Future.successful(
        Right(Calculations(taxDue = 17, chargeableTotal = 85, deductionsTotal = 15, packagingTotal = 100, isSubmittable = true)))
      )

      when(mockCalculateCreditConnector.get(any())(any()))
        .thenReturn(Future.successful(Right(CreditBalance(10, 20, 500L, true))))

      val result = createSut(Some(setUserAnswer)).onPageLoad()(FakeRequest(GET, "/foo"))
      status(result) mustEqual OK
      verify(mockView).apply(any(), any())(any(), any())
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val result = createSut(None).onPageLoad()(FakeRequest(GET, "/foo"))

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad.url
    }

    "call tax return API" in {
      setUpMockConnector(Right(calculations), Right(CreditBalance(10, 20, 500L, true)))

      val result = createSut(Some(setUserAnswer)).onPageLoad()(FakeRequest(GET, "/foo"))

      status(result) mustEqual OK
      verify(mockTaxReturnConnector).getCalculationReturns(ArgumentMatchers.eq("123"))(any())
    }

    "view claimed credits on pageLoading" in {
      setUpMockConnector(Right(calculations), Right(CreditBalance(10, 20, 500L, true)))

      val result = createSut(Some(setUserAnswer)).onPageLoad()(FakeRequest(GET, "/foo"))

      status(result) mustEqual OK
      verifyAndCaptorCreditDetails mustBe CreditsClaimedDetails(
        exported = CreditsAnswer(true, Some(200L)),
        converted = CreditsAnswer(true, Some(300L)),
        isClaimingTaxBack = true,
        totalWeight = 500L,
        totalCredits = 20L
      )
    }

    "handle credits no claimed on pageLoading" in {
      setUpMockConnector(Right(calculations), Right(CreditBalance(10, 20, 500L, true)))

      val ans = setUserAnswer.set(WhatDoYouWantToDoPage, false).get
      val result = createSut(Some(ans)).onPageLoad()(FakeRequest(GET, "/foo"))

      status(result) mustEqual OK
      verifyNoInteractions(mockCalculateCreditConnector)
      verifyAndCaptorCreditDetails mustBe NoCreditsClaimed
    }


    "must cache payment ref and redirect for a POST" in {
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
      when(mockTaxReturnConnector.submit(any())(any())).thenReturn(Future.successful(Right(Some("12345"))))

      val result = createSut(Some(setUserAnswer)).onSubmit()(FakeRequest(POST, "/foo"))

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.returns.routes.ReturnConfirmationController.onPageLoad().url
      verify(mockSessionRepository).set(any())
    }
  }

  "return an error" when {
    "cannot get credit" in {
      setUpMockConnector(Right(calculations), Left(new ServiceError("Credit Balance API error", new Exception("Credit Balance API error"))))

      intercept[RuntimeException] {
        await(createSut(Some(setUserAnswer)).onPageLoad()(FakeRequest(GET, "/foo")))
      }
    }

    "cannot get tax return calculation" in {
      setUpMockConnector(
        Left(new ServiceError("Tax return calculation error", new Exception("error"))),
        Right(CreditBalance(1,1,1, true)))
//
      intercept[RuntimeException] {
        await(createSut(Some(setUserAnswer)).onPageLoad()(FakeRequest(GET, "/foo")))
      }
    }

    "both api return an error" in {
      setUpMockConnector(Left(new ServiceError("Tax return calculation error", new Exception("error"))),
        Left(new ServiceError("Credit Balance API error", new Exception("Credit Balance API error")))
      )

      intercept[RuntimeException] {
        await(createSut(Some(setUserAnswer)).onPageLoad()(FakeRequest(GET, "/foo")))
      }
    }
  }

  private def setUpMockConnector(taxReturnConnectorResult: Either[ServiceError, Calculations],
                                 creditConnectorResult: Either[ServiceError, CreditBalance]): Unit = {
    when(mockTaxReturnConnector.getCalculationReturns(any())(any()))
      .thenReturn(Future.successful(taxReturnConnectorResult))

    when(mockCalculateCreditConnector.get(any())(any()))
      .thenReturn(Future.successful(creditConnectorResult))
  }

  private def verifyAndCaptorCreditDetails: Credits = {
    val captor = ArgumentCaptor.forClass(classOf[Credits])
    verify(mockView).apply(any(), captor.capture())(any(), any())
    captor.getValue
  }
  private def createSut(userAnswer: Option[UserAnswers]) = {
    new ReturnsCheckYourAnswersController(
      mockMessagesApi,
      new FakeIdentifierActionWithEnrolment(stubPlayBodyParsers(NoMaterializer)),
      new FakeDataRetrievalAction(userAnswer),
      new DataRequiredActionImpl(),
      mockTaxReturnConnector,
      mockCalculateCreditConnector,
      mockSessionRepository,
      controllerComponents,
      mockView
    )
  }

  def setUserAnswer: UserAnswers = {
    userAnswers
      .set(ExportedCreditsPage, CreditsAnswer(true, Some(200))).get
      .set(ConvertedCreditsPage, CreditsAnswer(true, Some(300L))).get
      .set(WhatDoYouWantToDoPage, true).get
  }

}
