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

package controllers.returns

import connectors.CacheConnector
import controllers.actions._
import forms.returns.DirectlyExportedComponentsFormProvider
import models.Mode
import navigation.Navigator
import pages.returns.DirectlyExportedComponentsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.returns.PlasticPackagingTotalSummary
import views.html.returns.DirectlyExportedComponentsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DirectlyExportedComponentsController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: DirectlyExportedComponentsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: DirectlyExportedComponentsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) {
      implicit request =>

        val totalPlastic = PlasticPackagingTotalSummary.calculateTotal(request.userAnswers)

        val preparedForm = request.userAnswers.fill(DirectlyExportedComponentsPage, form)
        Ok(view(preparedForm, mode, totalPlastic))
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request =>
        val pptId: String = request.pptReference
        val totalPlastic = PlasticPackagingTotalSummary.calculateTotal(request.userAnswers)

        form.bindFromRequest().fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, totalPlastic))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(DirectlyExportedComponentsPage, value))
              _              <- cacheConnector.set(pptId, updatedAnswers)

            } yield Redirect(navigator.nextPage(DirectlyExportedComponentsPage, mode, updatedAnswers))
        )
    }

}
