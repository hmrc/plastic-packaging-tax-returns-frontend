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

package controllers.amends

import cacheables.AmendObligationCacheable
import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.TaxReturnsConnector
import controllers.actions.JourneyAction
import models.amends.AmendSummaryRow
import models.requests.DataRequest
import models.requests.DataRequest.headerCarrier
import models.returns.{AmendsCalculations, TaxReturnObligation}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results.{Ok, Redirect}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import repositories.SessionRepository.Paths
import services.AmendReturnAnswerComparisonService
import viewmodels.PrintLong
import viewmodels.checkAnswers.amends._
import views.html.amends.CheckYourAnswersView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CheckYourAnswersController @Inject()
(override val messagesApi: MessagesApi,
 journeyAction: JourneyAction,
 returnsConnector: TaxReturnsConnector,
 appConfig: FrontendAppConfig,
 comparisonService: AmendReturnAnswerComparisonService,
 val controllerComponents: MessagesControllerComponents,
 sessionRepository: SessionRepository,
 view: CheckYourAnswersView,
) extends I18nSupport {

  def onPageLoad(): Action[AnyContent] =
    journeyAction.async {
      implicit request =>
        if (!appConfig.isAmendsFeatureEnabled) {
          Future.successful(Redirect(controllers.routes.IndexController.onPageLoad))
        }
        else {
          request.userAnswers.get[TaxReturnObligation](AmendObligationCacheable) match {
            case Some(obligation) =>
              returnsConnector.getCalculationAmends(request.pptReference).map {
                case Right(calculations) => displayPage(request, obligation, calculations)
                case Left(error) => throw error
              }
            case None => Future.successful(Redirect(routes.SubmittedReturnsController.onPageLoad()))
          }
        }
    }

  private def displayPage(request: DataRequest[AnyContent], obligation: TaxReturnObligation,
                          calculations: AmendsCalculations)(implicit r: DataRequest[_]) = {

    val totalRows: Seq[AmendSummaryRow] = Seq(
      AmendManufacturedPlasticPackagingSummary.apply(request.userAnswers),
      AmendImportedPlasticPackagingSummary.apply(request.userAnswers),
      AmendSummaryRow(
        "AmendsCheckYourAnswers.packagingTotal",
        calculations.original.packagingTotal.asKg,
        Some(calculations.amend.packagingTotal.asKg),
        None
      )
    )

    val deductionsRows: Seq[AmendSummaryRow] = Seq(
      AmendDirectExportPlasticPackagingSummary.apply(request.userAnswers),
      AmendHumanMedicinePlasticPackagingSummary.apply(request.userAnswers),
      AmendRecycledPlasticPackagingSummary.apply(request.userAnswers),
      AmendSummaryRow(
        "AmendsCheckYourAnswers.deductionsTotal",
        calculations.original.deductionsTotal.asKg,
        Some(calculations.amend.deductionsTotal.asKg),
        None
      )
    )

    val amendmentMade = comparisonService.hasMadeChangesOnAmend(request.userAnswers)

    Ok(view(obligation, totalRows, deductionsRows, calculations, amendmentMade))
  }

  def onSubmit(): Action[AnyContent] =
    journeyAction.async {
      implicit request =>
        returnsConnector.amend(request.pptReference).flatMap {
          optChargeRef =>
            sessionRepository.set(request.cacheKey, Paths.AmendChargeRef, optChargeRef).map {
              _ => Redirect(routes.AmendConfirmationController.onPageLoad())
            }
        }
    }
  
}
