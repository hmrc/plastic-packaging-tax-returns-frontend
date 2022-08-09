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

package controllers.amends

import cacheables.{ObligationCacheable, ReturnDisplayApiCacheable}
import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.TaxReturnsConnector
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.amends.AmendSummaryRow
import models.returns.{ReturnDisplayApi, TaxReturnObligation}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.{Entry, SessionRepository}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.{PrintBigDecimal, PrintLong}
import viewmodels.checkAnswers.amends._
import views.html.amends.CheckYourAnswersView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  returnsConnector: TaxReturnsConnector,
  appConfig: FrontendAppConfig,
  val controllerComponents: MessagesControllerComponents,
  sessionRepository: SessionRepository,
  view: CheckYourAnswersView
) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request =>
        if (appConfig.isAmendsFeatureEnabled) {
        request.userAnswers.get[TaxReturnObligation](ObligationCacheable) match {
          case Some(obligation) =>

            returnsConnector.getCalculationAmends(request.pptReference).map {
              case Right(calculations) =>
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

                Ok(view(obligation, totalRows, deductionsRows, calculations))
              case Left(error) => throw error
            }
          case None => Future.successful(Redirect(routes.SubmittedReturnsController.onPageLoad()))
        }
      } else {
          Future.successful(Redirect(controllers.routes.IndexController.onPageLoad))
        }
    }

  def onSubmit(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request =>

        val pptId: String = request.request.pptReference

        val submissionId: String = request.userAnswers.get[ReturnDisplayApi](ReturnDisplayApiCacheable)
          .getOrElse(throw new IllegalStateException("must have a submission id to amend a return"))
          .idDetails
          .submissionId

        returnsConnector.amend(pptId, submissionId).flatMap {
          case Right(optChargeRef) =>
            sessionRepository.set(Entry(request.cacheKey, optChargeRef)).map{
              _ => Redirect(routes.AmendConfirmationController.onPageLoad())
            }
          case Left(error) =>
            throw error
        }
    }
}
