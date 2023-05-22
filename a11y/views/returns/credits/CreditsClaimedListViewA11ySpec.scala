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
import models.Mode.NormalMode
import models.returns.credits.CreditSummaryRow
import play.api.data.Form
import play.api.data.Forms.boolean
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Text}
import uk.gov.hmrc.scalatestaccessibilitylinter.AccessibilityMatchers
import views.html.returns.credits.CreditsClaimedListView

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

  def render(form: Form[Boolean], canBeClaimed: Boolean): String =
    page(form, canBeClaimed, true, years, NormalMode)(request, messages).toString()

  "view" should {
    "pass accessibility tests" when {
      "can claim credit" in {
        render(form, true) must passAccessibilityChecks
      }

      "cannot claim credit" in {
        render(form, false) must passAccessibilityChecks
      }
    }

    "with error" in {
      render(form.withError("test", "message"), true) must passAccessibilityChecks
    }
  }
}