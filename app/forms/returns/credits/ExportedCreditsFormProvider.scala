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

package forms.returns.credits

import forms.mappings.Mappings
import models.returns.CreditsAnswer
import play.api.data.Form
import play.api.data.Forms.mapping
import uk.gov.voa.play.form.ConditionalMappings.{isEqual, mandatoryIf}

class ExportedCreditsFormProvider extends Mappings {

  def apply(): Form[CreditsAnswer] = {
    Form(
      mapping(
        "answer" -> boolean("exported.credits.error.required"),
        "exported-credits-weight" -> mandatoryIf(isEqual("answer", "true"),
          long("exported.credits.error.weight.required",
            wholeNumberKey = "exported.credits.error.whole.number",
            nonNumericKey = "exported.credits.error.non.numeric")
          .verifying(minimumValue(1L, "exported.credits.error.outOfRange.low"))
          .verifying(maximumValue(99999999999L, "exported.credits.error.outOfRange.high"))
        ))(CreditsAnswer.apply)(CreditsAnswer.unapply)
    )
  }
}