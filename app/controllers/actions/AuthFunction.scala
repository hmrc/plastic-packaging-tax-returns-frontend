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

package controllers.actions

import com.kenshoo.play.metrics.Metrics
import config.FrontendAppConfig
import controllers.home.{routes => homeRoutes}
import models.SignedInUser
import models.requests.{IdentifiedRequest, IdentityData}
import play.api.Logger
import play.api.mvc.{Request, Result, Results}
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuthFunction @Inject() (
  override val authConnector: AuthConnector,
  override val appConfig: FrontendAppConfig,
  metrics: Metrics
)(implicit ec: ExecutionContext)
    extends AuthorisedFunctions with CommonAuth {

  private val logger    = Logger(this.getClass)
  private val authTimer = metrics.defaultRegistry.timer("ppt.returns.upstream.auth.timer")

  def authorised[A](
    predicate: Predicate,
    request: Request[A],
    block: IdentifiedRequest[A] => Future[Result]
  ): Future[Result] = {

    implicit val hc: HeaderCarrier =
      HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    val authorisation = authTimer.time()
    val target = request.target.path

    authorised(predicate)
      .retrieve(authData) {
        case credentials ~ name ~ email ~ externalId ~ internalId ~ affinityGroup ~ allEnrolments ~ agentCode ~
            confidenceLevel ~ authNino ~ saUtr ~ dateOfBirth ~ agentInformation ~ groupIdentifier ~
            credentialRole ~ mdtpInformation ~ itmpName ~ itmpDateOfBirth ~ itmpAddress ~ credentialStrength ~ loginTimes =>
          authorisation.stop()
          logger.info(
            "Authorised with affinity group " + affinityGroup + " and enrolments " + allEnrolments
          )

          val maybeInternalId = internalId.getOrElse(
            throw new IllegalArgumentException(
              s"AuthenticatedIdentifierAction::invokeBlock -  internalId is required"
            )
          )

          val identityData = IdentityData(maybeInternalId,
                                          externalId,
                                          agentCode,
                                          credentials,
                                          Some(confidenceLevel),
                                          authNino,
                                          saUtr,
                                          name,
                                          dateOfBirth,
                                          email,
                                          Some(agentInformation),
                                          groupIdentifier,
                                          credentialRole.map(res => res.toJson.toString()),
                                          mdtpInformation,
                                          itmpName,
                                          itmpDateOfBirth,
                                          itmpAddress,
                                          affinityGroup,
                                          credentialStrength,
                                          Some(loginTimes)
          )

          executeRequest(request, block, identityData, allEnrolments)

      } recover {
      case _: NoActiveSession =>
        Results.Redirect(appConfig.loginUrl, Map("continue" -> Seq(target)))

      case _: IncorrectCredentialStrength =>
        Results.Redirect(appConfig.mfaUpliftUrl,
          Map("origin"      -> Seq(appConfig.serviceIdentifier),
            "continueUrl" -> Seq(target)
          )
        )

      case _: AuthorisationException =>
        Results.Redirect(homeRoutes.UnauthorisedController.unauthorised())
    }
  }

  private def executeRequest[A](
    request: Request[A],
    block: IdentifiedRequest[A] => Future[Result],
    identityData: IdentityData,
    allEnrolments: Enrolments
  ) = {
    val pptLoggedInUser = SignedInUser(allEnrolments, identityData)
    block(IdentifiedRequest(request, pptLoggedInUser, None))
  }

}
