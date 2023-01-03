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

package services

import connectors.SubscriptionConnector
import models.{EisError, EisFailure}
import models.subscription.group.{GroupPartnershipDetails, GroupPartnershipSubscription}
import models.subscription.{AddressDetails, ContactDetails, CustomerDetails, CustomerType, Declaration, GroupMembers, LegalEntityDetails, OrganisationDetails, PrimaryContactDetails, PrincipalPlaceOfBusinessDetails}
import models.subscription.subscriptionDisplay.SubscriptionDisplayResponse
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar
import org.mockito.MockitoSugar.{mock, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubscriptionServiceSpec extends PlaySpec with BeforeAndAfterEach {

  private val subscriptionConnector = mock[SubscriptionConnector]
  private val headerCarrier = mock[HeaderCarrier]

  private val service = new SubscriptionService(subscriptionConnector)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    MockitoSugar.reset(subscriptionConnector, headerCarrier)
    when(subscriptionConnector.get(any)(any)) thenReturn Future.successful(Right(subscriptionDisplayResponse))
  }


  "SubscriptionService" should {
    
    "call its connector" in {
      service.fetchGroupMemberNames("ppt-ref")(headerCarrier)    
      verify(subscriptionConnector).get("ppt-ref")(headerCarrier)
    }
    
    "extract the group member names" in {
      await(service.fetchGroupMemberNames("ppt-ref")(headerCarrier)) mustBe GroupMembers(Seq("Po"))
    }
    
    "handle connector failure" in {
      val eisFailure = EisFailure(Some(Seq(EisError("error-code", "error-reason"))))
      when(subscriptionConnector.get(any)(any)) thenReturn Future.successful(Left(eisFailure))
      the [RuntimeException] thrownBy 
        await(service.fetchGroupMemberNames("ppt-ref")(headerCarrier)) must have message "Subscription connector failed"
    }
    
  }

  
  private val customerDetails: CustomerDetails = CustomerDetails(customerType = CustomerType.Organisation,
    individualDetails = None, organisationDetails = None)

  private val addressDetails: AddressDetails = AddressDetails("", "", None, None, None, "GB")

  private val contactDetails: ContactDetails = ContactDetails("", "", None)

  private def subscriptionDisplayResponse = {
    SubscriptionDisplayResponse(
      changeOfCircumstanceDetails = None,
      LegalEntityDetails("", "", None, customerDetails, true, false, false),
      PrincipalPlaceOfBusinessDetails(addressDetails, contactDetails),
      PrimaryContactDetails("", contactDetails, ""), 
      businessCorrespondenceDetails = addressDetails,
      Declaration(true), 
      taxObligationStartDate = "", 
      last12MonthTotalTonnageAmt = 1.0,
      processingDate = "", 
      groupPartnershipSubscription = Some(GroupPartnershipSubscription(None, None, Seq(
        GroupPartnershipDetails("", "", None, Some(OrganisationDetails(None, "Po")), None, addressDetails, contactDetails, false)
      ))) 
    )
  }
}
