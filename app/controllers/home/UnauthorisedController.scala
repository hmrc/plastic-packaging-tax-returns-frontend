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

package controllers.home

import controllers.actions.AuthCheckAction
import controllers.agents.SelectedClientIdentifier
import models.Mode.NormalMode
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.UnauthorisedView
import controllers.{routes => agentRoutes}

import javax.inject.Inject

class UnauthorisedController @Inject() (
  authenticate: AuthCheckAction,
  val controllerComponents: MessagesControllerComponents,
  view: UnauthorisedView
) extends FrontendBaseController with I18nSupport with SelectedClientIdentifier {

  def unauthorised: Action[AnyContent] =
    Action { implicit request =>
      Ok(view())
    }

  def notEnrolled: Action[AnyContent] =
    authenticate { implicit IdentifierRequest =>
      // A signed in user has been redirect here for having no enrolment.
      // If this is an agent then they have landed here for one of 2 reasons:
      // - They have not supplied a client identifier
      // - They have supplied a client identifier but failed auth enrolment (invalid identifier or not one of their clients)
      // These can be sent to the agents client page
      val affinityGroup = IdentifierRequest.user.identityData.affinityGroup
      affinityGroup match {
        case Some(AffinityGroup.Agent) =>
          getSelectedClientIdentifierFrom(IdentifierRequest) match {
            case Some(_) =>
              Redirect(agentRoutes.AgentsController.onPageLoad(NormalMode)).flashing(
                ("clientPPTFailed" -> "true")
              )
            case _ =>
              Redirect(agentRoutes.AgentsController.onPageLoad(NormalMode))
          }
        case _ =>
          // All other users see the you need to register page.
          Ok(view())
      }
    }

}
