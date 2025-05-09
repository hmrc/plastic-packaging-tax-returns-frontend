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

package controllers.actions

import uk.gov.hmrc.play.bootstrap.metrics.Metrics
import config.FrontendAppConfig
import controllers.actions.AuthFunction.QueryParamKeys
import play.api.mvc.{Request, Result, Results, WrappedRequest}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.internalId
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

case class AuthedUser[A](internalId: String, request: Request[A]) extends WrappedRequest[A](request)

class AuthFunction @Inject() (
  override val authConnector: AuthConnector,
  appConfig: FrontendAppConfig,
  metrics: Metrics
)(implicit ec: ExecutionContext)
    extends AuthorisedFunctions {

  def authorised[A](
    predicate: Predicate,
    request: Request[A],
    block: AuthedUser[A] => Future[Result]
  ): Future[Result] = {

    implicit val hc: HeaderCarrier =
      HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    val target = request.target.path

    authorised(predicate)
      .retrieve(internalId) { maybeInternalId =>
        val internalId = maybeInternalId.getOrElse(throw new IllegalStateException("internalId is required"))
        block(AuthedUser(internalId, request))
      } recover {
      case _: NoActiveSession =>
        Results.Redirect(appConfig.loginUrl, Map(QueryParamKeys.continue -> Seq(target)))

      case _: IncorrectCredentialStrength =>
        Results.Redirect(
          appConfig.mfaUpliftUrl,
          Map(QueryParamKeys.origin -> Seq(appConfig.serviceIdentifier), QueryParamKeys.continueUrl -> Seq(target))
        )
    }
  }

}

object AuthFunction {
  object QueryParamKeys {
    val continue    = "continue"
    val origin      = "origin"
    val continueUrl = "continueUrl"
  }
}
