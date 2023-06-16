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
import models.returns.CreditRangeOption
import play.api.mvc.Call
import play.twirl.api.Html
import uk.gov.hmrc.scalatestaccessibilitylinter.AccessibilityMatchers
import views.html.returns.credits.ConfirmPackagingCreditView

import java.time.LocalDate

class ConfirmPlasticCreditViewA11ySpec extends ViewSpecBase with AccessibilityMatchers {

  private val page = inject[ConfirmPackagingCreditView]
  private val creditRangeOption = CreditRangeOption(LocalDate.of(2023, 4, 1), LocalDate.of(2024, 3, 31))
  private def render(canClaimCredit: Boolean ): Html = page("year-key", BigDecimal(200), Seq.empty, Call("GET", "/test"), NormalMode, creditRangeOption)(request, messages)

  "ConfirmPackagingCreditView" should {
    "pass accessibility checks" when {
      "can claim credit" in {
        render(true).toString() must passAccessibilityChecks
      }

      "cannot claim credit" in {
        render(false).toString() must passAccessibilityChecks
      }
    }
  }
}
