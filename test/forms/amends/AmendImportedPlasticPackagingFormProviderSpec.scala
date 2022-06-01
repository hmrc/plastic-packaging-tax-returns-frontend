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

package forms.amends

import forms.behaviours.IntFieldBehaviours
import play.api.data.FormError

class AmendImportedPlasticPackagingFormProviderSpec extends IntFieldBehaviours {

  val form = new AmendImportedPlasticPackagingFormProvider()()

  ".value" - {

    val fieldName = "value"

    val minimum = 0
    val maximum = 99999999

    val validDataGenerator = intsInRangeWithCommas(minimum, maximum)

    behave like fieldThatBindsValidData(form, fieldName, validDataGenerator)

    behave like intField(form,
                         fieldName,
                         nonNumericError =
                           FormError(fieldName, "amendImportedPlasticPackaging.error.nonNumeric"),
                         wholeNumberError =
                           FormError(fieldName, "amendImportedPlasticPackaging.error.wholeNumber")
    )

    behave like intFieldWithRange(form,
                                  fieldName,
                                  minimum = minimum,
                                  maximum = maximum,
                                  expectedError =
                                    FormError(fieldName,
                                              "amendImportedPlasticPackaging.error.outOfRange",
                                              Seq(minimum, maximum)
                                    )
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "amendImportedPlasticPackaging.error.required")
    )
  }
}
