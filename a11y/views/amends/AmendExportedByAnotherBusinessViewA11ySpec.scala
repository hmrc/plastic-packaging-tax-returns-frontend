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
import forms.amends.AmendExportedByAnotherBusinessFormProvider
import play.api.data.Form
import uk.gov.hmrc.scalatestaccessibilitylinter.AccessibilityMatchers
import views.html.amends.AmendExportedByAnotherBusinessView

class AmendExportedByAnotherBusinessViewA11ySpec extends ViewSpecBase with AccessibilityMatchers {

  val page         = inject[AmendExportedByAnotherBusinessView]
  private val form = new AmendExportedByAnotherBusinessFormProvider()()

  private def render(form: Form[Long] = form): String =
    page(form)(request, messages).toString()

  "view" should {
    "pass accessibility tests" in {
      render() must passAccessibilityChecks
    }

    "pass accessibility when error" in {
      render(form.withError("error", "error message")) must passAccessibilityChecks
    }
  }

}
