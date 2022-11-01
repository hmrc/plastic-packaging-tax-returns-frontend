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

import models.subscription.group.GroupPartnershipSubscription
import play.api.libs.json.{Json, OFormat}

case class Subscription( //  Duplicate?
  legalEntityDetails: LegalEntityDetails,
  principalPlaceOfBusinessDetails: PrincipalPlaceOfBusinessDetails,
  primaryContactDetails: PrimaryContactDetails,
  businessCorrespondenceDetails: AddressDetails,
  declaration: Declaration,
  taxObligationStartDate: String,
  last12MonthTotalTonnageAmt: Long,
  groupSubscription: Option[GroupPartnershipSubscription] = None
)

object Subscription {
  implicit val format: OFormat[Subscription] = Json.format[Subscription]

}
