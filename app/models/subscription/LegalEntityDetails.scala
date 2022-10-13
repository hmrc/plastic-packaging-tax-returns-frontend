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

package models.subscription

import models.subscription.CustomerType.{Individual, Organisation}
import play.api.libs.json.{Json, OFormat}

case class LegalEntityDetails(
  dateOfApplication: String,
  customerIdentification1: String,
  customerIdentification2: Option[String] = None,
  customerDetails: CustomerDetails,
  groupSubscriptionFlag: Boolean = false,
  regWithoutIDFlag: Option[Boolean] = None,
  partnershipSubscriptionFlag: Boolean = false
){
  val entityName: String = customerDetails.customerType match {
    case Individual =>
      customerDetails.individualDetails.map(
        details =>
          s"${details.title.map(_ + " ").getOrElse("")}${details.firstName} ${details.lastName}"
      ).getOrElse(throw new IllegalStateException("Individual name absent"))
    case Organisation =>
      customerDetails.organisationDetails.flatMap(_.organisationName).getOrElse(
        throw new IllegalStateException("Organisation name absent")
      )
  }

  val organisationType: Option[String] =
    customerDetails.organisationDetails.flatMap(_.organisationType)

  val isGroup: Boolean       = groupSubscriptionFlag
  val isPartnership: Boolean = partnershipSubscriptionFlag
}

object LegalEntityDetails {

  implicit val format: OFormat[LegalEntityDetails] = Json.format[LegalEntityDetails]
}
