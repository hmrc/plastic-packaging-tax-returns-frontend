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

package models.subscription

import models.subscription.CustomerType.{Individual, Organisation}
import play.api.libs.json.{Json, OFormat}

case class LegalEntityDetails(
  dateOfApplication: String,
  customerIdentification1: String,
  customerIdentification2: Option[String],
  customerDetails: CustomerDetails,
  groupSubscriptionFlag: Boolean,
  regWithoutIDFlag: Boolean,
  partnershipSubscriptionFlag: Boolean
) {
  def entityName: String = customerDetails.customerType match {
    case Individual   => customerDetails.individualDetails.get.toDisplayString
    case Organisation => customerDetails.organisationDetails.get.organisationName
    case _            => throw new IllegalStateException(s"Invalid customer type: ${customerDetails.customerType}")
  }

  val isGroup: Boolean       = groupSubscriptionFlag
  val isPartnership: Boolean = partnershipSubscriptionFlag
}

object LegalEntityDetails {

  implicit val format: OFormat[LegalEntityDetails] = Json.format[LegalEntityDetails]
}
