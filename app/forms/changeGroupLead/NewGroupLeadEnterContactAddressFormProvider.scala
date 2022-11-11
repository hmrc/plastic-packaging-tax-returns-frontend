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

import javax.inject.Inject

//todo: add more validation: see SoT
class NewGroupLeadEnterContactAddressFormProvider @Inject() extends Mappings {

   def apply(): Form[NewGroupLeadAddressDetails] = Form(
     mapping(
       addressLine1 -> text("newGroupLeadEnterContactAddress.error.addressLine.required")
        .verifying(maxLength(35, "newGroupLeadEnterContactAddress.error.addressLine1.length")),
       addressLine2 -> text("newGroupLeadEnterContactAddress.error.addressLine.required")
        .verifying(maxLength(35, "newGroupLeadEnterContactAddress.error.addressLine2.length")),
       addressLine3 -> optional(text("newGroupLeadEnterContactAddress.error.AddressLine2.required")),
       addressLine4 -> text("newGroupLeadEnterContactAddress.error.AddressLine2.required"),
       postalCode -> optional(text("newGroupLeadEnterContactAddress.error.AddressLine2.required")),
       countryCode -> text("newGroupLeadEnterContactAddress.error.AddressLine2.required")
         .verifying(maxLength(35, "newGroupLeadEnterContactAddress.error.AddressLine2.length"))
    )(NewGroupLeadAddressDetails.apply)(NewGroupLeadAddressDetails.unapply)
   )
 }

object NewGroupLeadEnterContactAddressFormProvider {
  val addressLine1 = "addressLine1"
  val addressLine2 = "addressLine2"
  val addressLine3 = "addressLine3"
  val addressLine4 = "addressLine4"
  val postalCode = "postalCode"
  val countryCode = "countryCode"
}
