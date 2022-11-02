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

import models.subscription.group.{GroupPartnershipDetails, GroupPartnershipSubscription}
import models.subscription.subscriptionDisplay.SubscriptionDisplayResponse
import org.scalatestplus.play.PlaySpec

class GroupMembersSpec extends PlaySpec{

  "create" should {
    
    "handle missing group subscription" in {
      val subscription = createSubscriptionDisplayResponse(groupPartnershipSubscription = None)
      the[NoSuchElementException] thrownBy GroupMembers.create(subscription) must have message "SubscriptionDisplayResponse " +
        "has no groupPartnershipSubscription field"
    }
    
    "handle empty group details" in {
      val subscription = createSubscriptionDisplayResponse(createGroupSubscription(Seq()))
      GroupMembers.create(subscription) mustBe GroupMembers(Seq()) // TODO This may not be what we want, throw instead?
    }
    
    "handle missing org-details within a member" in {
      val groupDetails = createGroupDetails(maybeOrganisationDetails = None)
      val subscription = createSubscriptionDisplayResponse(createGroupSubscription(Seq(groupDetails)))
      the[NoSuchElementException] thrownBy GroupMembers.create(subscription) must have message "SubscriptionDisplayResponse " +
        "has a groupPartnershipDetails entry missing its organisationDetails field"
    }
    
    "extract names from group members" in {
      val subscription = createSubscriptionDisplayResponse(createGroupSubscription(
        Seq(
          createGroupDetails(Some(OrganisationDetails(None, "Po"))), 
          createGroupDetails(Some(OrganisationDetails(None, "Laa-laa"))), 
          createGroupDetails(Some(OrganisationDetails(None, "Noo-noo"))), 
      )))
      GroupMembers.create(subscription) mustBe GroupMembers(Seq("Po", "Laa-laa", "Noo-noo"))
    }
    
  }

  "zipMap" should {
    "inject member name and a unique index" in {
      val groupMembers = GroupMembers(Seq("Trotters Independent Traders", "OCP"))
      val function = (s: String, i: Int) => (s, i)  
      groupMembers.zipMap(function) mustBe Seq(("Trotters Independent Traders", 0), ("OCP", 1))
    }
  }

  private def createGroupDetails(maybeOrganisationDetails: Option[OrganisationDetails]) = {
    GroupPartnershipDetails("", "", None, maybeOrganisationDetails, None, addressDetails, contactDetails, false)
  }

  private def createGroupSubscription(groupPartnershipDetails: Seq[GroupPartnershipDetails]) = {
    Some(
      GroupPartnershipSubscription(representativeControl = None, allMembersControl = None, groupPartnershipDetails)
    )
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

  private def createSubscriptionDisplayResponse(groupPartnershipSubscription: Option[GroupPartnershipSubscription]) = {
    SubscriptionDisplayResponse(changeOfCircumstanceDetails = None, legalEntityDetails,
      principalPlaceOfBusinessDetails, primaryContactDetails, businessCorrespondenceDetails = addressDetails, 
      declaration, taxObligationStartDate = "", last12MonthTotalTonnageAmt = 1.0, 
      groupPartnershipSubscription, processingDate = ""
    )
  }
}
