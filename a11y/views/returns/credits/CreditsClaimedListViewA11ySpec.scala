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
import models.returns.credits.CreditSummaryRow
import models.{CreditBalance, TaxablePlastic}
import play.api.data.Form
import play.api.data.Forms.boolean
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Text}
import uk.gov.hmrc.scalatestaccessibilitylinter.AccessibilityMatchers
import views.html.returns.credits.CreditsClaimedListView

import java.time.LocalDate

class CreditsClaimedListViewA11ySpec extends ViewSpecBase with AccessibilityMatchers {

  private val form = Form("value" -> boolean).fill(true)
  private val page = inject[CreditsClaimedListView]

  private val years = Seq(
    CreditSummaryRow(
      label = "2023-01-01-2023-03-31",
      value = "0",
      actions = Seq(
        ActionItem("change-url", Text("site.change")),
        ActionItem("remove-url", Text("site.remove"))
      )
    ),
    CreditSummaryRow(
      label = "2023-04-01-2024-03-31",
      value = "0",
      actions = Seq(
        ActionItem("change-url", Text("site.change")),
        ActionItem("remove-url", Text("site.remove"))
      )
    )
  )

  val creditRangeOption = CreditRangeOption(LocalDate.now(), LocalDate.now())

  val creditBalance = CreditBalance(
    10,
    20,
    5L,
    true,
    Map(
      creditRangeOption.key -> TaxablePlastic(0, 20, 0)
    )
  )

  def render(form: Form[Boolean], creditBalance: CreditBalance): String =
    page(form, creditBalance, LocalDate.now(), Seq(creditRangeOption), years, NormalMode)(request, messages).toString()

  "view" should {
    "pass accessibility tests" when {
      "can claim credit" in {
        render(form, creditBalance) must passAccessibilityChecks
      }

      "cannot claim credit" in {
        render(form, creditBalance.copy(canBeClaimed = false)) must passAccessibilityChecks
      }

      "no more years to claim" in {
        page(form, creditBalance, LocalDate.now(), Seq.empty, years, NormalMode)(
          request,
          messages
        ).toString() must passAccessibilityChecks
      }
    }

    "with error" in {
      render(form.withError("test", "message"), creditBalance) must passAccessibilityChecks
    }
  }
}
