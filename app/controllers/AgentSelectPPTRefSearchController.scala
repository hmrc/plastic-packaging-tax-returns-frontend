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

package controllers

import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import repositories.SessionRepository.Paths.AgentSelectedPPTRef
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.internalId
import uk.gov.hmrc.auth.core.{AffinityGroup, AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AgentSelectPPTRefSearchController @Inject() (
  val authConnector: AuthConnector,
  override val messagesApi: MessagesApi,
  override val controllerComponents: MessagesControllerComponents,
  sessionRepository: SessionRepository
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with AuthorisedFunctions {

  def get(): Action[AnyContent] = Action.async { implicit request =>
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)

    authorised(AffinityGroup.Agent).retrieve(internalId) { maybeInternalId =>
      maybeInternalId.fold(Future.successful[Result](NotFound))(internalId =>
        sessionRepository.get[String](internalId, AgentSelectedPPTRef).map {
          case Some(registration) => Ok(Json.obj("clientPPT" -> registration))
          case None               => NotFound
        }
      )
    }
  }
}
