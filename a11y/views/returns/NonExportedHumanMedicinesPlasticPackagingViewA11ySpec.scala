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

package views.returns

import base.ViewSpecBase
import forms.returns.NonExportedHumanMedicinesPlasticPackagingFormProvider
import models.Mode.NormalMode
import play.api.data.Form
import uk.gov.hmrc.scalatestaccessibilitylinter.AccessibilityMatchers
import views.html.returns.NonExportedHumanMedicinesPlasticPackagingView

class NonExportedHumanMedicinesPlasticPackagingViewA11ySpec extends ViewSpecBase with AccessibilityMatchers{

  val form = new NonExportedHumanMedicinesPlasticPackagingFormProvider()()
  private val page = inject[NonExportedHumanMedicinesPlasticPackagingView]
  private val amount = 321L

  private def createView(form: Form[Boolean], isYesNoDirectlyExported: Boolean): String =
    page(amount, form, NormalMode, isYesNoDirectlyExported)(request, messages).toString()

  "NonExportedHumanMedicinesPlasticPackagingView" should {

    "pass accessibility checks without error" when {

      "Directly Exported answer is yes" in {
        createView(form, true) must passAccessibilityChecks
      }

      "Directly Exported answer is no" in {
        createView(form, false) must passAccessibilityChecks
      }
    }

    "pass accessibility checks with error" when {

      "Directly Exported answer is yes" in {
        createView(form.withError("test", "message"), true) must passAccessibilityChecks
      }

      "Directly Exported answer is No" in {
        createView(form.withError("test", "message"), false) must passAccessibilityChecks
      }
    }
  }
}
