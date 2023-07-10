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
import forms.returns.credits.ExportedCreditsWeightFormProvider
import models.Mode.NormalMode
import models.returns.CreditRangeOption
import play.api.data.Form
import uk.gov.hmrc.scalatestaccessibilitylinter.AccessibilityMatchers
import views.html.returns.credits.ExportedCreditsWeightView

import java.time.LocalDate

class ExportedCreditsWeightViewA11ySpec extends ViewSpecBase with AccessibilityMatchers {

  val form = new ExportedCreditsWeightFormProvider()()
  val page = inject[ExportedCreditsWeightView]
  val creditRangeOption = CreditRangeOption(LocalDate.of(2023, 4, 1), LocalDate.of(2024, 3, 31))

  def render(form: Form[Long]): String =
    page(form, "year-key", NormalMode, creditRangeOption)(request, messages).toString()

  "view" should {
    "pass accessibility tests" when {
      "no error" in {
        render(form) must passAccessibilityChecks
      }

      "with error" in {
        render(form.withError("error", "error message")) must passAccessibilityChecks
      }
    }
  }

}
