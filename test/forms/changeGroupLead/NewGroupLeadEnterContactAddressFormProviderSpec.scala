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

import akka.actor.FSM.->
import forms.behaviours.StringFieldBehaviours
import forms.changeGroupLead.NewGroupLeadEnterContactAddressFormProvider.{addressLine1, addressLine2, addressLine4, countryCode, postalCode}
import play.api.data.FormError
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.postCode

//todo: add test for other fields validation. see SoT
class NewGroupLeadEnterContactAddressFormProviderSpec extends StringFieldBehaviours {

  val form = new NewGroupLeadEnterContactAddressFormProvider()()

  ".addressLine1" - {

    val fieldName = "addressLine1"
    val requiredKey = "newGroupLeadEnterContactAddress.error.addressLine.required"
    val lengthKey = "newGroupLeadEnterContactAddress.error.addressLine1.length"
    val maxLength = 35

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "must not include special character" in {
      val formq = form.bind(Map(addressLine1 -> "Ts%t T5est")).apply(addressLine1)

      formq.errors mustEqual Seq(FormError(fieldName, "newGroupLeadEnterContactAddress.error.addressLine.required"))
    }
  }

  ".addressLine2" - {

    val fieldName = "addressLine2"
    val requiredKey = "newGroupLeadEnterContactAddress.error.addressLine.required"
    val lengthKey = "newGroupLeadEnterContactAddress.error.addressLine2.length"
    val maxLength = 35

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
