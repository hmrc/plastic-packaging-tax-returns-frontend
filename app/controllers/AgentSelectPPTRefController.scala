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

import controllers.actions.AuthenticatedIdentifierAction.IdentifierAction.{pptEnrolmentIdentifierName, pptEnrolmentKey}
import controllers.actions._
import forms.AgentsFormProvider
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import repositories.SessionRepository.Paths.AgentSelectedPPTRef
import uk.gov.hmrc.auth.core.retrieve.EmptyRetrieval
import uk.gov.hmrc.auth.core.{AuthConnector, Enrolment, InsufficientEnrolments}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.AgentsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AgentSelectPPTRefController @Inject() (
  override val messagesApi: MessagesApi,
  authConnector: AuthConnector,
  sessionRepository: SessionRepository,
  identify: AuthAgentAction,
  form: AgentsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AgentsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = identify.async { implicit request =>
    sessionRepository
      .get[String](request.internalId, AgentSelectedPPTRef)
      .map { maybeSelectedClientIdentifier =>
        val preparedForm = form().fill(
          maybeSelectedClientIdentifier.getOrElse("")
        )
        Ok(view(preparedForm))
      }
  }

  def onSubmit(): Action[AnyContent] = identify.async { implicit request =>
    form()
      .bindFromRequest().fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
        selectedClientIdentifier =>
          {
            for {
              _ <- authConnector.authorise(
                Enrolment(pptEnrolmentKey)
                  .withIdentifier(pptEnrolmentIdentifierName, selectedClientIdentifier)
                  .withDelegatedAuthRule("ppt-auth"),
                EmptyRetrieval
              )
              _ <- sessionRepository.set(
                request.internalId,
                AgentSelectedPPTRef,
                selectedClientIdentifier
              ) // this will only happen if authConnector.authorise does NOT fail
            } yield Redirect(routes.IndexController.onPageLoad)
          }.recover { case _: InsufficientEnrolments =>
            val errorForm = form()
              .fill(selectedClientIdentifier)
              .withError("value", "agents.client.identifier.auth.error")
            BadRequest(view(errorForm))
          }
      )
  }
}
