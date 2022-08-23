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

package views.amends

import base.ViewSpecBase
import models.returns.{AmendsCalculations, Calculations, TaxReturnObligation}
import play.twirl.api.Html
import support.{ViewAssertions, ViewMatchers}
import views.html.amends.CheckYourAnswersView

import java.time.LocalDate

class CheckYourAnswersViewSpec extends ViewSpecBase with ViewAssertions with ViewMatchers{

  val page = inject[CheckYourAnswersView]
  val obligation: TaxReturnObligation = TaxReturnObligation(
    LocalDate.now(),
    LocalDate.now().plusWeeks(12),
    LocalDate.now().plusWeeks(16),
    "PK1")


  private def createView(calculation: AmendsCalculations, amendmentMade: Boolean): Html = {
    page(obligation, Seq.empty, Seq.empty, calculation, amendmentMade)(request, messages)
  }
  "View" should {
    "not allow to submit return when deduction greater than accretion" in {
        val view = createView(createCalculations(false), true)

        view.getElementsByClass("govuk-button") mustBe empty
        view.getElementById("submit-amend-return-header-error").text() mustBe "Submitting your amended return"
        view.getElementById("submit-amend-return-header-error").text() mustBe messages("AmendsCheckYourAnswers.error.heading")
        view.getElementById("submit-amend-return-error-line").text() mustBe "You cannot submit this amended return unless you change your answers. The weight of your total plastic packaging must be greater than, or equal to, the weight of your total deductions."
        view.getElementById("submit-amend-return-error-line").text() mustBe messages("AmendsCheckYourAnswers.error.line")
      }

    "not display send your amended return message when deduction greater than accretion" in {
      val view = createView(createCalculations(false), true)

      view.getElementById("now-send-heading") mustBe null
      view.getElementById("now-send-paragraph") mustBe null
    }

    "display the Confirm and Continue button" in {
      createView(createCalculations(true), true)
        .getElementsByClass("govuk-button") must not be empty
    }

    "display send now message when deduction less equal then accretion" in {
      val view = createView(createCalculations(true), true)

      view.getElementById("now-send-heading").text() mustBe "Now send your amended return"
      view.getElementById("now-send-heading").text() mustBe messages("AmendsCheckYourAnswers.nowSend.heading")
      view.getElementById("now-send-paragraph").text() mustBe "By submitting this amended return you are confirming that, to the best of your knowledge, the details you are providing are correct."
      view.getElementById("now-send-paragraph").text() mustBe messages("AmendsCheckYourAnswers.nowSend.para")
    }

    "hide submit button and show message when no amendments have been made" in {
      val view = createView(createCalculations(true), false)

      view.text() must include("You cannot submit this amended return as you have not made any changes.")
    }
  }

  private def createCalculations(isSubmittable: Boolean) = {
    AmendsCalculations(
      Calculations(12, 40, 100, 200, isSubmittable),
      Calculations(12, 40, 100, 200, isSubmittable)
    )
  }

}
