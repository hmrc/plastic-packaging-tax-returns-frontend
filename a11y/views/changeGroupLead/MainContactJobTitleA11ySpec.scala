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

package views.changeGroupLead

import base.ViewSpecBase
import forms.changeGroupLead.MainContactJobTitleFormProvider
import models.Mode.NormalMode
import play.api.data.Form
import uk.gov.hmrc.scalatestaccessibilitylinter.AccessibilityMatchers
import views.html.changeGroupLead.MainContactJobTitleView


class MainContactJobTitleA11ySpec extends ViewSpecBase with AccessibilityMatchers {
 private val page = inject[MainContactJobTitleView]

  val form: Form[String] = new MainContactJobTitleFormProvider()()

  private def createView(form: Form[String]): String =
    page(form, "contact-name", NormalMode)(request, messages).toString()

  "MainContactJobTitleView" should {

    "pass accessibility checks without error" in {
      createView(form) must passAccessibilityChecks
    }

    "pass accessibility checks with error" in {
      createView(form.withError("test", "message")) must passAccessibilityChecks
    }
  }

}
