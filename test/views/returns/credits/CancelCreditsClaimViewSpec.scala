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

package views.returns.credits

import base.ViewSpecBase
import forms.returns.credits.CancelCreditsClaimFormProvider
import models.returns.credits.SingleYearClaim
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar
import play.api.data.Form
import play.api.mvc.Call
import play.twirl.api.Html
import support.{ViewAssertions, ViewMatchers}
import views.html.returns.credits.CancelCreditsClaimView

class CancelCreditsClaimViewSpec extends ViewSpecBase with ViewAssertions with ViewMatchers with MockitoSugar {

  private val page = inject[CancelCreditsClaimView]
  private val form = new CancelCreditsClaimFormProvider()()

  private val singleYearClaim = mock[SingleYearClaim]

  private def createView(form: Form[Boolean]): Html = {
    when(singleYearClaim.toDateRangeString(any)) thenReturn "date-range-string"
    page(form, Call("POST", "call-url"), singleYearClaim)(request, messages)
  }

  "CancelCreditsClaimView" should {

    val view = createView(form)

    "have a title" in {
      view.select("title").text mustBe
        "Are you sure you want to remove this credit for date-range-string? - Submit return - Plastic Packaging Tax - GOV.UK"
      view.select("title").text must include(messages("cancelCreditsClaim.title-heading", "date-range-string"))
    }

    "have a heading" in {
      view.select("h1").text mustBe "Are you sure you want to remove this credit for date-range-string?"
      view.select("h1").text mustBe messages("cancelCreditsClaim.title-heading", "date-range-string")
    }

    "have a continue button" in {
      view.getElementsByClass("govuk-button").text() must include("Save and continue")
      view.getElementsByClass("govuk-button").text() must include(messages("site.continue"))
    }

    "error" when {
      "no option is selected" in {
        val errorForm = form.bind(Map("value" -> ""))
        createView(errorForm).getElementsByClass(
          "govuk-error-summary__list"
        ).text() mustBe "Select yes if you want to remove this credit"
        createView(errorForm).getElementsByClass("govuk-error-summary__list").text() mustBe messages(
          "cancelCreditsClaim.error.required"
        )
      }

      "display error summary box" in {
        val view = createView(form.withError("error key", "error message"))
        view.getElementsByClass("govuk-error-summary__title").text() mustBe "There is a problem"
        view.getElementsByClass("govuk-error-summary__title").text() mustBe messages("error.summary.title")
      }
    }
  }

}
