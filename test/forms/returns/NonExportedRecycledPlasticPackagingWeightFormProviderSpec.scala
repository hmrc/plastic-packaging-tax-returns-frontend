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

package forms.returns

import forms.behaviours.LongFieldBehaviours
import play.api.data.FormError

class NonExportedRecycledPlasticPackagingWeightFormProviderSpec extends LongFieldBehaviours {

  val form = new NonExportedRecycledPlasticPackagingWeightFormProvider()()

  ".value" - {

    val fieldName = "value"

    val minimum = 0
    val maximum = 99999999999L

    val validDataGenerator = longsInRangeWithCommas(minimum, maximum)

    behave like fieldThatBindsValidData(form, fieldName, validDataGenerator)

    behave like longField(form,
      fieldName,
      nonNumericError =
        FormError(fieldName, "NonExportRecycledPlasticPackagingWeight.error.nonNumeric"),
      wholeNumberError =
        FormError(fieldName, "NonExportRecycledPlasticPackagingWeight.error.wholeNumber")
    )

    behave like longFieldWithMinimum(form,
      fieldName,
      minimum = minimum,
      expectedError = FormError(
        fieldName,
        "NonExportRecycledPlasticPackagingWeight.error.outOfRange.low",
        Seq(minimum)
      )
    )

    behave like longFieldWithMaximum(form,
      fieldName,
      maximum = maximum,
      expectedError = FormError(
        fieldName,
        "NonExportRecycledPlasticPackagingWeight.error.outOfRange.high",
        Seq(maximum)
      )
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "NonExportRecycledPlasticPackagingWeight.error.required")
    )
  }
}
