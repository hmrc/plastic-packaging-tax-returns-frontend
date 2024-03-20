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

package forms.changeGroupLead

import forms.changeGroupLead.SelectNewGroupLeadForm.error
import models.subscription.Member
import play.api.data.Form
import play.api.data.Forms.{optional, text}

class SelectNewGroupLeadForm {

  def apply(members: Seq[Member]): Form[Member] =
    Form(
      "value" -> optional(text)
        .verifying(error, _.isDefined)
        .transform[String](_.get, Some(_))
        .verifying(error, crn => members.exists(_.crn == crn))
        .transform[Member](crn => members.find(_.crn == crn).get, _.crn)
    )

}

object SelectNewGroupLeadForm {
  val error = "select-new-representative.error.required"
}
