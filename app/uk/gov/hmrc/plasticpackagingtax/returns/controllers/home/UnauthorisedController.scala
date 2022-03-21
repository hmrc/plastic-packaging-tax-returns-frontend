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

package uk.gov.hmrc.plasticpackagingtax.returns.controllers.home

import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.actions.AuthCheckAction
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.agents.{
  SelectedClientIdentifier,
  routes => agentRoutes
}
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.home.unauthorised
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject

class UnauthorisedController @Inject() (
  authenticate: AuthCheckAction,
  mcc: MessagesControllerComponents,
  unauthorisedPage: unauthorised
) extends FrontendController(mcc) with I18nSupport with SelectedClientIdentifier {

  def notEnrolled(): Action[AnyContent] =
    authenticate { implicit authenticatedRequest =>
      // A signed in user has been redirect here for having no enrolment.
      // If this is an agent then they have landed here for one of 2 reasons:
      // - They have not supplied a client identifier
      // - They have supplied a client identifier but failed auth enrolment (invalid identifier or not one of their clients)
      // These can be sent to the agents client page
      val affinityGroup = authenticatedRequest.user.identityData.affinityGroup
      affinityGroup match {
        case Some(AffinityGroup.Agent) =>
          getSelectedClientIdentifierFrom(authenticatedRequest) match {
            case Some(_) =>
              Redirect(agentRoutes.AgentsController.displayPage()).flashing(
                ("clientPPTFailed" -> "true")
              )
            case _ =>
              Redirect(agentRoutes.AgentsController.displayPage())
          }
        case _ =>
          // All other users see the you need to register page.
          Ok(unauthorisedPage())
      }
    }

  def unauthorised: Action[AnyContent] =
    // General auth errors are redirected here
    Action { implicit request =>
      Ok(unauthorisedPage())
    }

}
