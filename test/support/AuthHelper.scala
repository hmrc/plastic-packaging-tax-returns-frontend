package support

import models.SignedInUser
import org.joda.time.DateTimeZone.UTC
import org.joda.time.{DateTime, LocalDate}
import uk.gov.hmrc.auth.core.User
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.externalId
import uk.gov.hmrc.auth.core.retrieve._

import scala.concurrent.Future

object AuthHelper {

  val nrsGroupIdentifierValue = Some("groupIdentifierValue")
  val nrsCredentialRole       = Some(User)
  val nrsMdtpInformation      = MdtpInformation("deviceId", "sessionId")
  val nrsItmpName             = ItmpName(Some("givenName"), Some("middleName"), Some("familyName"))
  val nrsDateOfBirth              = Some(LocalDate.now().minusYears(25))

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

  def createCredentialForUser(user: SignedInUser) = {
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
                                              Some(Name(user.identityData.name.get.name, user.identityData.name.get.lastName))
                                            ),
                                            Some(Email(user.identityData.email.get))
                                          ),
                                          Some(externalId(user.identityData.externalId.get))
                                        ),
                                        user.identityData.internalId
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
    )

  }
}
