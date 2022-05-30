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

package controllers

import controllers.actions._
import models.NormalMode
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.ImportedPlasticPackagingSummary.ConfirmImportedPlasticPackagingSummary
import viewmodels.checkAnswers.ImportedPlasticPackagingWeightSummary.ConfirmImportedPlasticPackagingWeightLabel
import viewmodels.checkAnswers.ManufacturedPlasticPackagingSummary.ConfirmManufacturedPlasticPackaging
import viewmodels.checkAnswers.ManufacturedPlasticPackagingWeightSummary.ConfirmManufacturedPlasticPackagingSummary
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._
import views.html.ConfirmPlasticPackagingTotalView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class ConfirmPlasticPackagingTotalController @Inject()
(
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: ConfirmPlasticPackagingTotalView
) (implicit ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad: Action[AnyContent] =
    (identify andThen getData andThen requireData) {
    implicit request =>
      Try(SummaryListViewModel(rows =
        Seq(
          ConfirmManufacturedPlasticPackaging,
          ConfirmManufacturedPlasticPackagingSummary,
          ConfirmImportedPlasticPackagingSummary,
          ConfirmImportedPlasticPackagingWeightLabel,
          PlasticPackagingTotalSummary
        ).flatMap(_.row(request.userAnswers))
      )) match {
        case Success(list) => Ok(view(list))
        case Failure(error) =>
          logger.error(error.getMessage)
          Redirect(routes.IndexController.onPageLoad)
      }
  }
}
