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

package controllers.amends

import base.utils.JourneyActionAnswer
import cacheables.AmendObligationCacheable
import connectors.{DownstreamServiceError, TaxReturnsConnector}
import controllers.BetterMockActionSyntax
import controllers.actions.JourneyAction
import models.amends.AmendNewAnswerType.AnswerWithValue
import models.amends.{AmendNewAnswerType, AmendSummaryRow}
import models.requests.DataRequest
import models.returns.{AmendsCalculations, Calculations}
import org.mockito.Answers
import org.mockito.ArgumentMatchers.{eq => meq}
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar.{mock, reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures._
import org.scalatestplus.play.PlaySpec
import pages.amends._
import play.api.i18n.MessagesApi
import play.api.libs.json.{JsPath, JsResultException, JsonValidationError}
import play.api.mvc.{Action, AnyContent}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import repositories.{ReturnsProcessingRepository, SessionRepository}
import repositories.SessionRepository.Paths
import services.AmendReturnAnswerComparisonService
import support.AmendExportedData
import util.EdgeOfSystem
import viewmodels.PrintLong
import viewmodels.govuk.SummaryListFluency
import views.html.amends.CheckYourAnswersView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

class CheckYourAnswersControllerSpec
    extends PlaySpec
    with SummaryListFluency
    with JourneyActionAnswer
    with AmendExportedData
    with BeforeAndAfterEach {

  val expectedHtml: Html = Html("correct view")

  private val dataRequest                         = mock[DataRequest[AnyContent]](Answers.RETURNS_DEEP_STUBS)
  private val messagesApi                         = mock[MessagesApi]
  private val journeyAction                       = mock[JourneyAction]
  private val returnsConnector                    = mock[TaxReturnsConnector]
  private val comparisonService                   = mock[AmendReturnAnswerComparisonService]
  private val sessionRepository                   = mock[SessionRepository]
  private val returnsProcessingRepository         = mock[ReturnsProcessingRepository]
  private val view                                = mock[CheckYourAnswersView]
  private implicit val edgeOfSystem: EdgeOfSystem = mock[EdgeOfSystem]

  private def sut = new CheckYourAnswersController(
    messagesApi,
    journeyAction,
    returnsConnector,
    comparisonService,
    stubMessagesControllerComponents(),
    sessionRepository,
    returnsProcessingRepository,
    view
  )

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(
      messagesApi,
      journeyAction,
      returnsConnector,
      comparisonService,
      sessionRepository,
      returnsProcessingRepository,
      view,
      dataRequest
    )

    when(view.apply(any, any, any, any, any)(any, any)).thenReturn(expectedHtml)
    when(journeyAction.apply(any)).thenAnswer(byConvertingFunctionArgumentsToAction)
    when(journeyAction.async(any)).thenAnswer(byConvertingFunctionArgumentsToFutureAction)
    when(edgeOfSystem.localDateTimeNow).thenReturn(taxReturnOb.dueDate.atStartOfDay())
  }

  "onPageLoad" should {
    "use the journey action" in {
      when(journeyAction.async(any)) thenReturn mock[Action[AnyContent]]
      Try(await(sut.onPageLoad()(FakeRequest())))
      verify(journeyAction).async(any)
    }

    "return 200" in {
      val calc = Calculations(1, 2, 3, 4, true, 200.0)
      when(returnsConnector.getCalculationAmends(any)(any)).thenReturn(
        Future.successful(Right(AmendsCalculations(calc, calc)))
      )
      when(dataRequest.userAnswers).thenReturn(createUserAnswerWithData)

      val result = sut.onPageLoad()(dataRequest)

      status(result) mustEqual OK
    }

    "return view" in {
      val calc = Calculations(1, 2, 3, 4, true, 200.0)
      when(returnsConnector.getCalculationAmends(any)(any)).thenReturn(
        Future.successful(Right(AmendsCalculations(calc, calc)))
      )
      when(dataRequest.userAnswers).thenReturn(createUserAnswerWithData)
      when(comparisonService.hasMadeChangesOnAmend(any)).thenReturn(true)

      await(sut.onPageLoad()(dataRequest))

      verify(view).apply(
        meq(taxReturnOb),
        meq(createExpectedTotalRows),
        meq(createExpectedDeductionRows),
        meq(createExpectedCalculationsRow),
        meq(true)
      )(any, any)
    }

    "redirect to submitted-return page if already submitted" in {
      when(dataRequest.userAnswers).thenReturn(createUserAnswers.remove(AmendObligationCacheable).get)

      val result = sut.onPageLoad()(dataRequest)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result) mustBe Some(routes.SubmittedReturnsController.onPageLoad().url)
    }

    "throw an error if cannot calculate amends" in {
      when(dataRequest.userAnswers).thenReturn(createUserAnswerWithData)
      when(returnsConnector.getCalculationAmends(any)(any))
        .thenReturn(
          Future.successful(Left(DownstreamServiceError("Calculation Error", new Exception("Calculation Exception"))))
        )

      intercept[Exception] {
        await(sut.onPageLoad()(dataRequest))
      }
    }
  }

  "onSubmit" should {
    "use the journey action" in {
      when(journeyAction.async(any)) thenReturn mock[Action[AnyContent]]
      Try(await(sut.onSubmit()(FakeRequest())))
      verify(journeyAction).async(any)
    }

    "submit the amendment" in {
      when(dataRequest.pptReference).thenReturn("pptReference")
      when(returnsConnector.amend(any)(any)).thenReturn(Future.successful(Some("12345")))
      when(sessionRepository.set(any, any, any)(any)).thenReturn(Future.successful(true))
      when(returnsProcessingRepository.set(any)).thenReturn(Future.successful(()))
      await(sut.onSubmit().skippingJourneyAction(dataRequest))

      verify(returnsConnector).amend(meq("pptReference"))(any)
    }

    "save request response to repository" in {
      when(dataRequest.pptReference).thenReturn("pptReference")
      when(dataRequest.cacheKey).thenReturn("cacheKey")
      when(returnsConnector.amend(any)(any)).thenReturn(Future.successful(Some("chargeRef")))
      when(sessionRepository.set(any, any, any)(any)).thenReturn(Future.successful(true))
      when(returnsProcessingRepository.set(any)).thenReturn(Future.successful(()))
      await(sut.onSubmit().skippingJourneyAction(dataRequest))

      whenReady(sessionRepository.set(any, any, any)(any)) { _ =>
        verify(sessionRepository).set(meq("cacheKey"), meq(Paths.AmendChargeRef), meq(Some("chargeRef")))(any)
      }
    }

    "redirect to amend-processing page" in {
      when(dataRequest.pptReference).thenReturn("pptReference")
      when(dataRequest.cacheKey).thenReturn("cacheKey")
      when(returnsConnector.amend(any)(any)).thenReturn(Future.successful(Some("chargeRef")))
      when(sessionRepository.set(any, any, any)(any)).thenReturn(Future.successful(true))
      when(returnsProcessingRepository.set(any)).thenReturn(Future.successful(()))
      val result = sut.onSubmit().skippingJourneyAction(dataRequest)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result) mustBe Some(routes.AmendProcessingController.onPageLoad().url)
    }

    "redirect to amend-processing page if cannot submit amend" in {
      when(dataRequest.pptReference).thenReturn("pptReference")
      when(returnsConnector.amend(any)(any)).thenReturn(Future.failed(new Exception("error")))
      when(returnsProcessingRepository.set(any)).thenReturn(Future.successful(()))
      val result = sut.onSubmit().skippingJourneyAction(dataRequest)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result) mustBe Some(routes.AmendProcessingController.onPageLoad().url)
    }

    "redirect to amend-processing page if cannot save to repository " in {
      when(dataRequest.pptReference).thenReturn("pptReference")
      when(dataRequest.cacheKey).thenReturn("cacheKey")
      when(returnsConnector.amend(any)(any)).thenReturn(Future.successful(Some("chargeRef")))
      when(sessionRepository.set(any, any, any)(any))
        .thenReturn(Future.failed(JsResultException(Seq((JsPath, Seq(JsonValidationError("error", "error")))))))
      when(returnsProcessingRepository.set(any)).thenReturn(Future.successful(()))
      val result = sut.onSubmit().skippingJourneyAction(dataRequest)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result) mustBe Some(routes.AmendProcessingController.onPageLoad().url)
    }
  }

  private def createExpectedCalculationsRow =
    AmendsCalculations(
      Calculations(1, 2, 3, 4, true, 200.0),
      Calculations(1, 2, 3, 4, true, 200.0)
    )

  private def createExpectedDeductionRows = {
    Seq(
      AmendSummaryRow(
        "amendDirectExportPlasticPackaging.checkYourAnswersLabel",
        "4kg",
        AmendNewAnswerType(Some("70kg"), "AmendsCheckYourAnswers.hiddenCell.newAnswer.1"),
        Some(("export", controllers.amends.routes.AmendExportedPlasticPackagingController.onPageLoad.url))
      ),
      AmendSummaryRow(
        "amendHumanMedicinePlasticPackaging.checkYourAnswersLabel",
        "3kg",
        AmendNewAnswerType(Some("30kg"), "AmendsCheckYourAnswers.hiddenCell.newAnswer.1"),
        Some(("medicine", controllers.amends.routes.AmendHumanMedicinePlasticPackagingController.onPageLoad().url))
      ),
      AmendSummaryRow(
        "amendRecycledPlasticPackaging.checkYourAnswersLabel",
        "5kg",
        AmendNewAnswerType(Some("20kg"), "AmendsCheckYourAnswers.hiddenCell.newAnswer.1"),
        Some(("recycled", controllers.amends.routes.AmendRecycledPlasticPackagingController.onPageLoad().url))
      ),
      totalRow(3, 3, "AmendsCheckYourAnswers.deductionsTotal")
    )
  }

  private def createExpectedTotalRows = {
    Seq(
      AmendSummaryRow(
        "amendManufacturedPlasticPackaging.checkYourAnswersLabel",
        "0kg",
        AmendNewAnswerType(Some("300kg"), "AmendsCheckYourAnswers.hiddenCell.newAnswer.1"),
        Some(("manufacture", controllers.amends.routes.AmendManufacturedPlasticPackagingController.onPageLoad().url))
      ),
      AmendSummaryRow(
        "amendImportedPlasticPackaging.checkYourAnswersLabel",
        "1kg",
        AmendNewAnswerType(Some("200kg"), "AmendsCheckYourAnswers.hiddenCell.newAnswer.1"),
        Some(("import", controllers.amends.routes.AmendImportedPlasticPackagingController.onPageLoad().url))
      ),
      totalRow(4, 4, "AmendsCheckYourAnswers.packagingTotal")
    )
  }

  private def createUserAnswerWithData = {
    createUserAnswers
      .set(AmendManufacturedPlasticPackagingPage, 300L).get
      .set(AmendImportedPlasticPackagingPage, 200L).get
      .set(AmendDirectExportPlasticPackagingPage, 50L, true).get
      .set(AmendExportedByAnotherBusinessPage, 20L).get
      .set(AmendHumanMedicinePlasticPackagingPage, 30L).get
      .set(AmendRecycledPlasticPackagingPage, 20L).get
  }

  private def totalRow(originalTotal: Long, amendedTotal: Long, key: String) = {
    AmendSummaryRow(
      key,
      originalTotal.asKg,
      AnswerWithValue(amendedTotal.asKg),
      None
    )
  }
}
