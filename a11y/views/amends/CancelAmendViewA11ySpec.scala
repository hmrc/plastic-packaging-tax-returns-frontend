/*
 * Copyright 2022 HM Revenue & Customs
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
import forms.amends.CancelAmendFormProvider
import models.returns.TaxReturnObligation
import play.api.data.Form
import play.twirl.api.Html
import uk.gov.hmrc.scalatestaccessibilitylinter.AccessibilityMatchers
import views.html.amends.CancelAmendView

import java.time.LocalDate

class CancelAmendViewA11ySpec extends ViewSpecBase with AccessibilityMatchers {

  val form = new CancelAmendFormProvider()()
  val page = inject[CancelAmendView]

  val aTaxObligation: TaxReturnObligation = TaxReturnObligation(
    LocalDate.now(),
    LocalDate.now().plusWeeks(12),
    LocalDate.now().plusWeeks(16),
    "PK1")

  private def render(form: Form[Boolean] = form):  Html =
    page(form, aTaxObligation)(request, messages)

  "CancelAmendView" should {
    "pass accessibility checks" in {
      render().toString() must passAccessibilityChecks
    }

    "pass accessibility checks when error" in {
      render(form.withError("error", "error message")).toString() must passAccessibilityChecks
    }
  }

}
