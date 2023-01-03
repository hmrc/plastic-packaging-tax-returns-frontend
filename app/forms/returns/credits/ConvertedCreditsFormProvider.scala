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

package forms.returns.credits

import forms.mappings.Mappings
import models.returns.CreditsAnswer
import play.api.data.Form
import play.api.data.Forms.mapping
import uk.gov.voa.play.form.ConditionalMappings.{isEqual, mandatoryIf}

class ConvertedCreditsFormProvider extends Mappings {

  def apply(): Form[CreditsAnswer] = {
    Form(
      mapping(
        "answer" -> boolean("converted.credits.error.required"),
        "converted-credits-weight" -> mandatoryIf(isEqual("answer", "true"),
          long("converted.credits.error.weight.required",
            wholeNumberKey = "converted.credits.error.whole.number",
            nonNumericKey = "converted.credits.error.non.numeric")
          .verifying(minimumValue(1L, "converted.credits.error.outOfRange.low"))
          .verifying(maximumValue(99999999999L, "converted.credits.error.outOfRange.high"))
        ))(CreditsAnswer.apply)(CreditsAnswer.unapply)
    )
  }
}