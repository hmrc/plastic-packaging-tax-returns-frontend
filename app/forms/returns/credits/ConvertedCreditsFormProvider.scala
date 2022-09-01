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

import javax.inject.Inject
import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms.mapping
import uk.gov.voa.play.form.ConditionalMappings.{isEqual, mandatoryIf}

case class ConvertedCreditsAnswer(yesNo: Boolean, weight: Option[Long])

class ConvertedCreditsFormProvider extends Mappings {
  val requiredKey = "convertedCredits.error.required"

  def apply(): Form[ConvertedCreditsAnswer] = {
    Form(
      mapping(
        "value" -> boolean(requiredKey),
        "converted-credits-weight" -> mandatoryIf(isEqual("answer", "yes"), long(requiredKey)
          .verifying(minimumValue(1L, "exportedPlasticPackagingWeight.error.outOfRange.low"))
          .verifying(maximumValue(99999999999L, "exportedPlasticPackagingWeight.error.outOfRange.high"))
        ))(ConvertedCreditsAnswer.apply)(ConvertedCreditsAnswer.unapply)
    )
  }
}