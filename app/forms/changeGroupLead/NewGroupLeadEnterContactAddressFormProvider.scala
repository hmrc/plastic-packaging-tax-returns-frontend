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

package forms.changeGroupLead

import forms.changeGroupLead.NewGroupLeadEnterContactAddressFormProvider._
import forms.mappings.Mappings
import models.changeGroupLead.NewGroupLeadAddressDetails
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraint
import play.api.data.validation.Constraints.pattern

import javax.inject.Inject

//todo: add more validation: see SoT
class NewGroupLeadEnterContactAddressFormProvider @Inject() extends Mappings {

   def apply(): Form[NewGroupLeadAddressDetails] = Form(
     mapping(
       addressLine1 -> text("newGroupLeadEnterContactAddress.error.addressLine.required")
        .verifying(maxLength(maxAddressLineLength, "newGroupLeadEnterContactAddress.error.addressLine1.length"))
         .verifying(isValidAddressLine("newGroupLeadEnterContactAddress.error.addressLine1.invalid.line")),
       addressLine2 -> text("newGroupLeadEnterContactAddress.error.addressLine.required")
        .verifying(maxLength(maxAddressLineLength, "newGroupLeadEnterContactAddress.error.addressLine2.length"))
         .verifying(isValidAddressLine("newGroupLeadEnterContactAddress.error.addressLine2.invalid.line")),
       addressLine3 -> optional(
         text("newGroupLeadEnterContactAddress.error.AddressLine2.required")
           .verifying(maxLength(maxAddressLineLength, "newGroupLeadEnterContactAddress.error.addressLine3.length"))
           .verifying(isValidAddressLine("newGroupLeadEnterContactAddress.error.addressLine3.invalid.line"))
       ),
       addressLine4 -> text("newGroupLeadEnterContactAddress.error.addressLine4.required")
         .verifying(maxLength(maxAddressLineLength, "newGroupLeadEnterContactAddress.error.addressLine4.length"))
         .verifying(isValidAddressLine("newGroupLeadEnterContactAddress.error.addressLine4.invalid.line")),
       postalCode -> optional(
         text("newGroupLeadEnterContactAddress.error.postalCode.required ")
           .verifying(maxLength(8, "newGroupLeadEnterContactAddress.error.postalCode.inRange"))
           .verifying(minLength(5, "newGroupLeadEnterContactAddress.error.postalCode.inRange"))
           .verifying(isValidAddressLine("newGroupLeadEnterContactAddress.error.postalCode.inRange"))
       ),
       countryCode -> text("newGroupLeadEnterContactAddress.error.countryCode.required")
         .verifying(maxLength(maxAddressLineLength, "newGroupLeadEnterContactAddress.error.countryCode.length"))
         .verifying(isValidAddressLine("newGroupLeadEnterContactAddress.error.countryCode.invalid"))
    )(NewGroupLeadAddressDetails.apply)(NewGroupLeadAddressDetails.unapply).verifying(
       "newGroupLeadEnterContactAddress.error.postalCode.required",
       fields =>
         fields match {
           case address => validatePostalCode(address.countryCode, address.postalCode)
         }
     )
   )

  private def isValidAddressLine(errorKey: String): Constraint[String] =
    pattern(addressLineRegExp.r, error = errorKey)

  private def validatePostalCode(countryCode: String, postalCode: Option[String]): Boolean = {
    if(countryCode.equals("GB") && !postalCode.isDefined) false
    else true

  }
 }

object NewGroupLeadEnterContactAddressFormProvider {
  val addressLine1 = "addressLine1"
  val addressLine2 = "addressLine2"
  val addressLine3 = "addressLine3"
  val addressLine4 = "addressLine4"
  val postalCode = "postalCode"
  val countryCode = "countryCode"

  val addressLineRegExp = "^[A-Za-z0-9-`,.&'\\s]*$"
  val maxAddressLineLength = 35
}
