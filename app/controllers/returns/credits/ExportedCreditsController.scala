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

package controllers.returns.credits

import connectors.CacheConnector
import controllers.actions._
import forms.returns.credits.ExportedCreditsFormProvider
import models.Mode
import navigation.ReturnsJourneyNavigator
import pages.returns.credits.ExportedCreditsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.returns.credits.ExportedCreditsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ExportedCreditsController @Inject()
(
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  navigator: ReturnsJourneyNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ExportedCreditsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ExportedCreditsView
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val userAnswers = request.userAnswers
      val preparedForm = userAnswers.get(ExportedCreditsPage) match {
        case None => formProvider()
        case Some(value) => formProvider().fill(value)
      }
      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request =>

        formProvider()
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode))),

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(ExportedCreditsPage, value))
                _ <- cacheConnector.set(request.pptReference, updatedAnswers)
              } yield Redirect(navigator.exportedCreditsRoute(mode))
          )
    }
}
