/*
 * Copyright 2025 HM Revenue & Customs
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

package models.subscription.group

import models.subscription.group.GroupPartnershipDetails.Representative
import models.subscription.{AddressDetails, ContactDetails, IndividualDetails, OrganisationDetails}
import play.api.libs.json.{Json, OFormat}

case class GroupPartnershipDetails(
  relationship: String,
  customerIdentification1: String,
  customerIdentification2: Option[String],
  organisationDetails: Option[OrganisationDetails],
  individualDetails: Option[IndividualDetails],
  addressDetails: AddressDetails,
  contactDetails: ContactDetails,
  regWithoutIDFlag: Boolean
) {
  def isRepresentative: Boolean = relationship == Representative
}

object GroupPartnershipDetails {
  val Representative                                    = "Representative"
  implicit val format: OFormat[GroupPartnershipDetails] = Json.format[GroupPartnershipDetails]
}
