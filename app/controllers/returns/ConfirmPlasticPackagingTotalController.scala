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
import models.requests.DataRequest
import models.requests.DataRequest.headerCarrier
import navigation.ReturnsJourneyNavigator
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.Results.{Ok, Redirect}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.ExportedPlasticAnswer
import viewmodels.checkAnswers.returns.ImportedPlasticPackagingSummary.ConfirmImportedPlasticPackagingSummary
import viewmodels.checkAnswers.returns.ImportedPlasticPackagingWeightSummary.ConfirmImportedPlasticPackagingWeightLabel
import viewmodels.checkAnswers.returns.ManufacturedPlasticPackagingSummary.ConfirmManufacturedPlasticPackaging
import viewmodels.checkAnswers.returns.ManufacturedPlasticPackagingWeightSummary.ConfirmManufacturedPlasticPackagingSummary
import viewmodels.checkAnswers.returns.PlasticPackagingTotalSummary
import viewmodels.govuk.summarylist._
import views.html.returns.ConfirmPlasticPackagingTotalView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ConfirmPlasticPackagingTotalController @Inject()
(
  override val messagesApi: MessagesApi,
  journeyAction: JourneyAction,
  val controllerComponents: MessagesControllerComponents,
  view: ConfirmPlasticPackagingTotalView,
  cacheConnector: CacheConnector,
  navigator: ReturnsJourneyNavigator
) (implicit ec: ExecutionContext)
  extends I18nSupport with Logging {

  def onPageLoad: Action[AnyContent] =
    journeyAction {
      implicit request =>

        NonExportedAmountHelper.totalPlastic(request.userAnswers).fold(
          Redirect(controllers.routes.IndexController.onPageLoad)
        )(_ => Ok(view(createSummaryList(request))))
    }

  def onwardRouting: Action[AnyContent] = {
    journeyAction.async {
      implicit request =>
        ExportedPlasticAnswer(request.userAnswers).resetAllIfNoTotalPlastic
          .save(cacheConnector.saveUserAnswerFunc(request.pptReference))
          .map(updateUserAnswer => Redirect(navigator.confirmTotalPlasticPackagingRoute(updateUserAnswer)))
    }
  }

  private def createSummaryList(request: DataRequest[AnyContent])(implicit messages: Messages) = {
    SummaryListViewModel(rows =
      Seq(
        ConfirmManufacturedPlasticPackaging,
        ConfirmManufacturedPlasticPackagingSummary,
        ConfirmImportedPlasticPackagingSummary,
        ConfirmImportedPlasticPackagingWeightLabel,
        PlasticPackagingTotalSummary
      ).flatMap(_.row(request.userAnswers))
    )
  }
}