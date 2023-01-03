/*
 * Copyright 2023 HM Revenue & Customs
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

import controllers.actions._
import controllers.agents.SelectedClientIdentifier
import forms.AgentsFormProvider

import javax.inject.Inject
import models.Mode
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.AgentsView

import scala.concurrent.ExecutionContext

class AgentsController @Inject()(
                                  override val messagesApi: MessagesApi,
                                  identify: AuthAgentAction,
                                  form: AgentsFormProvider,
                                  val controllerComponents: MessagesControllerComponents,
                                  view: AgentsView
                                )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with SelectedClientIdentifier {


  def onPageLoad(mode: Mode): Action[AnyContent] = identify {
    implicit request =>

      val currentlySelectedClientIdentifier = getSelectedClientIdentifierFrom(request)

      // Look for a flash signal if the client identifier on the session has failed auth
      request.flash.get("clientPPTFailed") match {

        case Some(_) =>
          val errorForm = form().fill(
            currentlySelectedClientIdentifier.getOrElse("")
          ).withError("identifier", "agents.client.identifier.auth.error")
          Forbidden(view(errorForm, mode))
        case _ =>
          val preparedForm = form().fill(
            currentlySelectedClientIdentifier.getOrElse("")
          )

          Ok(view(preparedForm, mode))
      }

  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify) {
    implicit request =>

      form().bindFromRequest().fold(
        formWithErrors =>
          BadRequest(view(formWithErrors, mode)),
        value => {

          // Set this on the session and then redirect to account page to attempt to auth with it
          appendSelectedClientIdentifierToResult(value, Redirect(routes.IndexController.onPageLoad))

        }
      )
  }
}
