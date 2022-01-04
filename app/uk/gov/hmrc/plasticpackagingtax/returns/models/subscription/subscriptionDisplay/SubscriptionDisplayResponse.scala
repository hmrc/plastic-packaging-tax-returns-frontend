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

package uk.gov.hmrc.plasticpackagingtax.returns.models.subscription.subscriptionDisplay

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.plasticpackagingtax.returns.models.subscription.CustomerType.{
  Individual,
  Organisation
}
import uk.gov.hmrc.plasticpackagingtax.returns.models.subscription._
import uk.gov.hmrc.plasticpackagingtax.returns.models.subscription.group.GroupOrPartnershipSubscription

case class SubscriptionDisplayResponse(
  processingDate: String,
  changeOfCircumstanceDetails: Option[ChangeOfCircumstanceDetails],
  legalEntityDetails: LegalEntityDetails,
  principalPlaceOfBusinessDetails: PrincipalPlaceOfBusinessDetails,
  primaryContactDetails: PrimaryContactDetails,
  businessCorrespondenceDetails: AddressDetails,
  taxObligationStartDate: String,
  last12MonthTotalTonnageAmt: BigDecimal,
  declaration: Declaration,
  groupOrPartnershipSubscription: Option[GroupOrPartnershipSubscription]
) {

  val entityName: Option[String] = legalEntityDetails.customerDetails.customerType match {
    case Individual =>
      legalEntityDetails.customerDetails.individualDetails.map(
        details =>
          s"${details.title.map(_ + " ").getOrElse("")}${details.firstName} ${details.lastName}"
      )
    case Organisation =>
      legalEntityDetails.customerDetails.organisationDetails.flatMap(_.organisationName)
  }

  val organisationType: Option[String] =
    legalEntityDetails.customerDetails.organisationDetails.flatMap(_.organisationType)

  val isGroup = legalEntityDetails.groupSubscriptionFlag

}

object SubscriptionDisplayResponse {

  implicit val format: OFormat[SubscriptionDisplayResponse] =
    Json.format[SubscriptionDisplayResponse]

}
