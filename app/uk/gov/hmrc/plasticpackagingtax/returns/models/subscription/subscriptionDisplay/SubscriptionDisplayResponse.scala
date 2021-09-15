/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.plasticpackagingtax.returns.models.subscription.subscriptionDisplay

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.plasticpackagingtax.returns.models.subscription.group.GroupOrPartnershipSubscription
import uk.gov.hmrc.plasticpackagingtax.returns.models.subscription._

case class SubscriptionDisplayResponse(
  processingDate: String,
  changeOfCircumstanceDetails: Option[ChangeOfCircumstanceDetails],
  legalEntityDetails: LegalEntityDetails,
  principalPlaceOfBusinessDetails: PrincipalPlaceOfBusinessDetails,
  primaryContactDetails: PrimaryContactDetails,
  businessCorrespondenceDetails: AddressDetails,
  taxObligationStartDate: String,
  last12MonthTotalTonnageAmt: Option[BigDecimal],
  declaration: Declaration,
  groupPartnershipSubscription: Option[GroupOrPartnershipSubscription]
)

object SubscriptionDisplayResponse {

  implicit val format: OFormat[SubscriptionDisplayResponse] =
    Json.format[SubscriptionDisplayResponse]

}
