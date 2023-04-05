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

import akka.stream.testkit.NoMaterializer
import base.FakeIdentifierActionWithEnrolment
import cacheables.ReturnObligationCacheable
import connectors.{CacheConnector, CalculateCreditsConnector, DownstreamServiceError, ServiceError, TaxReturnsConnector}
import config.FrontendAppConfig
import controllers.actions.{DataRequiredActionImpl, FakeDataRetrievalAction}
import controllers.helpers.TaxReturnHelper
import models.returns.Credits.{NoCreditAvailable, NoCreditsClaimed}
import models.returns._
import models.{CreditBalance, UserAnswers}
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito.{reset, verify, verifyNoInteractions, when}
import org.mockito.MockitoSugar.mock
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import pages.returns.credits.{ConvertedCreditsPage, ConvertedCreditsWeightPage, ExportedCreditsPage, ExportedCreditsWeightPage, WhatDoYouWantToDoPage}
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import repositories.SessionRepository
import repositories.SessionRepository.Paths
import viewmodels.govuk.SummaryListFluency
import views.html.returns.ReturnsCheckYourAnswersView

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReturnsCheckYourAnswersControllerSpec extends PlaySpec with SummaryListFluency with BeforeAndAfterEach {

  private val taxReturnOb: TaxReturnObligation = TaxReturnObligation(
    LocalDate.parse("2022-04-01"),
    LocalDate.parse("2022-06-30"),
    LocalDate.parse("2022-06-30").plusWeeks(8),
    "00XX")

  private val userAnswers = UserAnswers("123")
    .set(ReturnObligationCacheable, taxReturnOb).get

  private val calculations = Calculations(
    taxDue = 17,
    chargeableTotal = 85,
    deductionsTotal = 15,
    packagingTotal = 100,
    isSubmittable = true,
    taxRate = 200.0
  )

  private val mockView = mock[ReturnsCheckYourAnswersView]
  private val mockMessagesApi: MessagesApi = mock[MessagesApi]
  private val controllerComponents = stubMessagesControllerComponents()
  private val mockSessionRepository = mock[SessionRepository]
  private val mockTaxReturnConnector = mock[TaxReturnsConnector]
  private val mockCalculateCreditConnector = mock[CalculateCreditsConnector]
  private val cacheConnector = mock[CacheConnector]
  private val mockTaxReturnHelper = mock[TaxReturnHelper]
  private val appConfig = mock[FrontendAppConfig]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(
      mockSessionRepository,
      mockTaxReturnConnector,
      mockCalculateCreditConnector,
      mockTaxReturnHelper,
      mockView
    )

    when(mockView.apply(any(), any())(any(), any())).thenReturn(new Html(""))
    when(appConfig.isCreditsForReturnsFeatureEnabled).thenReturn(true)
  }

  "Returns Check Your Answers Controller" should {

    "return OK and the correct view for a GET" in {
      setUpMockConnector()

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
      setUpMockConnector()

      val result = createSut(Some(setUserAnswer)).onPageLoad()(FakeRequest(GET, "/foo"))

      status(result) mustEqual OK
      verify(mockTaxReturnConnector).getCalculationReturns(ArgumentMatchers.eq("123"))(any())
    }

    "view claimed credits on pageLoading" in {
      setUpMockConnector()

      val result = createSut(Some(setUserAnswer)).onPageLoad()(FakeRequest(GET, "/foo"))

      status(result) mustEqual OK
      verifyAndCaptorCreditDetails mustBe CreditsClaimedDetails(
        CreditsAnswer(true, Some(200L)),
        converted = CreditsAnswer(true, Some(300L)),
        totalWeight = 500L,
        totalCredits = 20L
      )
    }

    "handle credits no claimed on pageLoading" in {
      setUpMockConnector()

      val ans = setUserAnswer.set(WhatDoYouWantToDoPage, false).get
      val result = createSut(Some(ans)).onPageLoad()(FakeRequest(GET, "/foo"))

      status(result) mustEqual OK
      verifyNoInteractions(mockCalculateCreditConnector)
      verifyAndCaptorCreditDetails mustBe NoCreditsClaimed
    }


    "handle fist return for credits on pageLoading" in {
      setUpMockConnector(isFirstReturnResult = true)

      val result = createSut(Some(userAnswers)).onPageLoad()(FakeRequest(GET, "/foo"))

      status(result) mustEqual OK
      verifyAndCaptorCreditDetails mustBe NoCreditAvailable
    }


    "must cache payment ref and redirect for a POST" in {
      when(mockSessionRepository.set(any(), any(), any())(any())).thenReturn(Future.successful(true))
      when(mockTaxReturnConnector.submit(any())(any())).thenReturn(Future.successful(Right(Some("12345"))))

      val result = createSut(Some(setUserAnswer)).onSubmit()(FakeRequest(POST, "/foo"))

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.returns.routes.ReturnConfirmationController.onPageLoad(true).url
      verify(mockSessionRepository).set(any(), ArgumentMatchers.eq(Paths.ReturnChargeRef), ArgumentMatchers.eq(Some("12345")))(any())
    }
  }

  "show no credits section on page Load" in {
    setUpMockConnector()
    when(appConfig.isCreditsForReturnsFeatureEnabled).thenReturn(false)

    val result = createSut(Some(userAnswers)).onPageLoad()(FakeRequest(GET, "/foo"))

    status(result) mustEqual OK
    verify(mockTaxReturnConnector).getCalculationReturns(ArgumentMatchers.eq("123"))(any())
    verify(mockTaxReturnHelper).nextOpenObligationAndIfFirst(ArgumentMatchers.eq("123"))(any())
    verifyNoInteractions(mockCalculateCreditConnector)
    verifyAndCaptorCreditDetails mustBe NoCreditAvailable
  }

  "return an error" when {
    "cannot get credit" in {
      setUpMockConnector(
        taxReturnConnectorResult = Right(calculations),
        creditConnectorResult = Left(
          DownstreamServiceError("Credit Balance API error",
            new Exception("Credit Balance API error"))
        )
      )

      intercept[RuntimeException] {
        await(createSut(Some(setUserAnswer)).onPageLoad()(FakeRequest(GET, "/foo")))
      }
    }

    "cannot get tax return calculation" in {
      setUpMockConnector(
        taxReturnConnectorResult = Left(
          DownstreamServiceError("Tax return calculation error",
            new Exception("error"))
        )
      )

      intercept[RuntimeException] {
        await(createSut(Some(setUserAnswer)).onPageLoad()(FakeRequest(GET, "/foo")))
      }
    }

    "nextOpenObligationAndIfFirst return an error" in {
      setUpMockConnector()
      when(mockTaxReturnHelper.nextOpenObligationAndIfFirst(any())(any())).thenThrow(new RuntimeException("Error"))

      intercept[RuntimeException] {
        await(createSut(Some(setUserAnswer)).onPageLoad()(FakeRequest(GET, "/foo")))
      }
    }

    "all api return an error" in {
      setUpMockConnector(
        taxReturnConnectorResult = Left(DownstreamServiceError("Tax return calculation error", new Exception("error"))),
        creditConnectorResult = Left(DownstreamServiceError("Credit Balance API error", new Exception("Credit Balance API error")))
      )
      when(mockTaxReturnHelper.nextOpenObligationAndIfFirst(any())(any())).thenThrow(new RuntimeException("Error"))

      intercept[RuntimeException] {
        await(createSut(Some(setUserAnswer)).onPageLoad()(FakeRequest(GET, "/foo")))
      }
    }
  }

  private def setUpMockConnector(
    taxReturnConnectorResult: Either[ServiceError, Calculations] = Right(calculations),
    creditConnectorResult: Either[ServiceError, CreditBalance] = Right(CreditBalance(10, 20, 500L, true)),
    isFirstReturnResult: Boolean = false
  ): Unit = {
    when(mockTaxReturnConnector.getCalculationReturns(any())(any()))
      .thenReturn(Future.successful(taxReturnConnectorResult))

    when(mockCalculateCreditConnector.get(any())(any()))
      .thenReturn(Future.successful(creditConnectorResult))

    when(mockTaxReturnHelper.nextOpenObligationAndIfFirst(any())(any())).thenReturn(
      Future.successful(Some(taxReturnOb, isFirstReturnResult))
    )
  }

  private def verifyAndCaptorCreditDetails: Credits = {
    val captor = ArgumentCaptor.forClass(classOf[Credits])
    verify(mockView).apply(any(), captor.capture())(any(), any())
    captor.getValue
  }
  private def createSut(userAnswer: Option[UserAnswers]): ReturnsCheckYourAnswersController = {
    new ReturnsCheckYourAnswersController(
      mockMessagesApi,
      new FakeIdentifierActionWithEnrolment(stubPlayBodyParsers(NoMaterializer)),
      new FakeDataRetrievalAction(userAnswer),
      new DataRequiredActionImpl(),
      mockTaxReturnConnector,
      mockCalculateCreditConnector,
      mockTaxReturnHelper,
      mockSessionRepository,
      controllerComponents,
      appConfig,
      mockView,
      cacheConnector
    )
  }

  private def setUserAnswer: UserAnswers = {
    userAnswers
      .set(ExportedCreditsPage, true).get
      .set(ExportedCreditsWeightPage, 200L).get
      .set(ConvertedCreditsPage, true).get
      .set(ConvertedCreditsWeightPage, 300L).get
      .set(WhatDoYouWantToDoPage, true).get
  }

}
