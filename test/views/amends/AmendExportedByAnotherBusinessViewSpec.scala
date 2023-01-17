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
import forms.amends.AmendExportedByAnotherBusinessFormProvider
import play.api.data.Form
import play.twirl.api.Html
import support.ViewMatchers
import views.html.amends.AmendExportedByAnotherBusinessView

class AmendExportedByAnotherBusinessViewSpec extends ViewSpecBase with ViewMatchers {

  private val form: Form[Long] = new AmendExportedByAnotherBusinessFormProvider()()
  private val page = inject[AmendExportedByAnotherBusinessView]

  private def createView(form: Form[Long] = form): Html =
    page(form)(request, messages)

  "View" should {

    val view = createView()

    "have a title" in {
      view.select("title").text mustBe "How much of your finished plastic packaging components did another business export or convert? - Submit return - Plastic Packaging Tax - GOV.UK"
      view.select("title").text must include(messages("amendExportedByAnotherBusiness.title"))
    }

    "have a header" in {
      val headerText = view.select("h1").text()
      headerText mustBe "How much of your finished plastic packaging components did another business export or convert?"
      headerText mustBe messages("amendExportedByAnotherBusiness.heading")
    }

    "have a paragraph" in {
      val para = view.getElementsByClass("govuk-body").text()

      para must include("This will show as a deduction on your tax calculation.")
      para must include(messages("amendExportedByAnotherBusiness.paragraph.1"))

      para must include("You must have evidence that the export or conversion has taken place.")
      para must include(messages("amendExportedByAnotherBusiness.paragraph.2"))
    }

    "have a hint" in {
      val hintText = view.getElementById("value-hint").text()

      hintText mustBe "Enter the weight, in kilograms. 1 tonne is 1,000kg."
      hintText mustBe messages("amendExportedByAnotherBusiness.hint")
    }

    "have a 'Continue' button link" in {
      val buttonElement = view.getElementsByClass("govuk-button")

      buttonElement.text() mustBe "Save and continue"
      buttonElement.text() mustBe messages("site.continue")
    }

    "display an error summary box"  in {
      val view = createView(form.withError("error key", "error message"))

      view.getElementById("error-summary-title").text() mustBe "There is a problem"
      view.getElementById("error-summary-title").text() mustBe messages("error.summary.title")
      view.getElementsByClass("govuk-error-summary__list").get(0).text() mustBe "error message"
    }

    "show validation messages" when {
      "value is a letter with no number" in {
        val view = createView(form.bind(Map("value" -> "asasdf")))

        view.getElementsByClass("govuk-error-summary__list").get(0).text() mustBe "Weight must be entered as numbers"
      }

      "value surround by letters" in {
        val view = createView(form.bind(Map("value" -> "25nb6")))

        view.getElementsByClass("govuk-error-summary__list").get(0).text() mustBe "Weight must be entered as numbers"
      }

      "value not provided" in {
        val view = createView(form.bind(Map("value" -> "")))

        view.getElementsByClass("govuk-error-summary__list").get(0).text() mustBe "Enter the weight, in kilograms"
      }

      "value is less than 0" in {
        val view = createView(form.bind(Map("value" -> "-1")))

        view.getElementsByClass("govuk-error-summary__list").get(0).text() mustBe "Weight must be between 0kg and 99,999,999,999kg"
      }

      "value is greater than 99,999,999,999" in {
        val view = createView(form.bind(Map("value" -> "999999999999")))

        view.getElementsByClass("govuk-error-summary__list").get(0).text() mustBe "Weight must be between 0kg and 99,999,999,999kg"
      }

      "value is a decimal number" in {
        val view = createView(form.bind(Map("value" -> "10.5")))

        view.getElementsByClass("govuk-error-summary__list").get(0).text() mustBe "Weight must not include decimals"
      }
    }
  }

}
