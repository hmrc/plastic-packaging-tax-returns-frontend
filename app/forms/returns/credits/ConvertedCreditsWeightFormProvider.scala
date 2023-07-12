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
import play.api.data.Form

class ConvertedCreditsWeightFormProvider extends Mappings {

  def apply(): Form[Long] =
    Form(
      "value" -> long(
        "converted-credits-weight.error.required",
        "converted-credits-weight.error.wholeNumber",
        "converted-credits-weight.error.nonNumeric")
          .verifying(inRange(0L, 99999999999L, "converted-credits-weight.error.outOfRange"))
    )
}
