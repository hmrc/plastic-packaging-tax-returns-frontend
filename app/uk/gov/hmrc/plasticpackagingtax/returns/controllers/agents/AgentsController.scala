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

package uk.gov.hmrc.plasticpackagingtax.returns.controllers.agents

import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.actions.AuthAction
import uk.gov.hmrc.plasticpackagingtax.returns.forms.agents.ClientIdentifier
import uk.gov.hmrc.plasticpackagingtax.returns.models.request.JourneyAction
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.agents.agents_page
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AgentsController @Inject() (
  authenticate: AuthAction,
  journeyAction: JourneyAction,
  mcc: MessagesControllerComponents,
  page: agents_page
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  val displayPage: Action[AnyContent] = Action.async { implicit request =>
    // TODO needs to be wrapped in an agents only auth
    val form = ClientIdentifier.form().fill(ClientIdentifier(""))
    Future.successful(Ok(page(form)))
  }

  val submit: Action[AnyContent] = Action.async { implicit request =>
    // TODO needs to be wrapped in an agents only auth
    // Catch client identifier; validate and stash somewhere accessible pre auth and only to this user
    ClientIdentifier.form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[ClientIdentifier]) =>
          Future.successful(BadRequest(page(formWithErrors))),
        clientIdentifier =>
          // Set this on the session and then redirect account
          Future.successful(Ok("Got client identifier: " + clientIdentifier.identifier))
      )
  }

}
