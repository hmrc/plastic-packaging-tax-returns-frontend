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
import forms.changeGroupLead.NewGroupLeadEnterContactAddressFormProvider
import models.Mode
import models.Mode.NormalMode
import play.api.data.Form
import uk.gov.hmrc.scalatestaccessibilitylinter.AccessibilityMatchers
import views.html.changeGroupLead.NewGroupLeadEnterContactAddressView

class NewGroupLeadEnterContactAddressA11ySpec extends ViewSpecBase with AccessibilityMatchers {

  private val page = inject[NewGroupLeadEnterContactAddressView]

  val form: Form[_] = new NewGroupLeadEnterContactAddressFormProvider().apply()
  val organisationName: String = "Test"
  val mode: Mode = NormalMode

  private def createView(form: Form[_]): String =
    page(form, Map(), organisationName, mode)(request, messages).toString()

  "NewGroupLeadEnterContactAddressView" should {

    "pass accessibility checks without error" in {
      createView(form) must passAccessibilityChecks
    }

    "pass accessibility checks with error" in {
      createView(form.withError("test", "message")) must passAccessibilityChecks
    }
  }

}
