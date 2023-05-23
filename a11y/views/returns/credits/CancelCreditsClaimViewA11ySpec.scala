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
import models.returns.CreditsAnswer
import models.returns.credits.SingleYearClaim
import play.api.data.Form
import play.api.data.Forms.boolean
import play.api.mvc.Call
import play.twirl.api.Html
import uk.gov.hmrc.scalatestaccessibilitylinter.AccessibilityMatchers
import views.html.returns.credits.CancelCreditsClaimView

import java.time.LocalDate

class CancelCreditsClaimViewA11ySpec extends ViewSpecBase with AccessibilityMatchers {

  private val page = inject[CancelCreditsClaimView]
  private def render: Html = page(
    Form("value" -> boolean).fill(true),
    Call("POST", "call-url"), 
    SingleYearClaim(
      fromDate = LocalDate.of(1, 2, 3), 
      endDate = LocalDate.of(4, 5, 6), 
      exportedCredits = Some(CreditsAnswer(true, Some(7L))), 
      convertedCredits = Some(CreditsAnswer(true, Some(8L))))
  ) (request, messages)

  "view" should {
    "pass accessibility checks" in {
      render.toString() must passAccessibilityChecks
    }
  }

}
