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

package controllers.amends

import base.SpecBase
import cacheables.{AmendObligationCacheable, ReturnDisplayApiCacheable}
import config.FrontendAppConfig
import connectors.TaxReturnsConnector
import models.Mode.NormalMode
import models.UserAnswers
import models.amends.AmendSummaryRow
import models.returns.{AmendsCalculations, Calculations}
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.Mockito.{verify, when}
import org.mockito.MockitoSugar.mock
import play.api.i18n.Messages
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Application, inject}
import play.twirl.api.{Html, HtmlFormat}
import viewmodels.PrintLong
import viewmodels.govuk.SummaryListFluency
import views.html.amends.CheckYourAnswersView

import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  override def userAnswers: UserAnswers = UserAnswers(userAnswersId)
    .set(ReturnDisplayApiCacheable, retDisApi).get
    .set(AmendObligationCacheable, taxReturnOb).get

  val mockView = mock[CheckYourAnswersView]
  val expectedHtml = Html("correct view")
  when(mockView.apply(any(), any(), any(), any(), any())(any(), any())).thenReturn(expectedHtml)

  override def applicationBuilder(userAnswers: Option[UserAnswers]): GuiceApplicationBuilder =
    super.applicationBuilder(userAnswers)
      .overrides(
        inject.bind[FrontendAppConfig].toInstance(config),
        inject.bind[TaxReturnsConnector].toInstance(mockTaxReturnConnector),
        inject.bind[CheckYourAnswersView].toInstance(mockView)
      )

  "(Amend journey) Check Your Answers Controller" - {

    "must redirect to account page when amends toggle is disabled" in{
      when(config.isAmendsFeatureEnabled).thenReturn(false)

      val application: Application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)

        val result: Future[Result] = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.IndexController.onPageLoad.url
         }
    }

    "must return OK and the correct view for a GET" in {
      when(config.isAmendsFeatureEnabled).thenReturn(true)
      when(config.userResearchUrl).thenReturn("some Url")

      val calc = Calculations(1, 2, 3, 4, true)

      when(mockTaxReturnConnector.getCalculationAmends(any())(any())).thenReturn(Future.successful(Right(AmendsCalculations(calc, calc))))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)

        val result = route(application, request).value

        val totalRows: Seq[AmendSummaryRow] = Seq(
          AmendSummaryRow(
            "amendManufacturedPlasticPackaging.checkYourAnswersLabel", "0kg", None,
            Some("manufacture", controllers.amends.routes.AmendManufacturedPlasticPackagingController.onPageLoad().url)
          ),
          AmendSummaryRow(
            "amendImportedPlasticPackaging.checkYourAnswersLabel", "1kg", None,
            Some("import", controllers.amends.routes.AmendImportedPlasticPackagingController.onPageLoad().url)
          ),
          totalRow(4, 4, "AmendsCheckYourAnswers.packagingTotal")(messages(application))
        )

        val deductionsRows: Seq[AmendSummaryRow] = Seq(
          AmendSummaryRow(
            "amendDirectExportPlasticPackaging.checkYourAnswersLabel", "4kg", None,
           Some("export", controllers.amends.routes.AmendDirectExportPlasticPackagingController.onPageLoad().url)
          ),
          AmendSummaryRow(
            "amendHumanMedicinePlasticPackaging.checkYourAnswersLabel", "3kg", None,
            Some("medicine", controllers.amends.routes.AmendHumanMedicinePlasticPackagingController.onPageLoad().url)
          ),
          AmendSummaryRow(
            "amendRecycledPlasticPackaging.checkYourAnswersLabel", "5kg", None,
            Some("recycled", controllers.amends.routes.AmendRecycledPlasticPackagingController.onPageLoad().url)
          ),
          totalRow(3, 3, "AmendsCheckYourAnswers.deductionsTotal")(messages(application))
        )

        val calculationsRows = AmendsCalculations(
          Calculations(1, 2, 3, 4, true),
          Calculations(1, 2, 3, 4, true)
        )

        status(result) mustEqual OK
        contentAsString(result) mustBe expectedHtml.toString()
        verify(mockView).apply(
          refEq(taxReturnOb),
          refEq(totalRows),
          refEq(deductionsRows),
          refEq(calculationsRows),any())(any(), any())

      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      when(config.isAmendsFeatureEnabled).thenReturn(true)

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad.url
      }
    }

    "must redirect when previous tax return is not in user answers" in {
      when(config.isAmendsFeatureEnabled).thenReturn(true)

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET,  routes.CheckYourAnswersController.onPageLoad.url)

        val result = route(application, request).value

        redirectLocation(result) mustBe Some(routes.SubmittedReturnsController.onPageLoad().url)
      }
    }
  }

  private def totalRow(originalTotal: Long, amendedTotal: Long, key: String)
                      (implicit messages: Messages) = {
    AmendSummaryRow(
      key,
      originalTotal.asKg,
      Some(amendedTotal.asKg),
      None
    )
  }
}
