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

import models.subscription.subscriptionDisplay.SubscriptionDisplayResponse
import play.api.libs.json.{Format, Json}

case class Member(organisationName: String, crn: String)
object Member {
  implicit val formats: Format[Member] = Json.format[Member]
}

case class GroupMembers(membersNames: Seq[Member])

object GroupMembers {
  def create(response: SubscriptionDisplayResponse): GroupMembers = {
    val members =
      response.groupPartnershipSubscription
        .getOrElse(
          throw new NoSuchElementException("SubscriptionDisplayResponse has no groupPartnershipSubscription field")
        )
        .groupPartnershipDetails.collect {
        case details if details.addressDetails.isGB && !details.isRepresentative =>
          val orgName = details.organisationDetails
            .getOrElse(throw new NoSuchElementException("SubscriptionDisplayResponse has a groupPartnershipDetails " +
              "entry missing its organisationDetails field"))
            .organisationName
          Member(orgName, details.customerIdentification1)
      }
    GroupMembers(members.sortBy(_.organisationName))
  }
}
