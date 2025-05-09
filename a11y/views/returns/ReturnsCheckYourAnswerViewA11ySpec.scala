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

package views.returns

import base.ViewSpecBase
import models.returns.Credits.{NoCreditAvailable, NoCreditsClaimed}
import models.returns.credits.CreditSummaryRow
import models.returns.{Calculations, CreditsAnswer, CreditsClaimedDetails, TaxReturnObligation}
import models.{CreditBalance, UserAnswers}
import pages.returns._
import pages.returns.credits.{ConvertedCreditsPage, ExportedCreditsPage, WhatDoYouWantToDoPage}
import play.twirl.api.Html
import uk.gov.hmrc.scalatestaccessibilitylinter.AccessibilityMatchers
import viewmodels.TaxReturnViewModel
import views.html.returns.ReturnsCheckYourAnswersView

import java.time.LocalDate

class ReturnsCheckYourAnswerViewA11ySpec extends ViewSpecBase with AccessibilityMatchers {

  val page = inject[ReturnsCheckYourAnswersView]

  private val userAnswer = UserAnswers("123")
    .set(ManufacturedPlasticPackagingPage, false).get
    .set(ImportedPlasticPackagingPage, false).get
    .set(DirectlyExportedPage, false).get
    .set(AnotherBusinessExportedPage, false).get
    .set(NonExportedHumanMedicinesPlasticPackagingPage, false).get
    .set(NonExportedRecycledPlasticPackagingPage, false).get
    .set(ExportedCreditsPage("year-key"), CreditsAnswer.noClaim).get
    .set(ConvertedCreditsPage("year-key"), CreditsAnswer.answerWeightWith(1L)).get
    .set(WhatDoYouWantToDoPage, true).get

  private val aTaxObligation: TaxReturnObligation =
    TaxReturnObligation(LocalDate.now(), LocalDate.now().plusWeeks(12), LocalDate.now().plusWeeks(16), "PK1")

  def createViewModel(answers: UserAnswers): TaxReturnViewModel = {
    val calculations = Calculations(1, 2L, 3L, 5L, true, 0.2)
    TaxReturnViewModel(answers, "123", aTaxObligation, calculations)
  }

  val credits = CreditsClaimedDetails(
    summaryList = Seq(
      CreditSummaryRow("a-key", "£2.00", Seq()),
      CreditSummaryRow("Credit total [Use Key]", "£20.00", Seq())
    ),
    totalClaimAmount = 20
  )

  "view" should {
    "pass accessibility tests" when {
      "credits is claimed" ignore {
        def render: Html = page(
          createViewModel(userAnswer),
          CreditsClaimedDetails(userAnswer, CreditBalance(0, 0, 0L, true, Map())),
          "/change"
        )(request, messages)

        render.toString() must passAccessibilityChecks
      }

      "credits is not claimed" in {
        val ans = userAnswer.set(WhatDoYouWantToDoPage, false).get
        def render: Html = page(
          createViewModel(ans),
          NoCreditsClaimed,
          "/change"
        )(request, messages)

        render.toString() must passAccessibilityChecks
      }

      "first return" in {
        val ans = UserAnswers("123")
          .set(ManufacturedPlasticPackagingPage, false).get
          .set(ImportedPlasticPackagingPage, false).get
          .set(DirectlyExportedPage, false).get
          .set(AnotherBusinessExportedPage, false).get
          .set(NonExportedHumanMedicinesPlasticPackagingPage, false).get
          .set(NonExportedRecycledPlasticPackagingPage, false).get

        def render: Html = page(createViewModel(ans), NoCreditAvailable, "/change")(request, messages)

        render.toString() must passAccessibilityChecks
      }
    }
  }
}
