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

package models.subscription

import org.mockito.MockitoSugar.when
import org.mockito.stubbing.ReturnsDeepStubs
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.PlaySpec

class LegalEntityDetailsSpec extends PlaySpec {

  val mockCustomerDetails = mock[CustomerDetails](ReturnsDeepStubs)

  val sut = LegalEntityDetails(
    "date-of-application",
    "customerIdentification1",
    Some("customerIdentification2"),
    customerDetails = mockCustomerDetails,
    groupSubscriptionFlag = true,
    regWithoutIDFlag = true,
    partnershipSubscriptionFlag = true
  )

  "entityName" must {
    "get the name" when {
      "Individual" in {
        when(mockCustomerDetails.customerType).thenReturn(CustomerType.Individual)
        when(mockCustomerDetails.individualDetails.get.toDisplayString).thenReturn("Individual-entityName")

        sut.entityName mustBe "Individual-entityName"
      }
      "Organisation" in {
        when(mockCustomerDetails.customerType).thenReturn(CustomerType.Organisation)
        when(mockCustomerDetails.organisationDetails.get.organisationName).thenReturn("Organisation-entityName")

        sut.entityName mustBe "Organisation-entityName"
      }
    }
  }

  "isGroup" must {
    "proxy to groupSubscriptionFlag" in {
      sut.isGroup mustBe true
      sut.copy(groupSubscriptionFlag = false).isGroup mustBe false
    }
  }

  "isPartnership" must {
    "proxy to partnershipSubscriptionFlag" in {
      sut.isPartnership mustBe true
      sut.copy(partnershipSubscriptionFlag = false).isPartnership mustBe false
    }
  }

}
