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

import com.google.inject.Inject
import com.kenshoo.play.metrics.Metrics
import config.FrontendAppConfig
import controllers.actions.AuthenticatedIdentifierAction.IdentifierAction.{pptEnrolmentIdentifierName, pptEnrolmentKey}
import models.Mode.NormalMode
import models.SignedInUser
import models.requests.{IdentifiedRequest, IdentityData}
import play.api.Logging
import play.api.mvc.{ActionBuilder, ActionFunction, AnyContent, BodyParsers, Request, Result, Results}
import play.api.mvc.Results.Redirect
import uk.gov.hmrc.auth.core.{AffinityGroup, AuthConnector, AuthorisationException, AuthorisedFunctions, CredentialStrength, Enrolment, Enrolments, InsufficientEnrolments, NoActiveSession}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import controllers.{routes => agentRoutes}
import controllers.home.{routes => homeRoutes}
import repositories.SessionRepository
import repositories.SessionRepository.Paths.AgentSelectedPPTRef
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{affinityGroup, allEnrolments, internalId}

import scala.concurrent.{ExecutionContext, Future}


//used almost everywhere
trait AuthAction
  extends ActionBuilder[IdentifiedRequest, AnyContent]
    with ActionFunction[Request, IdentifiedRequest]

class AuthenticatedIdentifierAction @Inject() (
                                                override val authConnector: AuthConnector,
                                                appConfig: FrontendAppConfig,
                                                sessionRepository: SessionRepository,
                                                metrics: Metrics,
                                                val parser: BodyParsers.Default
                                              )(implicit val executionContext: ExecutionContext)
  extends AuthAction with AuthorisedFunctions with Logging {

  override def invokeBlock[A](
                               request: Request[A],
                               block: IdentifiedRequest[A] => Future[Result]
                             ): Future[Result] = {

    implicit val hc: HeaderCarrier =
      HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised(Enrolment(pptEnrolmentKey).and(CredentialStrength(CredentialStrength.strong)).or(AffinityGroup.Agent))
      .retrieve(internalId and affinityGroup and allEnrolments) {
      case maybeInternalId ~ affinityGroup ~ allEnrolments =>
        val internalId = maybeInternalId.getOrElse(throw new IllegalArgumentException("internalId is required"))
        val identityData = IdentityData(internalId, affinityGroup)
        val pptLoggedInUser = SignedInUser(allEnrolments, identityData)

        affinityGroup match {
          case Some(AffinityGroup.Agent) =>
            sessionRepository.get[String](internalId, AgentSelectedPPTRef).flatMap(
              _.fold(
                Future.successful(Redirect(agentRoutes.AgentsController.onPageLoad(NormalMode)))
              ) {
                pptEnrolmentIdentifier =>
                  block(IdentifiedRequest(request, pptLoggedInUser, pptEnrolmentIdentifier))
              }
            )
          case _ =>
            // A non agent has authed; their ppt enrolment will have been returned
            // from auth as it is a principal enrolment
            getPptReferenceNumber(allEnrolments) match {
              case Some(pptReferenceNumber) =>
                block(IdentifiedRequest(request, pptLoggedInUser, pptReferenceNumber))
              case None =>
                throw new RuntimeException("Auth verified PPT user does not have PPT Reference Number")
            }
        }
    } recover {
      case _: NoActiveSession =>
        Redirect(appConfig.loginUrl, Map("continue" -> Seq(request.target.path)))

      case _: InsufficientEnrolments =>
        Results.Redirect(homeRoutes.UnauthorisedController.notEnrolled())

      case _: AuthorisationException =>
        Redirect(homeRoutes.UnauthorisedController.notEnrolled())
    }
  }

  private def getPptReferenceNumber(allEnrolments: Enrolments): Option[String] = {
    allEnrolments
      .getEnrolment(pptEnrolmentKey)
      .flatMap(_.getIdentifier(pptEnrolmentIdentifierName))
      .filter(_.value.trim.nonEmpty) //is this necessary??
      .map(_.value)
  }

}

object AuthenticatedIdentifierAction {
  object IdentifierAction {
    val pptEnrolmentKey = "HMRC-PPT-ORG"
    val pptEnrolmentIdentifierName = "EtmpRegistrationNumber"
  }
}