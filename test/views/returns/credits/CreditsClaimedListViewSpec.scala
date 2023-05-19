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
import forms.returns.credits.CreditsClaimedListFormProvider
import models.Mode.NormalMode
import models.returns.credits.CreditSummaryRow
import play.api.data.Form
import play.twirl.api.Html
import support.ViewAssertions
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import views.html.returns.credits.CreditsClaimedListView

class CreditsClaimedListViewSpec extends ViewSpecBase with ViewAssertions{

  private val page = inject[CreditsClaimedListView]
  private val form = new CreditsClaimedListFormProvider()()

  private val rows = Seq(
    CreditSummaryRow(
      label = "exported",
      value = "answer",
      actions = Seq(
        ActionItem("/foo", Text("change")),
        ActionItem("/remove", Text("remove"))
      )
    ),
    CreditSummaryRow(
      label = "exported",
      value = "answer"
    )
  )
  private def createView(form: Form[_]): Html = page(form, true, true,rows, NormalMode)(request, messages)

  "View" should {

    "have a header" in {
      createView(form).select("h1").text mustBe messages("creditsSummary.title-heading")
    }
    "show a claimed credit" in {
      createView(form).getElementsByClass("govuk-table__row").size() must be  > 0
    }

    "not show change/remove link" in {
      createView(form).getElementsByClass("govuk-table__row")
        .last()
        .select("td").last().text mustBe ""
    }

    "Show claiming to much credit" when {
      "canBeClaimed is false" in {
        val view = page(form, canBeClaimed = false, true, rows, NormalMode)(request, messages)

        view.getElementsByTag("h2").text() must include(messages("confirmPackagingCredit.tooMuchCredit.heading"))
      }
    }

    "hide the yes/no " when {
      "moreYearsLeftToClaim is false" in {
        val view = page(form, true, moreYearsLeftToClaim = false, rows, NormalMode)(request, messages)

        view.text() must not include(messages("creditsSummary.add-to-list"))
        
        val defaultNoInput = view.getElementById("defaultNoInput")
        defaultNoInput.attr("name") mustBe "value"
        defaultNoInput.attr("type") mustBe "hidden"
        defaultNoInput.attr("value") mustBe "false"
      }
    }

    "show an error" when {

      "no option is selected" in {
        val errorForm = form.bind(Map("value" -> ""))
        createView(errorForm).getElementsByClass("govuk-error-summary__list").text() mustBe messages("creditsSummary.error.required")
      }

      "display error summary box" in {
        val view = createView(form.withError("error key", "error message"))
        view.getElementsByClass("govuk-error-summary__title").text() mustBe "There is a problem"
        view.getElementsByClass("govuk-error-summary__title").text() mustBe messages("error.summary.title")
      }
    }
  }

}
