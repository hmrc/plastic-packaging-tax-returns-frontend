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

import org.apache.pekko.stream.testkit.NoMaterializer
import base.FakeIdentifierActionWithEnrolment
import cacheables.ReturnObligationCacheable
import config.FrontendAppConfig
import connectors._
import controllers.actions.{DataRequiredActionImpl, FakeDataRetrievalAction}
import controllers.helpers.TaxReturnHelper
import models.returns.Credits.{NoCreditAvailable, NoCreditsClaimed}
import models.returns._
import models.returns.credits.CreditSummaryRow
import models.{CreditBalance, TaxablePlastic, UserAnswers}
import navigation.ReturnsJourneyNavigator
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchersSugar.{any, eqTo}
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.MockitoSugar.{mock, reset, verify, when}
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import pages.returns.credits.WhatDoYouWantToDoPage
import play.api.Logger
import play.api.http.Status
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.RequestHeader
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import repositories.ProcessingStatus.Complete
import repositories.{ProcessingEntry, ReturnsProcessingRepository, SessionRepository}
import repositories.SessionRepository.Paths
import viewmodels.govuk.SummaryListFluency
import views.html.returns.ReturnsCheckYourAnswersView

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReturnsCheckYourAnswersControllerSpec extends PlaySpec with SummaryListFluency with BeforeAndAfterEach {

  private val taxReturnObligation: TaxReturnObligation = TaxReturnObligation(
    LocalDate.parse("2022-04-01"),
    LocalDate.parse("2022-06-30"),
    LocalDate.parse("2022-06-30").plusWeeks(8),
    "00XX"
  )

  private val userAnswers = UserAnswers("123")
    .set(ReturnObligationCacheable, taxReturnObligation).get

  private val calculations = Calculations(
    taxDue = 17,
    chargeableTotal = 85,
    deductionsTotal = 15,
    packagingTotal = 100,
    isSubmittable = true,
    taxRate = 200.0
  )

  private val mockView                                                              = mock[ReturnsCheckYourAnswersView]
  private val mockMessagesApi: MessagesApi                                          = mock[MessagesApi]
  private val message                                                               = mock[Messages]
  private val controllerComponents                                                  = stubMessagesControllerComponents()
  private implicit val mockSessionRepository: SessionRepository                     = mock[SessionRepository]
  private implicit val mockReturnsProcessingRepository: ReturnsProcessingRepository = mock[ReturnsProcessingRepository]
  private val mockTaxReturnConnector                                                = mock[TaxReturnsConnector]
  private val mockCalculateCreditConnector                                          = mock[CalculateCreditsConnector]
  private val cacheConnector                                                        = mock[CacheConnector]
  private val mockTaxReturnHelper                                                   = mock[TaxReturnHelper]
  private val appConfig                                                             = mock[FrontendAppConfig]
  private val navigator                                                             = mock[ReturnsJourneyNavigator]
  private val keyString = s"${LocalDate.now()}-${LocalDate.now()}"

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(
      mockSessionRepository,
      mockReturnsProcessingRepository,
      mockTaxReturnConnector,
      mockCalculateCreditConnector,
      mockTaxReturnHelper,
      mockView
    )

    when(mockMessagesApi.preferred(any[RequestHeader])).thenReturn(message)
    when(message.apply(any[String], any)).thenReturn("messages")
    when(mockView.apply(any, any, any)(any, any)).thenReturn(new Html(""))
    when(mockMessagesApi.preferred(any[RequestHeader])).thenReturn(message)
    when(mockSessionRepository.set(any, any, any)(any)).thenReturn(Future.successful(true))
    when(mockSessionRepository.get[Boolean](any, any)(any)).thenReturn(Future.successful(Some(false)))
    when(mockReturnsProcessingRepository.set(any)).thenReturn(Future.successful(()))

    when(mockCalculateCreditConnector.getEventually(any)(any)) thenReturn Future.successful(
      CreditBalance(10, 20, 500L, true, Map(keyString -> TaxablePlastic(1, 2, 0.30)))
    )
  }

  "Returns Check Your Answers Controller" should {

    "return OK and the correct view for a GET" in {
      setUpMockConnector()
      when(message.apply(any[String])).thenAnswer((s: String) => s)

      val result = createSut(Some(setUserAnswer())).onPageLoad()(FakeRequest(GET, "/foo"))
      status(result) mustEqual OK
      verify(mockView).apply(any, any, any)(any, any)
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val result = createSut(None).onPageLoad()(FakeRequest(GET, "/foo"))

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad.url
    }

    "call tax return API" in {
      setUpMockConnector()

      val result = createSut(Some(setUserAnswer())).onPageLoad()(FakeRequest(GET, "/foo"))

      status(result) mustEqual OK
      verify(mockTaxReturnConnector).getCalculationReturns(ArgumentMatchers.eq("123"))(any)
    }

    "view claimed credits on pageLoading" in {
      setUpMockConnector()
      when(message.apply(anyString(), any, any, any)).thenReturn("any-key")
      when(message.apply(anyString())).thenReturn("total-key")

      val result = createSut(Some(setUserAnswer())).onPageLoad()(FakeRequest(GET, "/foo"))
      status(result) mustEqual OK
      verifyAndCaptorCreditDetails mustBe CreditsClaimedDetails(
        summaryList = Seq(
          CreditSummaryRow("any-key", "£2.00", Seq()),
          CreditSummaryRow("total-key", "£20.00", Seq())
        ),
        totalClaimAmount = 20
      )
    }

    "handle credits no claimed on pageLoading" in {
      setUpMockConnector()

      val ans    = setUserAnswer().set(WhatDoYouWantToDoPage, false).get
      val result = createSut(Some(ans)).onPageLoad()(FakeRequest(GET, "/foo"))

      status(result) mustEqual OK
      verifyNoInteractions(mockCalculateCreditConnector)
      verifyAndCaptorCreditDetails mustBe NoCreditsClaimed
    }

    "handle claim-credit yes-no answer missing" in {
      setUpMockConnector()

      val ans    = setUserAnswer().remove(WhatDoYouWantToDoPage).get
      val result = createSut(Some(ans)).onPageLoad()(FakeRequest(GET, "/foo"))

      status(result) mustEqual Status.SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.IndexController.onPageLoad.url)
    }

    "handle fist return for credits on pageLoading" in {
      setUpMockConnector(isFirstReturnResult = true)

      val result = createSut(Some(userAnswers)).onPageLoad()(FakeRequest(GET, "/foo"))

      status(result) mustEqual OK
      verifyAndCaptorCreditDetails mustBe NoCreditAvailable
    }

    "must cache payment ref and redirect for a POST" in {
      when(mockSessionRepository.set(any, any, any)(any)).thenReturn(Future.successful(true))
      when(mockTaxReturnConnector.submit(any)(any)).thenReturn(Future.successful(Right(Some("12345"))))

      val result = createSut(Some(setUserAnswer())).onSubmit()(FakeRequest(POST, "/foo"))

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.returns.routes.ReturnsProcessingController.onPageLoad(
        true
      ).url
      verify(mockSessionRepository).set(
        any,
        ArgumentMatchers.eq(Paths.ReturnChargeRef),
        ArgumentMatchers.eq(Some("12345"))
      )(any)
    }
  }

  "return an error" when {
    "cannot get credit" in {
      when(mockCalculateCreditConnector.getEventually(any)(any)) thenReturn Future.failed(
        DownstreamServiceError("Credit Balance API error", new Exception("Credit Balance API error"))
      )
      setUpMockConnector(taxReturnConnectorResult = Right(calculations))

      intercept[RuntimeException] {
        await(createSut(Some(setUserAnswer())).onPageLoad()(FakeRequest(GET, "/foo")))
      }
    }

    "cannot get tax return calculation" in {
      setUpMockConnector(taxReturnConnectorResult =
        Left(
          DownstreamServiceError("Tax return calculation error", new Exception("error"))
        )
      )
      intercept[RuntimeException] {
        await(createSut(Some(setUserAnswer())).onPageLoad()(FakeRequest(GET, "/foo")))
      }
    }

    "nextOpenObligationAndIfFirst return an error" in {
      setUpMockConnector()
      when(mockTaxReturnHelper.nextOpenObligationAndIfFirst(any)(any)).thenThrow(new RuntimeException("Error"))

      val userAnswers = setUserAnswer().remove(WhatDoYouWantToDoPage).get
      intercept[RuntimeException] {
        await(createSut(Some(userAnswers)).onPageLoad()(FakeRequest(GET, "/foo")))
      }
    }

    "all api return an error" in {
      when(mockCalculateCreditConnector.getEventually(any)(any)) thenReturn Future.failed(
        DownstreamServiceError("Credit Balance API error", new Exception("Credit Balance API error"))
      )
      setUpMockConnector(
        taxReturnConnectorResult = Left(DownstreamServiceError("Tax return calculation error", new Exception("error")))
      )
      when(mockTaxReturnHelper.nextOpenObligationAndIfFirst(any)(any)).thenThrow(new RuntimeException("Error"))

      intercept[RuntimeException] {
        await(createSut(Some(setUserAnswer())).onPageLoad()(FakeRequest(GET, "/foo")))
      }
    }
  }

  "navigate" should {

    val controller = createSut(Some(setUserAnswer()))
    "link to change credit answers" in {
      when(navigator.cyaChangeCredits) thenReturn "/change-me"
      setUpMockConnector()
      await {
        controller.onPageLoad()(FakeRequest(GET, "/foo"))
      }
      verify(mockView).apply(any, any, eqTo("/change-me"))(any, any)
    }
  }

  private def setUpMockConnector(
    taxReturnConnectorResult: Either[ServiceError, Calculations] = Right(calculations),
    isFirstReturnResult: Boolean = false
  ): Unit = {
    when(mockTaxReturnConnector.getCalculationReturns(any)(any)) thenReturn Future.successful(taxReturnConnectorResult)
    when(mockTaxReturnHelper.nextOpenObligationAndIfFirst(any)(any)) thenReturn Future.successful(
      Some((taxReturnObligation, isFirstReturnResult))
    )
  }

  private def verifyAndCaptorCreditDetails: Credits = {
    val captor = ArgumentCaptor.forClass(classOf[Credits])
    verify(mockView).apply(any, captor.capture(), any)(any, any)
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
      mockReturnsProcessingRepository,
      controllerComponents,
      appConfig,
      mockView,
      cacheConnector,
      navigator
    ) {
      override protected val logger: Logger = mock[Logger]
    }
  }

  private def setUserAnswer(): UserAnswers =
    UserAnswers(
      "123",
      Json.parse(s"""
        {
        |   "obligation":{
        |      "fromDate":"2022-04-01",
        |      "toDate":"2022-06-30",
        |      "dueDate":"2022-08-25",
        |      "periodKey":"00XX"
        |   },
        |   "credit":{
        |      "$keyString":{
        |         "fromDate": "2022-04-01",
        |         "toDate" : "2023-03-31",
        |         "exportedCredits":{
        |            "yesNo":true,
        |            "weight":200
        |         },
        |         "convertedCredits":{
        |            "yesNo":true,
        |            "weight":300
        |         }
        |      }
        |   },
        |   "whatDoYouWantToDo":true
        |}
        |""".stripMargin).as[JsObject]
    )

}
