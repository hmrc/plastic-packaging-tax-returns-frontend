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
import uk.gov.hmrc.plasticpackagingtax.returns.models.SignedInUser
import uk.gov.hmrc.plasticpackagingtax.returns.models.subscription._
import uk.gov.hmrc.plasticpackagingtax.returns.models.request.IdentityData

import java.util.UUID

object PptTestData {

  val nrsCredentials = Credentials(providerId = "providerId", providerType = "providerType")

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

  def ukLimitedCompanySubscription(
    pptReference: String = UUID.randomUUID().toString
  ): PptSubscription =
    PptSubscription(pptReference = pptReference,
                    primaryContactDetails =
                      PrimaryContactDetails(Some("FirstName LastName"),
                                            jobTitle = Some("CEO"),
                                            email =
                                              Some("test@test.com"),
                                            phoneNumber =
                                              Some("1234567890"),
                                            address = Some(
                                              Address(addressLine1 =
                                                        "addressLine1",
                                                      addressLine2 =
                                                        "line2",
                                                      addressLine3 =
                                                        Some("Town"),
                                                      postCode =
                                                        Some("PostCode")
                                              )
                                            )
                      ),
                    organisationDetails =
                      OrganisationDetails(isBasedInUk = Some(true),
                                          organisationType =
                                            Some("UK_COMPANY"),
                                          businessRegisteredAddress =
                                            Some(
                                              Address(addressLine1 =
                                                        "addressLine1",
                                                      addressLine3 =
                                                        Some("Town"),
                                                      addressLine2 = "line2",
                                                      postCode =
                                                        Some("PostCode")
                                              )
                                            ),
                                          safeNumber = Some("123"),
                                          incorporationDetails = Some(
                                            IncorporationDetails(companyName =
                                                                   Some("Plastics Limited"),
                                                                 phoneNumber = Some("12345678"),
                                                                 email = Some("test@email.com")
                                            )
                                          )
                      )
    )

  def soleTraderSubscription(pptReference: String = UUID.randomUUID().toString): PptSubscription = {
    val regDetails = ukLimitedCompanySubscription(pptReference)
    regDetails.copy(organisationDetails =
      regDetails.organisationDetails.copy(
        soleTraderDetails =
          Some(SoleTraderIncorporationDetails(firstName = Some("James"), lastName = Some("Bond"))),
        incorporationDetails = None
      )
    )
  }

}
