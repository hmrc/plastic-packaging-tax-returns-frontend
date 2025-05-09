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

package views.returns

import base.ViewSpecBase
import forms.returns.StartYourReturnFormProvider
import models.returns.TaxReturnObligation
import play.api.data.Form
import play.twirl.api.Html
import uk.gov.hmrc.scalatestaccessibilitylinter.AccessibilityMatchers
import views.html.returns.StartYourReturnView

import java.time.LocalDate

class StartYourReturnViewA11ySpec extends ViewSpecBase with AccessibilityMatchers {

  val page = inject[StartYourReturnView]
  val form = new StartYourReturnFormProvider()()

  val aTaxObligation: TaxReturnObligation =
    TaxReturnObligation(LocalDate.of(2022, 7, 5), LocalDate.of(2022, 10, 5), LocalDate.of(2023, 1, 5), "PK1")

  def render(form: Form[Boolean], isFirstReturn: Boolean): Html =
    page(form, aTaxObligation, isFirstReturn)(request, messages)

  "pass accessibility tests" when {
    "is not first return" in {
      render(form, false).toString() must passAccessibilityChecks
    }

    "is first return" in {
      render(form, true).toString() must passAccessibilityChecks
    }

    "error" in {
      render(form.withError("error", "error message"), true).toString() must passAccessibilityChecks
    }
  }
}
