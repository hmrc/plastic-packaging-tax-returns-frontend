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

import models.subscription.subscriptionDisplay.SubscriptionDisplayResponse
import org.scalatestplus.play.PlaySpec

class GroupMembersSpec extends PlaySpec{

  "create" should {
    
    "handle missing group subscription" in {
      val subscription = createSubscriptionDisplayResponse
      the[NoSuchElementException] thrownBy GroupMembers.create(subscription) must have message "SubscriptionDisplayResponse " +
        "has no groupPartnershipSubscription field"
    }
    
}

  private val customerDetails: CustomerDetails = CustomerDetails(customerType = CustomerType.Organisation, 
    individualDetails = None, organisationDetails = None)

  private val legalEntityDetails: LegalEntityDetails = LegalEntityDetails("", "", None, customerDetails, 
    groupSubscriptionFlag = true, regWithoutIDFlag = false, partnershipSubscriptionFlag = false)

  private val addressDetails: AddressDetails = AddressDetails("", "", None, None, None, "")

  private val contactDetails: ContactDetails = ContactDetails("", "", None)

  private val principalPlaceOfBusinessDetails: PrincipalPlaceOfBusinessDetails = PrincipalPlaceOfBusinessDetails(
    addressDetails, contactDetails)

  val primaryContactDetails: PrimaryContactDetails = PrimaryContactDetails("", contactDetails, "")

  val declaration: Declaration = Declaration(true)

  private def createSubscriptionDisplayResponse = {
    SubscriptionDisplayResponse(changeOfCircumstanceDetails = None, legalEntityDetails,
      principalPlaceOfBusinessDetails, primaryContactDetails, businessCorrespondenceDetails = addressDetails, 
      declaration, taxObligationStartDate = "", last12MonthTotalTonnageAmt = 1.0, groupPartnershipSubscription = None, 
      processingDate = "")
  }
}
