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

package forms

import forms.mappings.Mappings

import javax.inject.Inject
import play.api.data.Form
import play.api.data.validation.{Constraint, Invalid, Valid}

class ManufacturedPlasticPackagingWeightFormProvider @Inject() extends Mappings {

  def withinRange[A](minimum: A, maximum: A, errorKey: String)(implicit ev: Ordering[A]): Constraint[A] =
    Constraint {
      input =>
        import ev._

        if (input >= minimum && input <= maximum)
          Valid
        else
          Invalid(errorKey, minimum, maximum)
    }

  def apply(): Form[Long] =
    Form(
      "value" -> long(
        "manufacturedPlasticPackagingWeight.error.required",
        "manufacturedPlasticPackagingWeight.error.wholeNumber",
        "manufacturedPlasticPackagingWeight.error.nonNumeric"
      )
        .verifying(withinRange(0, 99999999999L, "manufacturedPlasticPackagingWeight.error.outOfRange"))
    )

  //todo this would need to be be big decimal for max 99999999999Kg
//  def apply(): Form[Int] = {
//    Form(
//      "value" -> int("manufacturedPlasticPackagingWeight.error.required",
//                     "manufacturedPlasticPackagingWeight.error.wholeNumber",
//                     "manufacturedPlasticPackagingWeight.error.nonNumeric")
//        .verifying(inRange(0, 999999999, "manufacturedPlasticPackagingWeight.error.outOfRange"))
//    )
//  }

}
