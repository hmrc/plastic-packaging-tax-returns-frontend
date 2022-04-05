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

package support

import models.SignedInUser
import org.joda.time.DateTimeZone.UTC
import org.joda.time.{DateTime, LocalDate}
import uk.gov.hmrc.auth.core.retrieve._
import uk.gov.hmrc.auth.core.{AffinityGroup, ConfidenceLevel, CredentialStrength, Enrolments, User}

import scala.concurrent.Future

object AuthHelper {

  val nrsGroupIdentifierValue = Some("groupIdentifierValue")
  val nrsCredentialRole       = Some(User)
  val nrsMdtpInformation      = MdtpInformation("deviceId", "sessionId")
  val nrsItmpName             = ItmpName(Some("givenName"), Some("middleName"), Some("familyName"))
  val nrsDateOfBirth          = Some(LocalDate.now().minusYears(25))

  val nrsItmpAddress =
    ItmpAddress(Some("line1"),
                Some("line2"),
                Some("line3"),
                Some("line4"),
                Some("line5"),
                Some("postCode"),
                Some("countryName"),
                Some("countryCode")
    )

  val nrsCredentialStrength       = Some("STRONG")
  val currentLoginTime: DateTime  = new DateTime(1530442800000L, UTC)
  val previousLoginTime: DateTime = new DateTime(1530464400000L, UTC)
  val nrsLoginTimes               = LoginTimes(currentLoginTime, Some(previousLoginTime))

  // @formatter:off
  def createCredentialForUser(user: SignedInUser): Future[Some[Credentials] ~ Some[Name] ~ Some[Email] ~ Option[String] ~
    Option[String] ~ Option[AffinityGroup] ~ Enrolments ~ Option[String] ~ ConfidenceLevel ~ Option[String] ~ Option[String] ~
    Option[LocalDate] ~ AgentInformation ~ Some[String] ~ Some[User.type] ~ Some[MdtpInformation] ~ Some[ItmpName] ~
    Some[LocalDate] ~ Some[ItmpAddress] ~ Some[String] ~ LoginTimes] = {
    Future.successful(
      new ~(
        new ~(
          new ~(
            new ~(
              new ~(
                new ~(
                  new ~(
                    new ~(
                      new ~(
                        new ~(
                          new ~(
                            new ~(
                              new ~(
                                new ~(
                                  new ~(
                                    new ~(
                                      new ~(
                                        new ~(
                                          new ~(
                                            new ~(
                                              Some(Credentials(user.identityData.credentials.get.providerId, user.identityData.credentials.get.providerType),
                                              ),
                                              Some(Name(user.identityData.name.get.name, user.identityData.name.get.lastName))
                                            ),
                                            Some(Email(user.identityData.email.get))
                                          ),
                                          user.identityData.externalId
                                        ),
                                        Some(user.identityData.internalId)
                                      ),
                                      user.identityData.affinityGroup
                                    ),
                                    user.enrolments
                                  ),
                                  user.identityData.agentCode
                                ),
                                user.identityData.confidenceLevel.get
                              ),
                              user.identityData.nino
                            ),
                            user.identityData.saUtr
                          ),
                          user.identityData.dateOfBirth
                        ),
                        user.identityData.agentInformation.get
                      ),
                      nrsGroupIdentifierValue
                    ),
                    nrsCredentialRole
                  ),
                  Some(nrsMdtpInformation)
                ),
                Some(nrsItmpName)
              ),
              nrsDateOfBirth
            ),
            Some(nrsItmpAddress)
          ),
          nrsCredentialStrength
        ),
        nrsLoginTimes
      )
    )
  } // @formatter:on

  val expectedAcceptableCredentialsPredicate =
    AffinityGroup.Agent.or(CredentialStrength(CredentialStrength.strong))
}
