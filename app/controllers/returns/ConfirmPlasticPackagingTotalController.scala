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
import models.Mode.NormalMode
import pages.returns._
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.returns.ImportedPlasticPackagingSummary.ConfirmImportedPlasticPackagingSummary
import viewmodels.checkAnswers.returns.ImportedPlasticPackagingWeightSummary.ConfirmImportedPlasticPackagingWeightLabel
import viewmodels.checkAnswers.returns.ManufacturedPlasticPackagingSummary.ConfirmManufacturedPlasticPackaging
import viewmodels.checkAnswers.returns.ManufacturedPlasticPackagingWeightSummary.ConfirmManufacturedPlasticPackagingSummary
import viewmodels.checkAnswers.returns.PlasticPackagingTotalSummary
import viewmodels.govuk.summarylist._
import views.html.returns.ConfirmPlasticPackagingTotalView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConfirmPlasticPackagingTotalController @Inject()
(
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: ConfirmPlasticPackagingTotalView,
  cacheConnector: CacheConnector
) (implicit ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad: Action[AnyContent] =
    (identify andThen getData andThen requireData) {
      implicit request =>
        val summaryList: SummaryList = SummaryListViewModel(rows =
          Seq(
            ConfirmManufacturedPlasticPackaging,
            ConfirmManufacturedPlasticPackagingSummary,
            ConfirmImportedPlasticPackagingSummary,
            ConfirmImportedPlasticPackagingWeightLabel,
            PlasticPackagingTotalSummary
          ).flatMap(_.row(request.userAnswers))
        )
        Ok(view(summaryList))
    }

  def onwardRouting: Action[AnyContent] = {
    (identify andThen getData andThen requireData).async {
      implicit request =>
        val totalPlastic = PlasticPackagingTotalSummary.calculateTotal(request.userAnswers)

        if(totalPlastic <= 0) {
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers
              .set(DirectlyExportedComponentsPage, false).get
              .set(ExportedPlasticPackagingWeightPage, 0L, cleanup = false).get
              .set(NonExportedHumanMedicinesPlasticPackagingPage, false, cleanup = false).get
              .set(NonExportedHumanMedicinesPlasticPackagingWeightPage, 0L, cleanup = false).get
              .set(NonExportedRecycledPlasticPackagingPage, false, cleanup = false).get
              .set(NonExportedRecycledPlasticPackagingWeightPage, 0L, cleanup = false)
            )
            _ <- cacheConnector.set(request.pptReference, updatedAnswers)

          } yield Redirect(controllers.returns.routes.ReturnsCheckYourAnswersController.onPageLoad())
        } else {
          Future.successful(Redirect(controllers.returns.routes.DirectlyExportedComponentsController.onPageLoad(NormalMode)))
        }
    }
  }
}