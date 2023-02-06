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

package views.changeGroupLead

import base.ViewSpecBase
import forms.changeGroupLead.SelectNewGroupLeadForm
import models.subscription.{GroupMembers, Member}
import play.api.data.Form
import play.api.mvc.Call
import uk.gov.hmrc.scalatestaccessibilitylinter.AccessibilityMatchers
import views.html.changeGroupLead.ChooseNewGroupLeadView


class ChooseNewGroupLeadA11ySpec extends ViewSpecBase  with AccessibilityMatchers {
  val page: ChooseNewGroupLeadView = inject[ChooseNewGroupLeadView]
  val members: GroupMembers = GroupMembers(Seq(Member("Test Company Ltd Asia", "1"), Member("Test Company Ltd Europe", "2"), Member("Test Company Ltd UK", "3")))
  val form: Form[Member] = new SelectNewGroupLeadForm().apply(members.membersNames)
  private val call = Call("get", "b")

  private def createView(form: Form[Member]): String =
    page(form, members, call)(request, messages).toString()

  "ChooseNewGroupLeadView" should {

    "pass accessibility checks without error" in {
      createView(form) must passAccessibilityChecks
    }

    "pass accessibility checks with error" in {
      createView(form.withError("test", "message")) must passAccessibilityChecks
    }

  }

}
