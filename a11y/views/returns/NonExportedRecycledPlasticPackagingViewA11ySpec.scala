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
import forms.returns.NonExportedRecycledPlasticPackagingFormProvider
import models.Mode.NormalMode
import uk.gov.hmrc.scalatestaccessibilitylinter.AccessibilityMatchers
import views.html.returns.NonExportedRecycledPlasticPackagingView

class NonExportedRecycledPlasticPackagingViewA11ySpec extends ViewSpecBase with AccessibilityMatchers {

  val form = new NonExportedRecycledPlasticPackagingFormProvider()()
  val page = inject[NonExportedRecycledPlasticPackagingView]

  "NonExportedRecycledPlasticPackagingView" should {

    val amount = 123L

    def render(directlyExportedYesNoAnswer: Boolean): String =
      page(form, NormalMode, amount, directlyExportedYesNoAnswer)(request, messages).toString()

    "pass accessibility checks" when {
      "directly exported answer is Yes" in {
        render(true) must passAccessibilityChecks
      }

      "directly exported answer is No" in {
        render(false) must passAccessibilityChecks
      }
    }
  }
}
