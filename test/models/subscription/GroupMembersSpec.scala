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

import models.subscription.group.{GroupPartnershipDetails, GroupPartnershipSubscription}
import models.subscription.subscriptionDisplay.SubscriptionDisplayResponse
import org.scalatestplus.play.PlaySpec

class GroupMembersSpec extends PlaySpec {

  "create" should {

    "handle missing group subscription" in {
      val subscription = createSubscriptionDisplayResponse(groupPartnershipSubscription = None)
      the[NoSuchElementException] thrownBy GroupMembers.create(
        subscription
      ) must have message "SubscriptionDisplayResponse " +
        "has no groupPartnershipSubscription field"
    }

    "handle empty group details" in {
      val subscription = createSubscriptionDisplayResponse(createGroupSubscription(Seq()))
      GroupMembers.create(subscription) mustBe GroupMembers(Seq())
    }

    "handle missing org-details within a member" in {
      val groupDetails = createGroupDetails(maybeOrganisationDetails = None)
      val subscription = createSubscriptionDisplayResponse(createGroupSubscription(Seq(groupDetails)))
      the[NoSuchElementException] thrownBy GroupMembers.create(
        subscription
      ) must have message "SubscriptionDisplayResponse " +
        "has a groupPartnershipDetails entry missing its organisationDetails field"
    }

    "extract names from group members in sorted order" in {
      val subscription = createSubscriptionDisplayResponse(
        createGroupSubscription(
          Seq(
            createGroupDetails(Some(OrganisationDetails(None, "Po"))),
            createGroupDetails(Some(OrganisationDetails(None, "Laa-laa"))),
            createGroupDetails(Some(OrganisationDetails(None, "Noo-noo")))
          )
        )
      )
      GroupMembers.create(subscription) mustBe GroupMembers(
        Seq(Member("Laa-laa", "crn"), Member("Noo-noo", "crn"), Member("Po", "crn"))
      )
    }

    "filter non GB organisations" in {
      val subscription = createSubscriptionDisplayResponse(
        createGroupSubscription(
          Seq(
            createGroupDetails(Some(OrganisationDetails(None, "non-gb"))).copy(addressDetails =
              addressDetails.copy(countryCode = "NONGB")
            ),
            createGroupDetails(Some(OrganisationDetails(None, "gb-1"))),
            createGroupDetails(Some(OrganisationDetails(None, "gb-2")))
          )
        )
      )
      GroupMembers.create(subscription) mustBe GroupMembers(Seq(Member("gb-1", "crn"), Member("gb-2", "crn")))
    }

    "filter the Representative from the members" in {
      val subscription = createSubscriptionDisplayResponse(
        createGroupSubscription(
          Seq(
            createGroupDetails(Some(OrganisationDetails(None, "rep"))).copy(relationship = "Representative"),
            createGroupDetails(Some(OrganisationDetails(None, "mem-1"))),
            createGroupDetails(Some(OrganisationDetails(None, "mem-2")))
          )
        )
      )
      GroupMembers.create(subscription) mustBe GroupMembers(Seq(Member("mem-1", "crn"), Member("mem-2", "crn")))
    }

  }

  private def createGroupDetails(maybeOrganisationDetails: Option[OrganisationDetails]) =
    GroupPartnershipDetails("", "crn", None, maybeOrganisationDetails, None, addressDetails, contactDetails, false)

  private def createGroupSubscription(groupPartnershipDetails: Seq[GroupPartnershipDetails]) =
    Some(
      GroupPartnershipSubscription(representativeControl = None, allMembersControl = None, groupPartnershipDetails)
    )

  private val customerDetails: CustomerDetails =
    CustomerDetails(customerType = CustomerType.Organisation, individualDetails = None, organisationDetails = None)

  private val legalEntityDetails: LegalEntityDetails = LegalEntityDetails(
    "",
    "",
    None,
    customerDetails,
    groupSubscriptionFlag = true,
    regWithoutIDFlag = false,
    partnershipSubscriptionFlag = false
  )

  private val addressDetails: AddressDetails = AddressDetails("", "", None, None, None, "GB")

  private val contactDetails: ContactDetails = ContactDetails("", "", None)

  private val principalPlaceOfBusinessDetails: PrincipalPlaceOfBusinessDetails =
    PrincipalPlaceOfBusinessDetails(addressDetails, Some(contactDetails))

  val primaryContactDetails: PrimaryContactDetails = PrimaryContactDetails("", contactDetails, "")

  val declaration: Declaration = Declaration(true)

  private def createSubscriptionDisplayResponse(groupPartnershipSubscription: Option[GroupPartnershipSubscription]) = {
    SubscriptionDisplayResponse(
      changeOfCircumstanceDetails = None,
      legalEntityDetails,
      principalPlaceOfBusinessDetails,
      primaryContactDetails,
      businessCorrespondenceDetails = addressDetails,
      declaration,
      taxObligationStartDate = "",
      last12MonthTotalTonnageAmt = 1.0,
      groupPartnershipSubscription,
      processingDate = ""
    )
  }
}
