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

  // TODO - get this from the calculation (back end end point) and append to each table
  private def totalRow(originalTotal: Long, amendedTotal: Long, key: String)
                      (implicit messages: Messages) = {
    AmendSummaryRow(
      messages(key),
      originalTotal.asKg,
      Some(amendedTotal.asKg),
      None
    )
  }

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request =>

        request.userAnswers.get[TaxReturnObligation](ObligationCacheable) match {
          case Some(obligation) =>

            returnsConnector.getCalculationAmends(request.pptReference).map {
              case Right(calculations) =>
                val totalRows: Seq[AmendSummaryRow] = Seq(
                  AmendManufacturedPlasticPackagingSummary.apply(request.userAnswers),
                  AmendImportedPlasticPackagingSummary.apply(request.userAnswers),
                  totalRow(
                    calculations.original.packagingTotal,
                    calculations.amend.packagingTotal,
                    "AmendsCheckYourAnswers.packagingTotal")
                )

                val deductionsRows: Seq[AmendSummaryRow] = Seq(
                  AmendDirectExportPlasticPackagingSummary.apply(request.userAnswers),
                  AmendHumanMedicinePlasticPackagingSummary.apply(request.userAnswers),
                  AmendRecycledPlasticPackagingSummary.apply(request.userAnswers),
                  totalRow(
                    calculations.original.deductionsTotal,
                    calculations.amend.deductionsTotal,
                    "AmendsCheckYourAnswers.deductionsTotal")
                )

                val calculationRows: Seq[AmendSummaryRow] = Seq(
                  AmendSummaryRow(
                    "AmendsCheckYourAnswers.calculation.row.1",
                    calculations.original.packagingTotal.asKg,
                    Some(calculations.amend.packagingTotal.asKg),
                    None
                  ),
                  AmendSummaryRow(
                    "AmendsCheckYourAnswers.calculation.row.2",
                    calculations.original.deductionsTotal.asKg,
                    Some(calculations.amend.deductionsTotal.asKg),
                    None
                  ),
                  AmendSummaryRow(
                    "AmendsCheckYourAnswers.calculation.row.3",
                    calculations.original.taxDue.asPounds,
                    Some(calculations.amend.taxDue.asPounds),
                    None
                  )
                )

                if (appConfig.isAmendsFeatureEnabled) {
                  Ok(view(obligation, totalRows, deductionsRows, calculationRows))
                }
                else {
                  Redirect(controllers.routes.IndexController.onPageLoad)
                }
              case Left(error) => throw error
            }
          case None => Future.successful(Redirect(routes.SubmittedReturnsController.onPageLoad()))

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
