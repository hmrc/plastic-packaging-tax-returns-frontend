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
import forms.returns.PlasticExportedByAnotherBusinessFormProvider
import models.Mode.NormalMode
import play.api.data.Form
import uk.gov.hmrc.scalatestaccessibilitylinter.AccessibilityMatchers
import views.html.returns.PlasticExportedByAnotherBusinessView

class PlasticExportedByAnotherBusinessViewA11ySpec extends ViewSpecBase with AccessibilityMatchers {

  private val page = inject[PlasticExportedByAnotherBusinessView]
  private val form = new PlasticExportedByAnotherBusinessFormProvider()()

  private def render(form: Form[Boolean] = form): String =
    page(form, NormalMode, 200L)(request, messages).toString()

  "pass accessibility checks without error" in {
    render() must passAccessibilityChecks
  }

  "pass accessibility checks with error" in {
    render(form.withError("error", "error message")) must passAccessibilityChecks
  }
}
