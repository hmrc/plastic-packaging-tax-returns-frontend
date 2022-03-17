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

package uk.gov.hmrc.plasticpackagingtax.returns.controllers.actions

import com.google.inject.{ImplementedBy, Inject}
import com.kenshoo.play.metrics.Metrics
import play.api.Logger
import play.api.mvc.Results.Redirect
import play.api.mvc._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{agentCode, _}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.plasticpackagingtax.returns.config.AppConfig
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.actions.AuthAction.{
  pptEnrolmentIdentifierName,
  pptEnrolmentKey
}
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.home.{routes => homeRoutes}
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.agents.{routes => agentRoutes}
import uk.gov.hmrc.plasticpackagingtax.returns.models.SignedInUser
import uk.gov.hmrc.plasticpackagingtax.returns.models.request.{AuthenticatedRequest, IdentityData}
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

class AuthActionImpl @Inject() (
  override val authConnector: AuthConnector,
  pptReferenceAllowedList: PptReferenceAllowedList,
  appConfig: AppConfig,
  metrics: Metrics,
  mcc: MessagesControllerComponents
) extends AuthAction with AuthorisedFunctions {

  implicit override val executionContext: ExecutionContext = mcc.executionContext
  override val parser: BodyParser[AnyContent]              = mcc.parsers.defaultBodyParser
  private val logger                                       = Logger(this.getClass)
  private val authTimer                                    = metrics.defaultRegistry.timer("ppt.returns.upstream.auth.timer")

  private def authPredicate = Enrolment("HMRC-PPT-ORG")

  private val authData =
    credentials and name and email and externalId and internalId and affinityGroup and allEnrolments and
      agentCode and confidenceLevel and nino and saUtr and dateOfBirth and agentInformation and groupIdentifier and
      credentialRole and mdtpInformation and itmpName and itmpDateOfBirth and itmpAddress and credentialStrength and loginTimes

  override def invokeBlock[A](
    request: Request[A],
    block: AuthenticatedRequest[A] => Future[Result]
  ): Future[Result] = {
    implicit val hc: HeaderCarrier =
      HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    val authorisation = authTimer.time()
    authorised(authPredicate)
      .retrieve(authData) {
        case credentials ~ name ~ email ~ externalId ~ internalId ~ affinityGroup ~ allEnrolments ~ agentCode ~
            confidenceLevel ~ authNino ~ saUtr ~ dateOfBirth ~ agentInformation ~ groupIdentifier ~
            credentialRole ~ mdtpInformation ~ itmpName ~ itmpDateOfBirth ~ itmpAddress ~ credentialStrength ~ loginTimes =>
          authorisation.stop()

          val identityData = IdentityData(internalId,
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

          affinityGroup match {
            case Some(AffinityGroup.Agent) =>
              // prevent agent access while the service is unable to support them
              Future.successful(Redirect(agentRoutes.AgentsController.displayPage()))
            case _ =>
              getPptEnrolmentId(allEnrolments, pptEnrolmentIdentifierName) match {
                case None =>
                  throw InsufficientEnrolments(
                    s"key: $pptEnrolmentKey and identifier: $pptEnrolmentIdentifierName is not found"
                  )
                case Some(pptEnrolmentIdentifier) =>
                  executeRequest(request,
                                 block,
                                 identityData,
                                 pptEnrolmentIdentifier,
                                 allEnrolments
                  )
              }
          }

      } recover {
      case _: NoActiveSession =>
        Results.Redirect(appConfig.loginUrl, Map("continue" -> Seq(appConfig.loginContinueUrl)))
      case _: InsufficientEnrolments =>
        // Authenticated users who lack PPT enrolments are redirected to registration frontend
        Results.Redirect(homeRoutes.UnauthorisedController.onPageLoad())
      case _: AuthorisationException =>
        Results.Redirect(homeRoutes.UnauthorisedController.onPageLoad())
    }
  }

  private def executeRequest[A](
    request: Request[A],
    block: AuthenticatedRequest[A] => Future[Result],
    identityData: IdentityData,
    pptEnrolmentIdentifier: String,
    allEnrolments: Enrolments
  ) =
    if (pptReferenceAllowedList.isAllowed(pptEnrolmentIdentifier)) {
      val pptLoggedInUser = SignedInUser(allEnrolments, identityData)
      block(new AuthenticatedRequest(request, pptLoggedInUser, Some(pptEnrolmentIdentifier)))
    } else {
      logger.warn("User id is not allowed, access denied")
      Future.successful(Results.Redirect(homeRoutes.UnauthorisedController.onPageLoad()))
    }

  private def getPptEnrolmentId(enrolments: Enrolments, identifier: String): Option[String] =
    getPptEnrolmentIdentifier(enrolments, identifier) match {
      case Some(enrolmentIdentifier) =>
        Option(enrolmentIdentifier).filter(_.value.trim.nonEmpty).map(_.value)
      case None => Option.empty
    }

  private def getPptEnrolmentIdentifier(
    enrolmentsList: Enrolments,
    identifier: String
  ): Option[EnrolmentIdentifier] =
    enrolmentsList.enrolments
      .filter(_.key == pptEnrolmentKey)
      .flatMap(_.identifiers)
      .find(_.key == identifier)

}

object AuthAction {
  val pptEnrolmentKey            = "HMRC-PPT-ORG"
  val pptEnrolmentIdentifierName = "EtmpRegistrationNumber"
}

@ImplementedBy(classOf[AuthActionImpl])
trait AuthAction
    extends ActionBuilder[AuthenticatedRequest, AnyContent]
    with ActionFunction[Request, AuthenticatedRequest]
