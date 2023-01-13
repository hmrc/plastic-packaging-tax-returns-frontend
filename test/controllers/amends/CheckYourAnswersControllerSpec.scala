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

package controllers.amends

import base.utils.JourneyActionAnswer
import cacheables.AmendObligationCacheable
import config.FrontendAppConfig
import connectors.{DownstreamServiceError, TaxReturnsConnector}
import controllers.BetterMockActionSyntax
import controllers.actions.JourneyAction
import models.amends.AmendSummaryRow
import models.requests.DataRequest
import models.returns.{AmendsCalculations, Calculations}
import org.mockito.Answers
import org.mockito.ArgumentMatchers.{eq => meq}
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar.{mock, reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import pages.amends._
import play.api.i18n.MessagesApi
import play.api.libs.json.{JsPath, JsResultException, JsonValidationError}
import play.api.mvc.{Action, AnyContent}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import repositories.SessionRepository
import repositories.SessionRepository.Paths
import services.AmendReturnAnswerComparisonService
import support.AmendExportedData
import viewmodels.PrintLong
import viewmodels.govuk.SummaryListFluency
import views.html.amends.CheckYourAnswersView

import scala.concurrent.Future
import scala.util.Try

class CheckYourAnswersControllerSpec
  extends PlaySpec
    with SummaryListFluency
    with JourneyActionAnswer
    with AmendExportedData
    with BeforeAndAfterEach {

  val expectedHtml = Html("correct view")

  private val dataRequest    = mock[DataRequest[AnyContent]](Answers.RETURNS_DEEP_STUBS)
  private val messagesApi = mock[MessagesApi]
  private val journeyAction = mock[JourneyAction]
  private val returnsConnector = mock[TaxReturnsConnector]
  private val appConfig = mock[FrontendAppConfig]
  private val comparisonService = mock[AmendReturnAnswerComparisonService]
  private val sessionRepository = mock[SessionRepository]
  private val view = mock[CheckYourAnswersView]

  private val sut = new CheckYourAnswersController(
    messagesApi,
    journeyAction,
    returnsConnector,
    appConfig,
    comparisonService,
    stubMessagesControllerComponents(),
    sessionRepository,
    view
  )

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset( messagesApi,
      journeyAction,
      returnsConnector,
      appConfig,
      comparisonService,
      sessionRepository,
      view,
      dataRequest
    )

    when(appConfig.isAmendsFeatureEnabled).thenReturn(true)
    when(view.apply(any, any, any, any, any)(any, any)).thenReturn(expectedHtml)
    when(journeyAction.apply(any)).thenAnswer(byConvertingFunctionArgumentsToAction)
    when(journeyAction.async(any)).thenAnswer(byConvertingFunctionArgumentsToFutureAction)
  }


  "onPageLoad" should {
    "use the journey action" in {
      when(journeyAction.async(any)) thenReturn mock[Action[AnyContent]]
      Try(await(sut.onPageLoad()(FakeRequest())))
      verify(journeyAction).async(any)
    }

    "return 200" in {
      val calc = Calculations(1, 2, 3, 4, true)
      when(returnsConnector.getCalculationAmends(any)(any)).thenReturn(Future.successful(Right(AmendsCalculations(calc, calc))))
      when(dataRequest.userAnswers).thenReturn(createUserAnswerWithData)

      val result = sut.onPageLoad()(dataRequest)

      status(result) mustEqual OK
    }

    "return view" in {
      val calc = Calculations(1, 2, 3, 4, true)
      when(returnsConnector.getCalculationAmends(any)(any)).thenReturn(Future.successful(Right(AmendsCalculations(calc, calc))))
      when(dataRequest.userAnswers).thenReturn(createUserAnswerWithData)
      when(comparisonService.hasMadeChangesOnAmend(any)).thenReturn(true)

      await(sut.onPageLoad()(dataRequest))

      verify(view).apply(
        meq(taxReturnOb),
        meq(createExpectedTotalRows),
        meq(createExpectedDeductionRows),
        meq(createExpectedCalculationsRow),
        meq(true))(any,any)
    }

    "redirect to submitted-return page if already submitted" in {
      when(dataRequest.userAnswers).thenReturn(createUserAnswers.remove(AmendObligationCacheable).get)

      val result = sut.onPageLoad()(dataRequest)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result) mustEqual Some(routes.SubmittedReturnsController.onPageLoad().url)
    }

    "throw an error if cannot calculate amends" in {
      when(dataRequest.userAnswers).thenReturn(createUserAnswerWithData)
      when(returnsConnector.getCalculationAmends(any)(any))
        .thenReturn(Future.successful(Left(DownstreamServiceError("Calculation Error",
          new Exception("Calculation Exception")))))

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
      when(sessionRepository.set(any,any,any)(any)).thenReturn(Future.successful(true))

      await(sut.onSubmit().skippingJourneyAction(dataRequest))

      verify(returnsConnector).amend(meq("pptReference"))(any)
    }

    "save request response to repository" in {
      when(dataRequest.pptReference).thenReturn("pptReference")
      when(dataRequest.cacheKey).thenReturn("cacheKey")
      when(returnsConnector.amend(any)(any)).thenReturn(Future.successful(Some("chargeRef")))
      when(sessionRepository.set(any,any,any)(any)).thenReturn(Future.successful(true))

      await(sut.onSubmit().skippingJourneyAction(dataRequest))

      verify(sessionRepository).set(
        meq("cacheKey"),
        meq(Paths.AmendChargeRef),
        meq(Some("chargeRef")))(any)
    }

    "redirect to return-amended page" in {
      when(dataRequest.pptReference).thenReturn("pptReference")
      when(dataRequest.cacheKey).thenReturn("cacheKey")
      when(returnsConnector.amend(any)(any)).thenReturn(Future.successful(Some("chargeRef")))
      when(sessionRepository.set(any,any,any)(any)).thenReturn(Future.successful(true))

      val result = sut.onSubmit().skippingJourneyAction(dataRequest)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result) mustEqual Some(routes.AmendConfirmationController.onPageLoad().url)
    }

    "let it throw if cannot submit amend " in {
      when(dataRequest.pptReference).thenReturn("pptReference")
      when(returnsConnector.amend(any)(any)).thenReturn(Future.failed(new Exception("error")))

      intercept[Exception] {
       await(sut.onSubmit().skippingJourneyAction(dataRequest))
      }
    }

    "let it throw if cannot save to repository " in {
      when(dataRequest.pptReference).thenReturn("pptReference")
      when(dataRequest.cacheKey).thenReturn("cacheKey")
      when(returnsConnector.amend(any)(any)).thenReturn(Future.successful(Some("chargeRef")))
      when(sessionRepository.set(any,any,any)(any))
        .thenReturn(Future.failed(JsResultException(Seq((JsPath, Seq(JsonValidationError("error", "error")))))))

      intercept[Exception] {
        await(sut.onSubmit().skippingJourneyAction(dataRequest))
      }
    }
  }

  private def createExpectedCalculationsRow = {
    AmendsCalculations(
      Calculations(1, 2, 3, 4, true),
      Calculations(1, 2, 3, 4, true)
    )
  }

  private def createExpectedDeductionRows = {
    Seq(
      AmendSummaryRow(
        "amendDirectExportPlasticPackaging.checkYourAnswersLabel", "4kg", Some("70kg"),
        Some("export", controllers.amends.routes.AmendExportedPlasticPackagingController.onPageLoad.url)),
      AmendSummaryRow(
        "amendHumanMedicinePlasticPackaging.checkYourAnswersLabel", "3kg", Some("30kg"),
        Some("medicine", controllers.amends.routes.AmendHumanMedicinePlasticPackagingController.onPageLoad().url)),
      AmendSummaryRow(
        "amendRecycledPlasticPackaging.checkYourAnswersLabel", "5kg", Some("20kg"),
        Some("recycled", controllers.amends.routes.AmendRecycledPlasticPackagingController.onPageLoad().url)),
      totalRow(3, 3, "AmendsCheckYourAnswers.deductionsTotal"))
  }

  private def createExpectedTotalRows = {
    Seq(
      AmendSummaryRow(
        "amendManufacturedPlasticPackaging.checkYourAnswersLabel", "0kg", Some("300kg"),
        Some("manufacture", controllers.amends.routes.AmendManufacturedPlasticPackagingController.onPageLoad().url)),
      AmendSummaryRow(
        "amendImportedPlasticPackaging.checkYourAnswersLabel", "1kg", Some("200kg"),
        Some("import", controllers.amends.routes.AmendImportedPlasticPackagingController.onPageLoad().url)),
      totalRow(4, 4, "AmendsCheckYourAnswers.packagingTotal"))
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

//  "(Amend journey) Check Your Answers Controller" - {
//
//    "must redirect to account page when amends toggle is disabled" in{
//      when(config.isAmendsFeatureEnabled).thenReturn(false)
//
//      val application: Application = applicationBuilder(userAnswers = Some(userAnswers)).build()
//
//      running(application) {
//        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
//
//        val result: Future[Result] = route(application, request).value
//
//        status(result) mustEqual SEE_OTHER
//        redirectLocation(result).value mustEqual controllers.routes.IndexController.onPageLoad.url
//         }
//    }
//
//    "must return OK and the correct view for a GET" in {
//      when(config.isAmendsFeatureEnabled).thenReturn(true)
//      when(config.userResearchUrl).thenReturn("some Url")
//
//      val calc = Calculations(1, 2, 3, 4, true)
//
//      when(mockTaxReturnConnector.getCalculationAmends(any())(any())).thenReturn(Future.successful(Right(AmendsCalculations(calc, calc))))
//
//      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
//
//      running(application) {
//        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
//
//        val result = route(application, request).value
//
//        val totalRows: Seq[AmendSummaryRow] = Seq(
//          AmendSummaryRow(
//            "amendManufacturedPlasticPackaging.checkYourAnswersLabel", "0kg", None,
//            Some("manufacture", controllers.amends.routes.AmendManufacturedPlasticPackagingController.onPageLoad().url)
//          ),
//          AmendSummaryRow(
//            "amendImportedPlasticPackaging.checkYourAnswersLabel", "1kg", None,
//            Some("import", controllers.amends.routes.AmendImportedPlasticPackagingController.onPageLoad().url)
//          ),
//          totalRow(4, 4, "AmendsCheckYourAnswers.packagingTotal")(messages(application))
//        )
//
//        val deductionsRows: Seq[AmendSummaryRow] = Seq(
//          AmendSummaryRow(
//            "amendDirectExportPlasticPackaging.checkYourAnswersLabel", "4kg", None,
//           Some("export", controllers.amends.routes.AmendDirectExportPlasticPackagingController.onPageLoad().url)
//          ),
//          AmendSummaryRow(
//            "amendHumanMedicinePlasticPackaging.checkYourAnswersLabel", "3kg", None,
//            Some("medicine", controllers.amends.routes.AmendHumanMedicinePlasticPackagingController.onPageLoad().url)
//          ),
//          AmendSummaryRow(
//            "amendRecycledPlasticPackaging.checkYourAnswersLabel", "5kg", None,
//            Some("recycled", controllers.amends.routes.AmendRecycledPlasticPackagingController.onPageLoad().url)
//          ),
//          totalRow(3, 3, "AmendsCheckYourAnswers.deductionsTotal")(messages(application))
//        )
//
//        val calculationsRows = AmendsCalculations(
//          Calculations(1, 2, 3, 4, true),
//          Calculations(1, 2, 3, 4, true)
//        )
//
//        status(result) mustEqual OK
//        contentAsString(result) mustBe expectedHtml.toString()
//        verify(mockView).apply(
//          refEq(taxReturnOb),
//          refEq(totalRows),
//          refEq(deductionsRows),
//          refEq(calculationsRows),any())(any(), any())
//
//      }
//    }
//
//    "must redirect to Journey Recovery for a GET if no existing data is found" in {
//      when(config.isAmendsFeatureEnabled).thenReturn(true)
//
//      val application = applicationBuilder(userAnswers = None).build()
//
//      running(application) {
//        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
//
//        val result = route(application, request).value
//
//        status(result) mustEqual SEE_OTHER
//        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad.url
//      }
//    }
//
//    "must redirect when previous tax return is not in user answers" in {
//      when(config.isAmendsFeatureEnabled).thenReturn(true)
//
//      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
//
//      running(application) {
//        val request = FakeRequest(GET,  routes.CheckYourAnswersController.onPageLoad.url)
//
//        val result = route(application, request).value
//
//        redirectLocation(result) mustBe Some(routes.SubmittedReturnsController.onPageLoad().url)
//      }
//    }
//  }

  private def totalRow(originalTotal: Long, amendedTotal: Long, key: String) = {
    AmendSummaryRow(
      key,
      originalTotal.asKg,
      Some(amendedTotal.asKg),
      None
    )
  }
}
