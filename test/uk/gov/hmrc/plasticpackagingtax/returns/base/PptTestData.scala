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

package uk.gov.hmrc.plasticpackagingtax.returns.base

import org.joda.time.{DateTime, LocalDate}
import uk.gov.hmrc.auth.core.ConfidenceLevel.L50
import uk.gov.hmrc.auth.core.retrieve.{AgentInformation, Credentials, LoginTimes, Name}
import uk.gov.hmrc.auth.core.{Enrolment, Enrolments}
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.actions.AuthAction
import uk.gov.hmrc.plasticpackagingtax.returns.models.request.IdentityData
import uk.gov.hmrc.plasticpackagingtax.returns.models.subscription._
import uk.gov.hmrc.plasticpackagingtax.returns.models.subscription.subscriptionDisplay.{
  ChangeOfCircumstanceDetails,
  SubscriptionDisplayResponse
}
import uk.gov.hmrc.plasticpackagingtax.returns.models.subscription.subscriptionUpdate.SubscriptionUpdateRequest
import uk.gov.hmrc.plasticpackagingtax.returns.models.{subscription, SignedInUser}

import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime.now
import java.time.format.DateTimeFormatter

object PptTestData {

  val nrsCredentials = Credentials(providerId = "providerId", providerType = "providerType")

  val ukLimitedCompanySubscription: Subscription = Subscription(
    legalEntityDetails =
      LegalEntityDetails(dateOfApplication =
                           now(UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                         customerIdentification1 = "123456789",
                         customerIdentification2 = Some("1234567890"),
                         customerDetails = CustomerDetails(customerType = CustomerType.Organisation,
                                                           organisationDetails =
                                                             Some(
                                                               subscription.OrganisationDetails(
                                                                 organisationName =
                                                                   Some("Plastics Ltd"),
                                                                 organisationType =
                                                                   Some("UK Limited Company")
                                                               )
                                                             )
                         )
      ),
    principalPlaceOfBusinessDetails =
      PrincipalPlaceOfBusinessDetails(
        addressDetails = AddressDetails(addressLine1 = "2-3 Scala Street",
                                        addressLine2 = "Soho",
                                        addressLine3 = Some("London"),
                                        postalCode = Some("W1T 2HN"),
                                        countryCode = "GB"
        ),
        contactDetails = ContactDetails(email = "test@test.com", telephone = "02034567890")
      ),
    primaryContactDetails =
      PrimaryContactDetails(name = "Kevin Durant",
                            contactDetails =
                              ContactDetails(email = "test@test.com", telephone = "02034567890"),
                            positionInCompany = "Director"
      ),
    businessCorrespondenceDetails = AddressDetails(addressLine1 = "addressLine1",
                                                   addressLine2 = " line2 Town",
                                                   postalCode = Some("PostCode"),
                                                   countryCode = "GB"
    ),
    taxObligationStartDate = now(UTC).toString,
    last12MonthTotalTonnageAmt = 15000,
    declaration = Declaration(declarationBox1 = true),
    groupSubscription = None
  )

  val soleTraderSubscription: Subscription = {
    val subscription = ukLimitedCompanySubscription.copy(legalEntityDetails =
      LegalEntityDetails(dateOfApplication =
                           now(UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                         customerIdentification1 = "123456789",
                         customerIdentification2 = Some("1234567890"),
                         customerDetails =
                           CustomerDetails(customerType = CustomerType.Individual,
                                           individualDetails =
                                             Some(
                                               IndividualDetails(title = Some("MR"),
                                                                 firstName = "James",
                                                                 lastName = "Bond"
                                               )
                                             )
                           )
      )
    )
    subscription
  }

  def newUser(
    externalId: String = "123",
    enrolments: Option[Enrolments] = Some(pptEnrolment("123"))
  ): SignedInUser =
    SignedInUser(enrolments.getOrElse(Enrolments(Set())),
                 IdentityData(Some("Int-ba17b467-90f3-42b6-9570-73be7b78eb2b"),
                              Some(externalId),
                              None,
                              Some(nrsCredentials),
                              Some(L50),
                              None,
                              None,
                              Some(Name(Some("Aldo"), Some("Rain"))),
                              Some(LocalDate.now().minusYears(25)),
                              Some("amina@hmrc.co.uk"),
                              Some(
                                AgentInformation(Some("agentId"),
                                                 Some("agentCode"),
                                                 Some("agentFriendlyName")
                                )
                              ),
                              None,
                              None,
                              None,
                              None,
                              None,
                              None,
                              None,
                              Some("crdentialStrength 50"),
                              Some(LoginTimes(DateTime.now, None))
                 )
    )

  def pptEnrolment(pptEnrolmentId: String) =
    newEnrolments(
      newEnrolment(AuthAction.pptEnrolmentKey,
                   AuthAction.pptEnrolmentIdentifierName,
                   pptEnrolmentId
      )
    )

  def newEnrolments(enrolment: Enrolment*): Enrolments =
    Enrolments(enrolment.toSet)

  def newEnrolment(key: String, identifierName: String, identifierValue: String): Enrolment =
    Enrolment(key).withIdentifier(identifierName, identifierValue)

  def createSubscriptionDisplayResponse(subscription: Subscription) =
    SubscriptionDisplayResponse(processingDate = "2020-05-05",
                                changeOfCircumstanceDetails =
                                  Some(
                                    ChangeOfCircumstanceDetails(changeOfCircumstance =
                                      "update"
                                    )
                                  ),
                                legalEntityDetails =
                                  subscription.legalEntityDetails,
                                principalPlaceOfBusinessDetails =
                                  subscription.principalPlaceOfBusinessDetails,
                                primaryContactDetails =
                                  subscription.primaryContactDetails,
                                businessCorrespondenceDetails =
                                  subscription.businessCorrespondenceDetails,
                                taxObligationStartDate =
                                  subscription.taxObligationStartDate,
                                last12MonthTotalTonnageAmt =
                                  subscription.last12MonthTotalTonnageAmt.longValue(),
                                declaration =
                                  subscription.declaration,
                                groupOrPartnershipSubscription =
                                  subscription.groupSubscription
    )

  def createSubscriptionUpdateRequest(
    subscription: Subscription,
    changeOfCircumstanceDetails: ChangeOfCircumstanceDetails
  ) =
    SubscriptionUpdateRequest(changeOfCircumstanceDetails = changeOfCircumstanceDetails,
                              legalEntityDetails =
                                subscription.legalEntityDetails,
                              principalPlaceOfBusinessDetails =
                                subscription.principalPlaceOfBusinessDetails,
                              primaryContactDetails =
                                subscription.primaryContactDetails,
                              businessCorrespondenceDetails =
                                subscription.businessCorrespondenceDetails,
                              taxObligationStartDate =
                                subscription.taxObligationStartDate,
                              last12MonthTotalTonnageAmt =
                                subscription.last12MonthTotalTonnageAmt.longValue(),
                              declaration =
                                subscription.declaration,
                              groupOrPartnershipSubscription =
                                subscription.groupSubscription
    )

}
