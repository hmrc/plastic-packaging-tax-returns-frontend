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

package views.amends

import base.ViewSpecBase
import models.amends.AmendNewAnswerType.{AnswerWithValue, AnswerWithoutValue}
import models.amends.AmendSummaryRow
import models.returns.{AmendsCalculations, Calculations, TaxReturnObligation}
import play.twirl.api.Html
import support.{ViewAssertions, ViewMatchers}
import views.html.amends.CheckYourAnswersView

import java.time.LocalDate

class CheckYourAnswersViewSpec extends ViewSpecBase with ViewAssertions with ViewMatchers {

  val page = inject[CheckYourAnswersView]
  val obligation: TaxReturnObligation =
    TaxReturnObligation(LocalDate.now(), LocalDate.now().plusWeeks(12), LocalDate.now().plusWeeks(16), "PK1")

  private def createView(calculation: AmendsCalculations, amendmentMade: Boolean): Html =
    page(obligation, Seq.empty, Seq.empty, calculation, amendmentMade)(request, messages)

  private def createViewWithDeduction(deductions: Seq[AmendSummaryRow], amendmentMade: Boolean): Html =
    page(obligation, Seq.empty, deductions, createCalculations(true), amendmentMade)(request, messages)

  "View" should {
    "not allow to submit return when deduction greater than accretion" in {
      val view = createView(calculation = createCalculations(false), amendmentMade = true)

      view.getElementsByClass("govuk-button").text() must not include "Submit amended return"
      view.getElementById("submit-amend-return-header-error").text() mustBe "Submitting your amended return"
      view.getElementById("submit-amend-return-header-error").text() mustBe messages(
        "AmendsCheckYourAnswers.error.heading"
      )
      view.getElementById(
        "submit-amend-return-error-line"
      ).text() mustBe "You cannot submit this amended return unless you change your answers. The weight of your total plastic packaging must be greater than, or equal to, the weight of your total deductions."
      view.getElementById("submit-amend-return-error-line").text() mustBe messages("AmendsCheckYourAnswers.error.line")
    }

    "not display send your amended return message when deduction greater than accretion" in {
      val view = createView(calculation = createCalculations(false), amendmentMade = true)

      view.getElementById("now-send-heading") mustBe null
      view.getElementById("now-send-paragraph") mustBe null
    }

    "display the Confirm and Continue button" in {
      createView(calculation = createCalculations(true), amendmentMade = true)
        .getElementsByClass("govuk-button").text() must include("Submit amended return")
    }

    "display send now message when deduction less equal then accretion" in {
      val view = createView(calculation = createCalculations(true), amendmentMade = true)

      view.getElementById("now-send-heading").text() mustBe "Now send your amended return"
      view.getElementById("now-send-heading").text() mustBe messages("AmendsCheckYourAnswers.nowSend.heading")
      view.getElementById(
        "now-send-paragraph"
      ).text() mustBe "By submitting this amended return you are confirming that, to the best of your knowledge, the details you are providing are correct."
      view.getElementById("now-send-paragraph").text() mustBe messages("AmendsCheckYourAnswers.nowSend.para")
    }

    "hide submit button and show message when no amendments have been made" in {
      val view = createView(createCalculations(true), false)

      view.text() must include("You cannot submit this amended return as you have not made any changes.")
    }

    "display deduction" when {
      "is amending" in {
        val view = createViewWithDeduction(deductions = createExpectedDeductionRows, amendmentMade = true)
        view.text() must include("exportedPlastic 4kg 70k")
        view.text() must include("humanMedicine 3kg 30kg")
        view.text() must include("recycledPlastic 5kg 20kg")
        view.text() must include("total 3kg 3kg")
      }

      "is not amending" in {
        val view = createViewWithDeduction(deductions = createExpectedDeductionRowsForNotAmended, amendmentMade = false)
        view.text() must include("exportedPlastic 4kg hidden text")
        view.text() must include("humanMedicine 3kg hidden text")
        view.text() must include("recycledPlastic 5kg hidden text")
        view.text() must include("total 3kg hidden field")
      }
    }
    "display calculation section" when {

      "display tax rate" in {
        val view = createView(calculation = createCalculations(false), amendmentMade = true)

        view.getElementsByClass("govuk-body").text() must include(
          "For this period, tax is charged at a rate of £300.00 per tonne."
        )
      }
      "amended" in {
        val view = createView(calculation = createCalculations(true), amendmentMade = true)
        view.text() must include("Plastic packaging total 200kg 200kg")
        view.text() must include("Deductions total 100kg 100kg")
        view.text() must include("Chargeable plastic packaging total 40kg 40kg")
        view.text() must include("Tax due on this return £12.00 £12.00")
      }

      "not amendment done" in {
        val view = createView(calculation = createCalculations(false), amendmentMade = false)

        view.text() must include("Plastic packaging total 200kg You cannot amend this field")
        view.text() must include("Deductions total 100kg You cannot amend this field")
        view.text() must include("Chargeable plastic packaging total 40kg You cannot amend this field")
        view.text() must include("Tax due on this return £12.00 You cannot amend this field")
      }

    }

    "display Credit header" in {
      val view = createView(calculation = createCalculations(true), amendmentMade = true)

      view.select("h2").text() must include("Credits")
      view.select("h2").text() must include(messages("AmendsCheckYourAnswers.credits.heading"))
    }

    "display credit message" in {
      val view = createView(calculation = createCalculations(true), amendmentMade = true)

      view.getElementsByClass("govuk-body").text() must include("You cannot amend credits.")
      view.getElementsByClass("govuk-body").text() must include(messages("AmendsCheckYourAnswers.credit.paragraph"))
    }
  }

  private def createCalculations(isSubmittable: Boolean) =
    AmendsCalculations(
      Calculations(12, 40, 100, 200, isSubmittable, 200.0),
      Calculations(12, 40, 100, 200, isSubmittable, 0.3)
    )

  private def createExpectedDeductionRows: Seq[AmendSummaryRow] =
    Seq(
      AmendSummaryRow("exportedPlastic", "4kg", AnswerWithValue("70kg"), Some(("export", "/url"))),
      AmendSummaryRow("humanMedicine", "3kg", AnswerWithValue("30kg"), Some(("medicine", "/url"))),
      AmendSummaryRow("recycledPlastic", "5kg", AnswerWithValue("20kg"), Some(("recycled", "/rycycled"))),
      AmendSummaryRow("total", "3kg", AnswerWithValue("3kg"), None)
    )

  private def createExpectedDeductionRowsForNotAmended: Seq[AmendSummaryRow] =
    Seq(
      AmendSummaryRow("exportedPlastic", "4kg", AnswerWithoutValue("hidden text"), Some(("export", "/url"))),
      AmendSummaryRow("humanMedicine", "3kg", AnswerWithoutValue("hidden text"), Some(("medicine", "/url"))),
      AmendSummaryRow("recycledPlastic", "5kg", AnswerWithoutValue("hidden text"), Some(("recycled", "/rycycled"))),
      AmendSummaryRow("total", "3kg", AnswerWithoutValue("hidden field"), None)
    )

}
