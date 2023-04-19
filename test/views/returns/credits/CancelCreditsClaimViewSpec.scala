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
import forms.returns.credits.CancelCreditsClaimFormProvider
import play.api.data.Form
import play.twirl.api.Html
import support.{ViewAssertions, ViewMatchers}
import views.html.returns.credits.CancelCreditsClaimView

class CancelCreditsClaimViewSpec extends ViewSpecBase with ViewAssertions with ViewMatchers {
  val page = inject[CancelCreditsClaimView]
  val form = new CancelCreditsClaimFormProvider()()

  private def createView(form: Form[Boolean]): Html = page(form)(request, messages)

  "CancelCreditsClaimView" should {
    val view = createView(form)
    "have a title" in {
      view.select("title").text mustBe
        "Are you sure you want to cancel this credit for 1 April 2022 to 31 March 2023? - Submit return - Plastic Packaging Tax - GOV.UK"
      view.select("title").text must include(messages("cancelCreditsClaim.title"))
    }

    "have a heading" in {
      view.select("h1").text mustBe "Are you sure you want to cancel this credit for 1 April 2022 to 31 March 2023?"
      view.select("h1").text mustBe messages("cancelCreditsClaim.heading")
    }

    "have a continue button" in {
      view.getElementsByClass("govuk-button").text() mustBe "Continue"
      view.getElementsByClass("govuk-button").text() mustBe messages("site.button.continue")
    }


    "error" when {
      "no option is selected" in {
        val errorForm = form.bind(Map("value" -> ""))
        createView(errorForm).getElementsByClass("govuk-error-summary__list").text() mustBe "Select yes if you want to cancel this credit"
        createView(errorForm).getElementsByClass("govuk-error-summary__list").text() mustBe messages("cancelCreditsClaim.error.required")
      }

      "display error summary box" in {
        val view = createView(form.withError("error key", "error message"))
        view.getElementsByClass("govuk-error-summary__title").text() mustBe "There is a problem"
        view.getElementsByClass("govuk-error-summary__title").text() mustBe messages("error.summary.title")
      }
    }
  }

}
