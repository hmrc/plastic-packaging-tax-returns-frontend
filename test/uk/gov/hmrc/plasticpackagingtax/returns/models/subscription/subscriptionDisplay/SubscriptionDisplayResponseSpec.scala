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

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import uk.gov.hmrc.plasticpackagingtax.returns.base.PptTestData.{
  createSubscriptionDisplayResponse,
  soleTraderSubscription,
  ukLimitedCompanySubscription
}

class SubscriptionDisplayResponseSpec extends AnyWordSpecLike with Matchers {

  "SubscriptionDisplayResponse" should {

    "return valid entity name" when {

      "subscription is for organisation" in {
        val subscription = createSubscriptionDisplayResponse(ukLimitedCompanySubscription)

        subscription.entityName mustBe Some("Plastics Ltd")
      }

      "subscription is for organisation but details are missing" in {
        val subscription = createSubscriptionDisplayResponse(
          ukLimitedCompanySubscription.copy(legalEntityDetails =
            ukLimitedCompanySubscription.legalEntityDetails.copy(customerDetails =
              ukLimitedCompanySubscription.legalEntityDetails.customerDetails.copy(
                organisationDetails = None
              )
            )
          )
        )

        subscription.entityName mustBe None
      }

      "subscription is for individual" in {
        val subscription = createSubscriptionDisplayResponse(soleTraderSubscription)

        subscription.entityName mustBe Some("MR James Bond")
      }

      "subscription is for individual with no title" in {
        val subscription = createSubscriptionDisplayResponse(
          soleTraderSubscription.copy(legalEntityDetails =
            soleTraderSubscription.legalEntityDetails.copy(customerDetails =
              soleTraderSubscription.legalEntityDetails.customerDetails.copy(individualDetails =
                soleTraderSubscription.legalEntityDetails.customerDetails.individualDetails.map(
                  _.copy(title = None)
                )
              )
            )
          )
        )

        subscription.entityName mustBe Some("James Bond")
      }

      "subscription is for individual but details are missing" in {
        val subscription = createSubscriptionDisplayResponse(
          soleTraderSubscription.copy(legalEntityDetails =
            soleTraderSubscription.legalEntityDetails.copy(customerDetails =
              soleTraderSubscription.legalEntityDetails.customerDetails.copy(individualDetails =
                None
              )
            )
          )
        )

        subscription.entityName mustBe None
      }

    }
  }
}
