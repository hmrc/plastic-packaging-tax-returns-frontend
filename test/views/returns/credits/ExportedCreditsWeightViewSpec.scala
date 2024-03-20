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

package views.returns.credits

import base.ViewSpecBase
import forms.returns.credits.ExportedCreditsWeightFormProvider
import models.Mode.NormalMode
import models.returns.CreditRangeOption
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import play.twirl.api.Html
import support.{ViewAssertions, ViewMatchers}
import views.html.returns.credits.ExportedCreditsWeightView

import java.time.LocalDate

class ExportedCreditsWeightViewSpec extends ViewSpecBase with ViewAssertions with ViewMatchers {

  val page: ExportedCreditsWeightView      = inject[ExportedCreditsWeightView]
  val form                                 = new ExportedCreditsWeightFormProvider()()
  val creditRangeOption: CreditRangeOption = CreditRangeOption(LocalDate.of(2023, 4, 1), LocalDate.of(2024, 3, 31))

  private def createView(form: Form[Long]): Html =
    page(form, "year-key", NormalMode, creditRangeOption)(request, messages)

  "view" should {
    val view = createView(form)
    "have title" in {
      view.select(
        "title"
      ).text() mustBe "How much plastic packaging did you pay tax on before it was exported? - Submit return - Plastic Packaging Tax - GOV.UK"
      view.select("title").text() must include(messages("exportedCreditsWeight.heading"))
    }

    "have a header" in {
      view.select("h1").text() mustBe "How much plastic packaging did you pay tax on before it was exported?"
      view.select("h1").text() must include(messages("exportedCreditsWeight.heading"))
    }

    "have a caption" in {
      view.getElementById("section-header").text() mustBe "Credit for 1 April 2023 to 31 March 2024"
      view.getElementById("section-header").text() mustBe messages("credits.caption", "1 April 2023 to 31 March 2024")
    }

    "have a hint" in {
      val doc: Document = Jsoup.parse(view.toString())

      doc.getElementById("value-hint").text mustBe "Enter the weight in kilograms. 1 tonne is 1,000kg."
      doc.getElementById("value-hint").text mustBe messages("exportedCreditsWeight.hint")
    }

    "contain save & continue button" in {
      view.getElementsByClass("govuk-button").text() must include("Save and continue")
      view.getElementsByClass("govuk-button").text() must include(messages("site.continue"))
    }

    "show an error message" when {

      "display an error summary box" in {
        val view = createView(form.withError("error key", "error message"))

        view.getElementsByClass("govuk-error-summary__title").text() mustBe "There is a problem"
        view.getElementsByClass("govuk-error-summary__title").text() mustBe messages("error.summary.title")
        view.getElementsByClass("govuk-error-summary__list").get(0).text() mustBe "error message"
      }

      "no input" in {
        val view = createView(form.bind(Map("value" -> "")))

        view.getElementsByClass("govuk-error-summary__list").get(0).text() mustBe "Enter the weight, in kilograms"
      }

      "letter with no numbers" in {
        val view = createView(form.bind(Map("value" -> "asad")))

        view.getElementsByClass("govuk-error-summary__list").get(0).text() mustBe "Weight must be entered as numbers"
      }

      "weight surrounding letter" in {
        val view = createView(form.bind(Map("value" -> "25HF23")))

        view.getElementsByClass("govuk-error-summary__list").get(0).text() mustBe "Weight must be entered as numbers"
      }

      "weight less than 0" in {
        val view = createView(form.bind(Map("value" -> "-1")))

        view.getElementsByClass("govuk-error-summary__list").get(
          0
        ).text() mustBe "Weight must be between 0kg and 99,999,999,999kg"
      }

      "weight greater than 99999999999Kg" in {
        val view = createView(form.bind(Map("value" -> "999999999999")))

        view.getElementsByClass("govuk-error-summary__list").get(
          0
        ).text() mustBe "Weight must be between 0kg and 99,999,999,999kg"
      }

      "weight is a decimal number" in {
        val view = createView(form.bind(Map("value" -> "2.5")))

        view.getElementsByClass("govuk-error-summary__list").get(0).text() mustBe "Weight must not include decimals"
      }
    }
  }
}
