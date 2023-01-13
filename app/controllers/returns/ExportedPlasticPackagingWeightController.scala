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
import controllers.actions.JourneyAction
import controllers.helpers.NonExportedAmountHelper
import forms.returns.ExportedPlasticPackagingWeightFormProvider
import models.Mode
import models.Mode._
import models.requests.DataRequest
import models.requests.DataRequest._
import navigation.ReturnsJourneyNavigator
import pages.returns.ExportedPlasticPackagingWeightPage
import play.api.data.Form
import play.api.data.FormBinding.Implicits.formBinding
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results.{BadRequest, Ok, Redirect}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.ExportedPlasticAnswer
import views.html.returns.ExportedPlasticPackagingWeightView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ExportedPlasticPackagingWeightController @Inject()(
                                                          override val messagesApi: MessagesApi,
                                                          cacheConnector: CacheConnector,
                                                          navigator: ReturnsJourneyNavigator,
                                                          nonExportedAmountHelper: NonExportedAmountHelper,
                                                          journeyAction: JourneyAction,
                                                          form: ExportedPlasticPackagingWeightFormProvider,
                                                          val controllerComponents: MessagesControllerComponents,
                                                          view: ExportedPlasticPackagingWeightView
)(implicit ec: ExecutionContext)
  extends I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] =
    journeyAction {
      implicit request =>

        val preparedForm = request.userAnswers.fill(ExportedPlasticPackagingWeightPage, form())
        nonExportedAmountHelper.totalPlastic(request.userAnswers).fold(
            Redirect(controllers.routes.IndexController.onPageLoad)
          )(totalPlastic => Ok(view(preparedForm, mode, totalPlastic))
        )

    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    journeyAction.async {
      implicit request =>

        form().bindFromRequest().fold(
          formWithErrors =>
            Future.successful(handleTotalPlasticCalculationError(mode, formWithErrors)),
          value =>
            request.userAnswers
              .setOrFail(ExportedPlasticPackagingWeightPage, value, isCleanUp(mode))
              .save(cacheConnector.saveUserAnswerFunc(request.pptReference))
              .map(updatedAnswers =>
                Redirect(navigator.exportedPlasticPackagingWeightRoute(
                  ExportedPlasticAnswer(updatedAnswers).isAllPlasticExported,
                  mode))
              )
        )
    }

  private def handleTotalPlasticCalculationError(
    mode: Mode,
    formWithErrors: Form[Long]
  )(implicit request: DataRequest[AnyContent]) = {
    nonExportedAmountHelper.totalPlastic(request.userAnswers).fold(
      Redirect(controllers.routes.IndexController.onPageLoad)
    )(totalPlastic => BadRequest(view(formWithErrors, mode, totalPlastic)))
  }

  private def isCleanUp(mode: Mode) =
    if(mode == CheckMode) false else true
}
