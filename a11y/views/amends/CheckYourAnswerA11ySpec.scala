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

package views.amends

import base.ViewSpecBase
import models.amends.AmendSummaryRow
import models.returns.{AmendsCalculations, Calculations, TaxReturnObligation}
import uk.gov.hmrc.scalatestaccessibilitylinter.AccessibilityMatchers
import viewmodels.{PrintBigDecimal, PrintLong}
import views.html.amends.CheckYourAnswersView

import java.time.LocalDate

class CheckYourAnswerA11ySpec extends ViewSpecBase with AccessibilityMatchers{

  val page = inject[CheckYourAnswersView]
  val obligation: TaxReturnObligation = TaxReturnObligation(
    LocalDate.now(),
    LocalDate.now().plusWeeks(12),
    LocalDate.now().plusWeeks(16),
    "PK1")

  val amendsCalculations = AmendsCalculations(
    Calculations(12, 200L, 100L, 300L, true),
    Calculations(12, 200L, 150L, 350L, true)
  )
  val totalRows = createTotalPlasticTable
  val deductionsRows = createDeductionTable
  val calculations = createCalculationTable

  private def render(isSubmittable: Boolean, amendmentMade: Boolean): String = {
    page(obligation, totalRows, deductionsRows, calculations, amendmentMade)(request, messages).toString()
  }

  "pass accessibility checks" when {
    "no error" in {
      render(true, true) must passAccessibilityChecks
    }

    "calculation grater than accretion" in {
      render(false, true) must passAccessibilityChecks
    }
    "no amendments made" in {
      render(true, false)
    }
  }

  private def createTotalPlasticTable = {
    Seq(
      AmendSummaryRow(
        messages("amendManufacturedPlasticPackaging.checkYourAnswersLabel"),
        "200",
        Some("0"),
        Some("manufacture", controllers.amends.routes.AmendManufacturedPlasticPackagingController.onPageLoad().url)),
      AmendSummaryRow(messages("amendImportedPlasticPackaging.checkYourAnswersLabel"),
        "100",
        Some("0"),
        Some("import", controllers.amends.routes.AmendImportedPlasticPackagingController.onPageLoad().url)
      ),
      AmendSummaryRow(messages("AmendsCheckYourAnswers.packagingTotal"),
        amendsCalculations.original.deductionsTotal.asKg,
        Some(amendsCalculations.amend.deductionsTotal.asKg),
        None
      )
    )
  }

  private def createDeductionTable = {
    Seq(
      AmendSummaryRow(
        messages("amendDirectExportPlasticPackaging.checkYourAnswersLabel"),
        "20",
        Some("0"),
        Some("export", controllers.amends.routes.AmendDirectExportPlasticPackagingController.onPageLoad().url)
      ),
      AmendSummaryRow(
        messages("amendHumanMedicinePlasticPackaging.checkYourAnswersLabel"),
        "30",
        Some("0"),
        Some("medicine", controllers.amends.routes.AmendHumanMedicinePlasticPackagingController.onPageLoad().url)
      ),
      AmendSummaryRow(
        messages("amendRecycledPlasticPackaging.checkYourAnswersLabel"),
        "50",
        Some("0"),
        Some("recycled", controllers.amends.routes.AmendRecycledPlasticPackagingController.onPageLoad().url)
      ),
      AmendSummaryRow(
        messages("AmendsCheckYourAnswers.deductionsTotal"),
        amendsCalculations.original.deductionsTotal.asKg,
        Some(amendsCalculations.amend.deductionsTotal.asKg),
        None
      )
    )
  }

  private def createCalculationTable = {
    AmendsCalculations(
      Calculations(12, 40, 100, 200, true),
      Calculations(12, 40, 100, 200, true)
    )
  }
}
