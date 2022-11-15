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

package views.changeGrouplead

import base.ViewSpecBase
import models.changeGroupLead.RepresentativeMemberDetails
import play.api.mvc.Call
import uk.gov.hmrc.scalatestaccessibilitylinter.AccessibilityMatchers
import views.html.changeGroupLead.NewGroupLeadCheckYourAnswerView

class NewGroupLeadCheckYourAnswerViewA11ySpec extends ViewSpecBase with AccessibilityMatchers {

  private val page = inject[NewGroupLeadCheckYourAnswerView]

  def render: String =
    page(RepresentativeMemberDetails("test test", "name", "job"), Call("get", ""))(request, messages).toString()


  "view" should {
    "pass accessibility tests" in {
        render must passAccessibilityChecks
    }
  }
}
