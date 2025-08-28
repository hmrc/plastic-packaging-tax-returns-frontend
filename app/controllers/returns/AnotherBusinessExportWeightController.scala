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

package controllers.returns

import connectors.CacheConnector
import controllers.actions._
import controllers.helpers.NonExportedAmountHelper
import forms.returns.AnotherBusinessExportWeightFormProvider
import models.Mode
import models.Mode.CheckMode
import models.requests.DataRequest.headerCarrier
import navigation.ReturnsJourneyNavigator
import pages.returns.AnotherBusinessExportedWeightPage
import play.api.data.FormBinding.Implicits.formBinding
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Results}
import services.ExportedPlasticAnswer
import views.html.returns.AnotherBusinessExportWeightView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AnotherBusinessExportWeightController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  returnsNavigator: ReturnsJourneyNavigator,
  journeyAction: JourneyAction,
  form: AnotherBusinessExportWeightFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AnotherBusinessExportWeightView,
  nonExportedAmountHelper: NonExportedAmountHelper
)(implicit ec: ExecutionContext)
    extends Results
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = journeyAction { implicit request =>
    val preparedForm = request.userAnswers.fill(AnotherBusinessExportedWeightPage, form())

    nonExportedAmountHelper.totalPlasticAdditions(request.userAnswers)
      .fold(Redirect(controllers.routes.IndexController.onPageLoad))(totalPlastic =>
        Ok(view(totalPlastic, preparedForm, mode))
      )
  }

  def onSubmit(mode: Mode): Action[AnyContent] = journeyAction.async { implicit request =>
    form().bindFromRequest().fold(
      formWithErrors =>
        Future.successful(
          nonExportedAmountHelper.totalPlasticAdditions(request.userAnswers)
            .fold(Redirect(controllers.routes.IndexController.onPageLoad))(totalPlastic =>
              BadRequest(view(totalPlastic, formWithErrors, mode))
            )
        ),
      value =>
        request.userAnswers
          .setOrFail(AnotherBusinessExportedWeightPage, value, mode != CheckMode)
          .save(cacheConnector.saveUserAnswerFunc(request.pptReference))
          .map(updatedUserAnswers =>
            Redirect(
              returnsNavigator.exportedByAnotherBusinessWeightRoute(
                ExportedPlasticAnswer(updatedUserAnswers).isAllPlasticExported,
                mode
              )
            )
          )
    )
  }
}
