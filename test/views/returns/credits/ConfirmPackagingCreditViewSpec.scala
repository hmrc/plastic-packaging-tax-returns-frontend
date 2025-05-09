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
import models.Mode.NormalMode
import models.returns.CreditRangeOption
import play.api.mvc.Call
import play.twirl.api.Html
import support.{ViewAssertions, ViewMatchers}
import uk.gov.hmrc.govukfrontend.views.Aliases.{Text, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{ActionItem, Actions, Key, SummaryListRow}
import views.html.returns.credits.ConfirmPackagingCreditView

import java.time.LocalDate

class ConfirmPackagingCreditViewSpec extends ViewSpecBase with ViewAssertions with ViewMatchers {

  private val page: ConfirmPackagingCreditView = inject[ConfirmPackagingCreditView]
  private val requestedCredit                  = BigDecimal(500)
  private val continueCall                     = Call("TEST", "/end-point")
  private val creditRangeOption                = CreditRangeOption(LocalDate.of(2023, 4, 1), LocalDate.of(2024, 3, 31))

  private val summaryList = Seq(
    SummaryListRow(key = Key(Text("tax rate")), value = Value(Text("value in pounds"))),
    SummaryListRow(
      key = Key(Text("exported")),
      value = Value(Text("answer")),
      actions = Some(Actions(items = Seq(ActionItem("/foo", Text("change")))))
    )
  )
  private def createView(): Html =
    page("year-key", requestedCredit, summaryList, continueCall, NormalMode, creditRangeOption)(request, messages)

  "View" should {

    val view = createView()

    "have a title" in {
      view.select("title").text() must include(
        "Confirm credit for 1 April 2023 to 31 March 2024 - Submit return - Plastic Packaging Tax - GOV.UK"
      )
      view.select("title").text() must include(
        messages("confirmPackagingCredit.title", "1 April 2023 to 31 March 2024")
      )
    }

    "have a header" in {
      view.select("h1").text mustBe s"Confirm credit for 1 April 2023 to 31 March 2024"
      view.select("h1").text mustBe messages("confirmPackagingCredit.heading", "1 April 2023 to 31 March 2024")
    }

    "display summary list row" in {
      view.getElementsByClass("govuk-summary-list__row").size() mustEqual 2
    }

    "display conditional paragraph" when {
      "only one year available to claim" in {
        view.getElementsByClass("govuk-body").text() must include(
          "Your £500.00 credit will be applied against your total balance in your Plastic Packaging Tax account."
        )
        view.getElementsByClass("govuk-body").text() must include(
          messages("confirmPackagingCredit.requestedCredits", "£500.00")
        )

        withClue("should not show too much credit paragraph") {
          view.select("h2").text() must not include "You are claiming too much credit"
        }
      }
    }

    "show cancel button when can claim credit" in {
      createView().getElementById("link-cancel").text() mustBe "Cancel credit claim"
      createView().getElementById("link-cancel").select("a").get(0) must
        haveHref(controllers.returns.credits.routes.CancelCreditsClaimController.onPageLoad("year-key").url)
    }

    "have a confirm and continue button" in {
      view.getElementById("link-button") must haveHref(continueCall.url)
      view.getElementById("link-button").text() mustBe "Confirm and continue"
      view.getElementById("link-button").text() mustBe messages("site.continue.confirm")
    }

  }
}
