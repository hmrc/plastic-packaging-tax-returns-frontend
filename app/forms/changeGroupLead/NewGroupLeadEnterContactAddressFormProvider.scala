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

import forms.ConditionalMapping
import forms.changeGroupLead.NewGroupLeadEnterContactAddressFormProvider._
import models.changeGroupLead.{NewGroupLeadAddressDetails, NewGroupLeadAddressDetailsFormBuffer}
import play.api.data.Forms.{mapping, optional, text}
import play.api.data.{Form, Mapping, OptionalMapping}
import uk.gov.voa.play.form.{Condition, MandatoryOptionalMapping}


class NewGroupLeadEnterContactAddressFormProvider {

  def apply(): Form[NewGroupLeadAddressDetails] = Form(
     mapping(
       addressLine1 -> addressLineTextValidation(
         addressLineRequiredKey,
         addressLine1LengthKey,
         addressLine1InvalidCharKey
       ),
       addressLine2 -> optional(play.api.data.Forms.text)
         .verifying(addressLine2LengthKey, _.forall(_.length <= maxAddressLineLength))
         .verifying(addressLine2InvalidCharKey, _.forall(_.matches(addressLineRegExp))
       ),
       addressLine3 -> optional(play.api.data.Forms.text)
         .verifying(addressLine3LengthKey, _.forall(_.length <= maxAddressLineLength))
         .verifying(addressLine3InvalidCharKey, _.forall(_.matches(addressLineRegExp))
       ),
       addressLine4 -> addressLineTextValidation(
         addressLine4RequiredKey,
         addressLine4LengthKey,
         addressLine4InvalidChar
       ),
       postalCode -> mandatoryIfEqualOrOptional(countryCode, "GB",
         gbPostCodeValidation,
         optionalPostalCodeValidation
       ),
       countryCode -> countryCodeValidation
    )(NewGroupLeadAddressDetailsFormBuffer.apply)(NewGroupLeadAddressDetailsFormBuffer.unapply)
       .transform[NewGroupLeadAddressDetails](_.toNewGroupLeadAddressDetails, _.toBuffer)
   )

  def mandatoryIfEqualOrOptional[T](fieldName: String, value: String, mapping: Mapping[T],
                                    opMapping: Mapping[T]): Mapping[Option[T]] = {
    val condition: Condition = _.get(fieldName).exists(_ == value)
    ConditionalMapping(condition,
      MandatoryOptionalMapping(mapping, Nil),
      OptionalMapping(opMapping,Nil),
      Seq.empty)
  }

  private def optionalPostalCodeValidation = {
    optional(text)
      .transform[String](_.get, Some(_))
      .verifying(postalCodeMaxLengthKey, _.length <= postalCodMaxLength)
  }
  private def isInRange(min: Int, max: Int, length: Int) = {
    min <= length && max >= length
  }

  private def addressLineTextValidation(requiredKey: String, maxLengthKey: String, invalidCharacterKey: String) = {
    optional(play.api.data.Forms.text)
      .verifying(requiredKey, _.isDefined)
      .transform[String](_.get, Some(_))
      .verifying(requiredKey, _.trim.nonEmpty)
      .verifying(maxLengthKey, _.length <= maxAddressLineLength)
      .verifying(invalidCharacterKey, _.matches(addressLineRegExp))
  }

  private def countryCodeValidation = {
    optional(play.api.data.Forms.text)
      .verifying(countryCodeRequiredKey, _.isDefined)
      .transform[String](_.get, Some(_))
      .verifying(countryCodeRequiredKey, _.trim.nonEmpty)
  }

  private def gbPostCodeValidation = {
    optional(play.api.data.Forms.text)
      .verifying(postalCodeRequiredKey, _.isDefined)
      .transform[String](_.get, Some(_))
      .verifying(postalCodeRequiredKey, _.trim.nonEmpty)
      .verifying(postalCodeMaxLengthKey, _.matches(postcodeRegex))
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

  val postalCodMaxLength = 35
  val postcodeRegex = "^[A-Z]{1,2}[0-9][0-9A-Z]?\\s?[0-9][A-Z]{2}|BFPO\\s?[0-9]{1,10}$"

  val addressLineRequiredKey = "newGroupLeadEnterContactAddress.error.addressLine.required"
  val addressLine1LengthKey = "newGroupLeadEnterContactAddress.error.addressLine1.length"
  val addressLine1InvalidCharKey = "newGroupLeadEnterContactAddress.error.addressLine1.invalid.line"
  val addressLine2LengthKey = "newGroupLeadEnterContactAddress.error.addressLine2.length"
  val addressLine2InvalidCharKey = "newGroupLeadEnterContactAddress.error.addressLine2.invalid.line"
  val addressLine3LengthKey = "newGroupLeadEnterContactAddress.error.addressLine3.length"
  val addressLine3InvalidCharKey = "newGroupLeadEnterContactAddress.error.addressLine3.invalid.line"
  val addressLine4RequiredKey = "newGroupLeadEnterContactAddress.error.addressLine4.required"
  val addressLine4LengthKey = "newGroupLeadEnterContactAddress.error.addressLine4.length"
  val addressLine4InvalidChar = "newGroupLeadEnterContactAddress.error.addressLine4.invalid.line"
  val postalCodeMaxLengthKey = "newGroupLeadEnterContactAddress.error.postalCode.inRange"
  val postalCodeRequiredKey = "newGroupLeadEnterContactAddress.error.postalCode.required"
  val countryCodeRequiredKey = "newGroupLeadEnterContactAddress.error.countryCode.required"
  val countryCodeLengthKey = "newGroupLeadEnterContactAddress.error.countryCode.length"
  val countryCodeInvalidCharKey = "newGroupLeadEnterContactAddress.error.countryCode.invalid"
}
