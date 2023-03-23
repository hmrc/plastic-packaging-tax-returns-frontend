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

package models.changeGroupLead

import play.api.libs.json.{Json, OFormat}
import play.api.mvc.Request
import services.CountryService


case class NewGroupLeadAddressDetails(
  addressLine1: String,
  addressLine2: String,
  addressLine3: Option[String],
  addressLine4: Option[String],
  postalCode: Option[String],
  countryCode: String // If 'GB' then must have postalCode field, otherwise postalCode is optional
) {
  def definedFields(countryService: CountryService) (implicit request: Request[_])=
    Seq(
      Some(addressLine1),
      Some(addressLine2),
      addressLine3,
      addressLine4,
      postalCode,
      Some(countryService.tryLookupCountryName(countryCode))
    ).flatten

  def toBuffer: NewGroupLeadAddressDetailsFormBuffer = {
    val defined = Seq(Some(addressLine1), Some(addressLine2), addressLine3, addressLine4).flatten

    NewGroupLeadAddressDetailsFormBuffer(
      addressLine1 = defined.head,
      addressLine2 = defined.drop(1).dropRight(1).headOption,
      addressLine3 = defined.drop(2).dropRight(1).headOption,
      townOrCity = defined.last,
      postalCode = postalCode,
      countryCode = countryCode
    )
  }

}

object NewGroupLeadAddressDetails {
  implicit val format: OFormat[NewGroupLeadAddressDetails] = Json.format[NewGroupLeadAddressDetails]
}

case class NewGroupLeadAddressDetailsFormBuffer (
  addressLine1: String,
  addressLine2: Option[String],
  addressLine3: Option[String],
  townOrCity: String,
  postalCode: Option[String],
  countryCode: String // If 'GB' then must have postalCode field, otherwise postalCode is optional
) {

  def toNewGroupLeadAddressDetails: NewGroupLeadAddressDetails = {
    val defined = Seq(Some(addressLine1), addressLine2, addressLine3, Some(townOrCity)).flatten

    NewGroupLeadAddressDetails(
      defined.head,
      defined.drop(1).head,
      defined.drop(2).headOption,
      defined.drop(3).headOption,
      postalCode,
      countryCode
    )
  }
}

