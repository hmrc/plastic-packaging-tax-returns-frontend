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
import models.returns.TaxReturnObligation
import uk.gov.hmrc.scalatestaccessibilitylinter.AccessibilityMatchers
import views.html.returns.SubmittedReturnsView

import java.time.LocalDate

class SubmittedReturnsViewA11ySpec extends ViewSpecBase with AccessibilityMatchers {

  private val page = inject[SubmittedReturnsView]

  val obligation = TaxReturnObligation(
    fromDate = LocalDate.of(2022, 5, 6),
    toDate = LocalDate.of(2022, 5, 6).plusMonths(2),
    dueDate = LocalDate.of(2022, 5, 6).plusMonths(3),
    periodKey = "ABC"
  )

  private def render(obligations: Seq[TaxReturnObligation]): String =
    page(obligations)(request, messages).toString()

  "pass accessibility checks" when {
    "has obligation" in {
      render(Seq(obligation, obligation.copy(), obligation.copy())) must passAccessibilityChecks
    }

    "has no obligation" in {
      render(Seq.empty) must passAccessibilityChecks
    }
  }

}
