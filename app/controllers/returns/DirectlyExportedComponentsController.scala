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

package controllers.returns

import connectors.CacheConnector
import controllers.actions._
import controllers.helpers.NonExportedAmountHelper
import forms.returns.DirectlyExportedComponentsFormProvider
import models.Mode
import models.requests.DataRequest
import models.requests.DataRequest.headerCarrier
import navigation.ReturnsJourneyNavigator
import pages.returns.DirectlyExportedComponentsPage
import play.api.data.Form
import play.api.data.FormBinding.Implicits.formBinding
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results.{BadRequest, Ok, Redirect}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import views.html.returns.DirectlyExportedComponentsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DirectlyExportedComponentsController @Inject()(
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  navigator: ReturnsJourneyNavigator,
  journeyAction: JourneyAction,
  nonExportedAmountHelper: NonExportedAmountHelper,
  form: DirectlyExportedComponentsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: DirectlyExportedComponentsView
)(implicit ec: ExecutionContext)
  extends I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] =
    journeyAction {
      implicit request =>

        nonExportedAmountHelper.totalPlastic(request.userAnswers).fold(
          Redirect(controllers.routes.IndexController.onPageLoad)
        )(totalPlastic => {
          val preparedForm = request.userAnswers.fill(DirectlyExportedComponentsPage, form())
          Ok(view(preparedForm, mode, totalPlastic))
        })
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    journeyAction.async {
      implicit request =>

        form().bindFromRequest().fold(
          formWithErrors => handleErrorInForm(mode, formWithErrors),
          newAnswer =>
            request.userAnswers
              .setOrFail(DirectlyExportedComponentsPage, newAnswer)
              .save(cacheConnector.saveUserAnswerFunc(request.pptReference))
              .map(_ => Redirect(navigator.directlyExportedComponentsRoute(newAnswer, mode)))
        )
    }

  private def handleErrorInForm(
    mode: Mode,
    formWithErrors: Form[Boolean]
  )(implicit request: DataRequest[AnyContent]): Future[Result] = {
    nonExportedAmountHelper.totalPlastic(request.userAnswers).fold(
      Future.successful(Redirect(controllers.routes.IndexController.onPageLoad)))(
      totalPlastic => Future.successful(BadRequest(view(formWithErrors, mode, totalPlastic)))
    )
  }
}
